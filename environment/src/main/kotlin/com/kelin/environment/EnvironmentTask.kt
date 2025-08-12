package com.kelin.environment

import com.android.build.gradle.AppExtension
import com.kelin.environment.extension.EnvironmentExtension
import com.kelin.environment.extension.TaskExtension
import com.kelin.environment.model.Version
import com.kelin.environment.model.lessThan
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.util.Locale

/**
 * **描述:** 用来配置环境的Task。
 *
 *
 * **创建人:** kelin
 *
 *
 * **创建时间:** 2019-09-06  13:38
 *
 *
 * **版本:** v 1.0.0
 */
open class EnvironmentTask : DefaultTask() {

    private val envGenerators = ArrayList<GeneratedEnvConfig>()

    private val environment by lazy { project.extensions.findByName("environment") as TaskExtension }


    private val releaseExt by lazy { project.extensions.findByName("releaseEnv") as EnvironmentExtension }
    private val devExt by lazy { project.extensions.findByName("devEnv") as EnvironmentExtension }
    private val testExt by lazy { project.extensions.findByName("testEnv") as EnvironmentExtension }
    private val demoExt by lazy { project.extensions.findByName("demoEnv") as EnvironmentExtension }

    private fun getCurrentVariant(): Pair<String, String> {
        val taskRequests = project.gradle.startParameter.taskRequests
        val tskReqStr = taskRequests.toString()
        println("startParameter:$tskReqStr")
        var start = tskReqStr.indexOf("app:generate")
        val channel = if (start > 0) {
            var end = tskReqStr.indexOf("ReleaseSources", start)
            end = if (end > 0) end else tskReqStr.indexOf("DebugSources", start)
            if (end > 0) {
                tskReqStr.substring(start + 12, end).lowercase(Locale.getDefault())
            } else {
                ""
            }
        } else {
            start = tskReqStr.indexOf("app:assemble")
            if (start > 0) {
                var end = tskReqStr.indexOf("Release", start)
                end = if (end > 0) end else tskReqStr.indexOf("Debug", start)
                if (end > 0) {
                    tskReqStr.substring(start + 12, end).lowercase(Locale.getDefault())
                } else {
                    ""
                }
            } else {
                ""
            }
        }
        return Pair(
            channel,
            if (tskReqStr.lowercase(Locale.getDefault())
                    .contains("release") || tskReqStr.contains(
                    "aR"
                )
            ) "release" else "debug"
        )
    }

    @TaskAction
    fun publicEnvironment() {
        println("\n==========☆★ Environment Plugin Beginning ★☆==========\n")
        if (releaseExt.alias.isEmpty()) {
            releaseExt.alias = "Release"
        }
        if (environment.online) {
            environment.initEnvironment = EnvType.RELEASE
        }
        require(releaseExt.variables.isNotEmpty()) { "you must have release environment, you need called the releaseEnv method!" }
        if (!environment.online) {
//            devExt.mergeVariables(releaseExt.variables)
            if (devExt.alias.isEmpty()) {
                devExt.alias = "Dev"
            }
//            testExt.mergeVariables(releaseExt.variables)
            if (testExt.alias.isEmpty()) {
                testExt.alias = "Test"
            }
//            demoExt.mergeVariables(releaseExt.variables)
            if (demoExt.alias.isEmpty()) {
                demoExt.alias = "Demo"
            }
        }
        environment.fixPlaceholder()
        val appExt = project.extensions.findByType(AppExtension::class.java)
        val app = appExt?.applicationVariants
        val defaultConfig = appExt?.defaultConfig
        val info = getCurrentVariant()
        val channel = info.first
        val type = info.second
        println("Channel: ${channel.ifEmpty { "unknown" }}")
        println("BuildType: $type")
        if (defaultConfig != null || !app.isNullOrEmpty()) {
            println("\n------Generate placeholder Beginning------\n")
            environment.manifestPlaceholders.forEach {
                println("${it.key} : ${it.value}")
            }
            println("\n------Generate placeholder End------\n")
        }
        defaultConfig?.manifestPlaceholders?.putAll(environment.manifestPlaceholders)
        app?.all { variant ->
            if (variant.name.lowercase(Locale.getDefault()).contains("$channel$type")) {
                variant.mergedFlavor.manifestPlaceholders.also { manifestPlaceholders ->
                    manifestPlaceholders.putAll(environment.manifestPlaceholders)
                }
                variant.generateBuildConfigProvider.get().run {
                    val support = Version(project.gradle.gradleVersion).lessThan(Version("6.7.1"))
                    println("GradleVersion:${project.gradle.gradleVersion}|support:${support}")
                    val srcOutputDir = if (support) {
                        sourceOutputDir.absolutePath
                    } else {
                        "${project.buildDir.absolutePath}/generated/source/buildConfig/${variant.dirName}"
                    }
                    println("SrcOutputDir:${srcOutputDir}")
                    val packageName = environment.envPackage.ifBlank { environment.applicationId.ifBlank { variant.applicationId } }
                    println("BuildConfigPackageName:${packageName}")
                    envGenerators.add(
                        GeneratedEnvConfig(
                            srcOutputDir,
                            packageName,
                            environment.initEnvironment,
                            environment.online,
                            variant.versionName ?: "",
                            environment.enabledConfig,
                            environment.innerConstants,
                            environment.allVariables,
                            releaseExt,
                            devExt,
                            testExt,
                            demoExt
                        )
                    )
                    doLast {
                        envGenerators.forEach { it.generate() }

                        println("Generated Constants:")
                        environment.innerConstants.forEach { c ->
                            println("${c.key} : ${c.value}")
                        }

                        println("\nBaseVariables:")
                        environment.innerVariables.forEach { v ->
                            println("${v.key} : ${v.value}")
                        }
                        println("\nPackageVariables:")
                        environment.curConfig.variables.forEach { v ->
                            println("${v.key} : ${v.value}")
                        }
                        println("\nVersionInfo:")
                        println("Code: ${versionCode.get()}")
                        println("Name: ${versionName.get()}")
                        println("\n==========☆★ Environment Plugin End ★☆==========\n")
                    }
                }
            }
        }
    }
}
