package com.kelin.environment

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.core.GradleVariantConfiguration
import com.android.build.gradle.options.StringOption
import com.google.common.collect.ImmutableMap
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import kotlin.reflect.full.IllegalCallableAccessException

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

    var release = false
    var placeholderEnvironment = ""

    var devVersionCode = -1
    var devVersionName = ""
    var releaseVersionCode = -1
    var releaseVersionName = ""


    val versionCode: Int
        get() {
            val vc = if (release) {
                releaseVersionCode
            } else {
                devVersionCode
            }
            if (vc != -1) {
                return vc
            } else {
                throw RuntimeException("you need set the devVersionCode's value and the releaseVersionName's value.")
            }
        }

    val versionName: String
        get(){
            val vn = if (release) {
                releaseVersionName
            } else {
                devVersionName
            }
            if (vn.isNotEmpty()) {
                return vn
            } else {
                throw RuntimeException("you need set the devVersionName's value and the releaseVersionCode's value.")
            }
        }

    private val envGenerators = ArrayList<GeneratedEnvConfig>()
    private lateinit var releaseExt: EnvironmentExtension
    private lateinit var devExt: EnvironmentExtension
    private lateinit var testExt: EnvironmentExtension
    private lateinit var demoExt: EnvironmentExtension

    @TaskAction
    fun publicEnvironment() {


        project.configurations.forEach {
            println(">>>>>>>>>>>>>>>>>-+-: ${it.name}:$it")
            if (it is GradleVariantConfiguration) {
                println("找到了太棒了————————————————————————————————————: ${it.name}:$it")
            }
        }
        val properties = project.extensions.extraProperties.properties
        properties["android.injected.version.code"] = 100
        properties["android.injected.version.name"] = "1.0.0"
        for ((key, value) in properties) {
            println(">>>>>>>>>>>>>>>>>+$key:$value")
        }
        project.gradle.buildFinished { configureEnvironment() }
        releaseExt = project.extensions.findByName("releaseEnv") as EnvironmentExtension
        devExt = project.extensions.findByName("devEnv") as EnvironmentExtension
        testExt = project.extensions.findByName("testEnv") as EnvironmentExtension
        demoExt = project.extensions.findByName("demoEnv") as EnvironmentExtension
        if (release) {
            placeholderEnvironment = "release"
        }
        if (releaseExt.variables.isEmpty()) {
            throw IllegalArgumentException("you must have release environment, you need called the releaseEnv method!")
        } else {
            if (!release) {
                devExt.mergeVariables(releaseExt.variables)
                testExt.mergeVariables(releaseExt.variables)
                demoExt.mergeVariables(releaseExt.variables)
            }
        }
    }

    private fun configureEnvironment() {

        val app = project.extensions.findByType<AppExtension>(AppExtension::class.java)?.applicationVariants
        app?.all { variant ->
            println("\n========Placeholder Environment-${variant.name}:")
            when (placeholderEnvironment) {
                "release" -> {
                    releaseExt.variables
                }
                "dev" -> {
                    devExt.variables
                }
                "test" -> {
                    testExt.variables
                }
                "demo" -> {
                    demoExt.variables
                }
                else -> {
                    releaseExt.variables
                }
            }.forEach {
                if (it.value.placeholder) {
                    println("========${variant.name}: ${it.key} | ${it.value.value}")
                    variant.mergedFlavor.manifestPlaceholders[it.key] = it.value.value
                }
            }
            println("\n\n")

            val buildConfig = variant.generateBuildConfigProvider.get()
            generatedEnvConfig(
                buildConfig.sourceOutputDir.absolutePath,
                buildConfig.buildConfigPackageName,
                placeholderEnvironment,
                release,
                variant.versionName ?: "",
                releaseExt.variables,
                devExt.variables,
                testExt.variables,
                demoExt.variables
            )
        }
        envGenerators.forEach { it.generate() }
    }


    private fun generatedEnvConfig(
        filePath: String,
        packageName: String,
        environment: String,
        isRelease: Boolean,
        version: String,
        release: HashMap<String, Variable>,
        dev: HashMap<String, Variable>,
        test: HashMap<String, Variable>,
        demo: HashMap<String, Variable>
    ) {
        envGenerators.add(
            GeneratedEnvConfig(
                filePath,
                packageName,
                environment,
                isRelease,
                version,
                release,
                dev,
                test,
                demo
            )
        )
    }
}
