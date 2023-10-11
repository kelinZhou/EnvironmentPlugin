package com.kelin.environment

import com.android.build.gradle.AppExtension
import com.android.builder.model.BaseConfig
import com.kelin.environment.extension.EnvironmentExtension
import com.kelin.environment.extension.PackageConfigExtension
import com.kelin.environment.model.Version
import com.kelin.environment.model.lessThan
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

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
open class EnvironmentTask : DefaultTask(), VariableExtension, ImmutableExtension {

    @get:Input
    val release = EnvType.RELEASE

    @get:Input
    val dev = EnvType.DEV

    @get:Input
    val test = EnvType.TEST

    @get:Input
    val demo = EnvType.DEMO

    @get:Input
    var online = false

    @get:Input
    var initEnvironment = release

    @get:Input
    val manifestPlaceholders: MutableMap<String, Any>
        get() = LinkedHashMap<String, Any>().apply {
            if (config.appIcon.isNotEmpty()) {
                this["APP_ICON"] = config.appIcon
            }
            if (config.appRoundIcon.isNotEmpty()) {
                this["APP_ROUND_ICON"] =
                    config.appRoundIcon
            }
            if (config.appName.isNotEmpty()) {
                this["APP_NAME"] = config.appName
            }
            innerConstants.forEach {
                if (it.value.placeholder) {
                    this[it.key.toUpperCase(Locale.US)] = it.value.value
                }
            }
            when (initEnvironment) {
                EnvType.RELEASE -> releaseExt
                EnvType.DEV -> devExt
                EnvType.TEST -> testExt
                EnvType.DEMO -> demoExt
            }.createManifestPlaceholders(this, allVariables)
        }

    private val innerVariables = HashMap<String, EnvValue>()
    private val innerConstants = HashMap<String, EnvValue>()

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

    @get:Input
    val appName: String
        get() = config.appName

    @get:Input
    val appIcon: String
        get() = config.appIcon

    @get:Input
    val appRoundIcon: String
        get() = config.appRoundIcon

    @get:Input
    val versionCode: Int
        get() {
            return when {
                config.versionCode != -1 -> return config.versionCode
                else -> Date().let { SimpleDateFormat("yyMMddHH", Locale.CHINA).format(it).toInt() }
            }
        }

    @get:Input
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

    @get:Input
    val applicationId: String
        get() = config.applicationId

    private val allVariables: Map<String, EnvValue>
        get() {
            return LinkedHashMap<String, EnvValue>().apply {
                putAll(innerVariables)
                putAll(config.variables)
                putAll(currentEnvVariables)
            }
        }

    private fun fixPlaceholder() {
        ArrayList(innerVariables.entries).apply {
            addAll(config.variables.entries)
            addAll(currentEnvVariables.entries)
        }.groupBy { it.key }.forEach { group ->
            if (group.value.size > 1) {
                group.value.forEach { v ->
                    v.value.placeholder = group.value.any { entry -> entry.value.placeholder }
                }
            }
        }
    }

    private val currentEnvVariables: HashMap<String, EnvValue>
        get() = LinkedHashMap(releaseExt.variables).apply {
            if (online) {
                releaseExt
            } else {
                when (initEnvironment) {
                    EnvType.RELEASE -> releaseExt
                    EnvType.DEV -> devExt
                    EnvType.TEST -> testExt
                    EnvType.DEMO -> demoExt
                }
            }.variables.forEach {
                put(it.key, it.value)
            }
        }

    override fun constant(name: String, value: EnvValue) {
        innerConstants[name] = value
    }

    override fun variable(name: String, value: EnvValue) {
        innerVariables[name] = value
    }

    fun getVariable(key: String): String {
        return innerVariables[key]?.value ?: config.getVariable(key)
        ?: currentEnvVariables[key]?.value
        ?: ""
    }

    fun getConstant(key: String): String {
        return innerConstants[key]?.value ?: ""
    }

    private fun getCurrentVariant(): Pair<String, String> {
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
        return Pair(
            channel,
            if (tskReqStr.toLowerCase(Locale.getDefault())
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
        if (online) {
            initEnvironment = EnvType.RELEASE
        }
        require(releaseExt.variables.isNotEmpty()) { "you must have release environment, you need called the releaseEnv method!" }
        if (!online) {
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
        fixPlaceholder()
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
            manifestPlaceholders.forEach {
                println("${it.key} : ${it.value}")
            }
            println("\n------Generate placeholder End------\n")
        }
        defaultConfig?.manifestPlaceholders?.putAll(this.manifestPlaceholders)
        app?.all { variant ->
            if (variant.name.toLowerCase(Locale.getDefault()).contains("$channel$type")) {
                variant.mergedFlavor.manifestPlaceholders.also { manifestPlaceholders ->
                    manifestPlaceholders.putAll(this.manifestPlaceholders)
                }
                variant.generateBuildConfigProvider.get().run {
                    println("GradleVersion:${project.gradle.gradleVersion}")
                    val srcOutputDir = if (Version(project.gradle.gradleVersion).lessThan(Version("6.7.1"))) {
                        sourceOutputDir.absolutePath
                    } else {
                        "${project.buildDir.absolutePath}/generated/source/buildConfig/${variant.dirName}"
                    }
                    println("SrcOutputDir:${srcOutputDir}")
                    envGenerators.add(
                        GeneratedEnvConfig(
                            srcOutputDir,
                            buildConfigPackageName.get(),
                            initEnvironment,
                            online,
                            variant.versionName ?: "",
                            innerConstants,
                            allVariables,
                            releaseExt,
                            devExt,
                            testExt,
                            demoExt
                        )
                    )
                    doLast {
                        envGenerators.forEach { it.generate() }

                        println("Generated Constants:")
                        innerConstants.forEach {
                            println("${it.key} : ${it.value}")
                        }

                        println("\nBaseVariables:")
                        innerVariables.forEach {
                            println("${it.key} : ${it.value}")
                        }
                        println("\nPackageVariables:")
                        config.variables.forEach {
                            println("${it.key} : ${it.value}")
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
