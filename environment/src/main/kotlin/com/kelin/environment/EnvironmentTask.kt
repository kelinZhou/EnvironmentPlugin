package com.kelin.environment

import com.android.build.gradle.AppExtension
import com.android.builder.model.ClassField
import com.kelin.environment.extension.EnvironmentExtension
import com.kelin.environment.extension.PackageConfigExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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

    val release = EnvType.RELEASE
    val dev = EnvType.DEV
    val test = EnvType.TEST
    val demo = EnvType.DEMO

    var online = false
    var initEnvironment = release

    private val envGenerators = ArrayList<GeneratedEnvConfig>()
    private val releaseExt by lazy { project.extensions.findByName("releaseEnv") as EnvironmentExtension }
    private val devExt by lazy { project.extensions.findByName("devEnv") as EnvironmentExtension }
    private val testExt by lazy { project.extensions.findByName("testEnv") as EnvironmentExtension }
    private val demoExt by lazy { project.extensions.findByName("demoEnv") as EnvironmentExtension }

    private val config by lazy {
        if (online) {
            project.extensions.findByName("releaseConfig") as PackageConfigExtension
        } else {
            project.extensions.findByName("devConfig") as PackageConfigExtension
        }
    }

    val versionCode: Int
        get() {
            return when {
                config.versionCode != -1 -> return config.versionCode
                else -> Date().let { SimpleDateFormat("yyMMddHH", Locale.CHINA).format(it).toInt() }
            }
        }

    val versionName: String
        get() {
            return when {
                config.versionName.isNotEmpty() -> config.versionName
                config.versionCode != -1 -> {
                    val codeStr = config.versionCode.toString()
                    if (codeStr.length > 3) {
                        throw RuntimeException("versionCode:${config.versionCode} does not support.Your versionCode’s length must be <= 3, such as 100.")
                    } else {
                        when (codeStr.length) {
                            1 -> "0.0.$codeStr"
                            2 -> "0.${codeStr.toCharArray().joinToString(".")}"
                            3 -> codeStr.toCharArray().joinToString(".")
                            else -> throw RuntimeException("versionCode:${config.versionCode} does not support.Your versionCode’s length must be <= 3, such as 100.")
                        }
                    }
                }
                else -> throw RuntimeException("You need set the versionName's value for ${if (online) "releaseConfig" else "devConfig"}.")
            }
        }

    val applicationId: String
        get() {
            return when {
                config.applicationId.isNotEmpty() -> config.applicationId
                else -> throw RuntimeException("You need set the versionName's value for ${if (online) "releaseConfig" else "devConfig"}.")
            }
        }

    val variables: Map<String, String>?
        get() {
            return config.variables
        }

    fun getVariable(key: String): String {
        return config.variables?.get(key) ?: ""
    }

    private fun getCurrentVariant(): Array<String> {
        val taskRequests = project.gradle.startParameter.taskRequests
        val tskReqStr = taskRequests.toString()
        println("startParameter:$tskReqStr")
        var start = tskReqStr.indexOf("app:generate")
        val channel = if (start > 0) {
            var end = tskReqStr.indexOf("ReleaseSources", start)
            end = if (end > 0) end else tskReqStr.indexOf("DebugSources", start)
            if (end > 0) {
                tskReqStr.substring(start + 12, end).toLowerCase(Locale.getDefault())
            } else {
                ""
            }
        } else {
            start = tskReqStr.indexOf("app:assemble")
            if (start > 0) {
                var end = tskReqStr.indexOf("Release", start)
                end = if (end > 0) end else tskReqStr.indexOf("Debug", start)
                if (end > 0) {
                    tskReqStr.substring(start + 12, end).toLowerCase(Locale.getDefault())
                } else {
                    ""
                }
            } else {
                ""
            }
        }
        return arrayOf(
            channel,
            if (tskReqStr.toLowerCase(Locale.getDefault()).contains("release") || tskReqStr.contains(
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
        if (online) {
            initEnvironment = EnvType.RELEASE
        }
        require(releaseExt.variables.isNotEmpty()) { "you must have release environment, you need called the releaseEnv method!" }
        if (!online) {
            devExt.mergeVariables(releaseExt.variables)
            if (devExt.alias.isEmpty()) {
                devExt.alias = "Dev"
            }
            testExt.mergeVariables(releaseExt.variables)
            if (testExt.alias.isEmpty()) {
                testExt.alias = "Test"
            }
            demoExt.mergeVariables(releaseExt.variables)
            if (demoExt.alias.isEmpty()) {
                demoExt.alias = "Demo"
            }
        }

        val appExt = project.extensions.findByType<AppExtension>(AppExtension::class.java)
        val app = appExt?.applicationVariants

        val info = getCurrentVariant()
        val channel = info[0]
        val type = info[1]
        println("Channel: ${if (channel.isEmpty()) "unknown" else channel}")
        println("BuildType: $type")
        app?.all { variant ->
            if (variant.name.toLowerCase(Locale.getDefault()).contains("$channel$type")) {
                println("\nGenerate placeholder for ${variant.name}:\n")
                when (initEnvironment) {
                    EnvType.RELEASE -> {
                        releaseExt.variables
                    }
                    EnvType.DEV -> {
                        devExt.variables
                    }
                    EnvType.TEST -> {
                        testExt.variables
                    }
                    EnvType.DEMO -> {
                        demoExt.variables
                    }
                }.forEach {
                    if (it.value.placeholder) {
                        println("${it.key} | ${it.value.value}")
                        variant.mergedFlavor.manifestPlaceholders[it.key] = it.value.value
                    }
                    if (config.appIcon.isNotEmpty()) {
                        variant.mergedFlavor.manifestPlaceholders["APP_ICON"] = config.appIcon
                    }
                    if (config.appRoundIcon.isNotEmpty()) {
                        variant.mergedFlavor.manifestPlaceholders["APP_ROUND_ICON"] =
                            config.appRoundIcon
                    }
                    if (config.appName.isNotEmpty()) {
                        variant.mergedFlavor.manifestPlaceholders["APP_NAME"] = config.appName
                    }
                }
                println()

                val buildConfig = variant.generateBuildConfigProvider.get()
                buildConfig.doFirst {
                    buildConfig.items.add(object : ClassField {
                        override fun getName(): String {
                            return "IS_DEBUG"
                        }

                        override fun getAnnotations(): MutableSet<String> {
                            return mutableSetOf()
                        }

                        override fun getType(): String {
                            return "boolean"
                        }

                        override fun getValue(): String {
                            return "Boolean.parseBoolean(\"${if (online) "false" else "true"}\")"
                        }

                        override fun getDocumentation(): String {
                            return "Created by EnvironmentPlugin to indicate whether the current is or not Debug state."
                        }
                    })
                }
                envGenerators.add(
                    GeneratedEnvConfig(
                        buildConfig.sourceOutputDir.absolutePath,
                        buildConfig.buildConfigPackageName,
                        initEnvironment,
                        online,
                        variant.versionName ?: "",
                        releaseExt,
                        devExt,
                        testExt,
                        demoExt
                    )
                )
                buildConfig.doLast {
                    envGenerators.forEach { it.generate() }

                    println("PackageVariables:")
                    config.variables?.forEach {
                        println("${it.key} : ${it.value}")
                    }
                    println("\nVersionInfo:")
                    println("Code: $versionCode")
                    println("Name: $versionName")
                    println("\n==========☆★ Environment Plugin End ★☆==========\n")
                }
            }
        }
    }
}
