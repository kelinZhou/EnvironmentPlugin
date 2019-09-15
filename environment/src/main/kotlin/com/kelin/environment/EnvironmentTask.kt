package com.kelin.environment

import com.android.build.gradle.AppExtension
import com.android.builder.model.ClassField
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.util.regex.Pattern

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
    var initEnvironment = ""

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
        get() {
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

    private fun getCurrentVariant(): Array<String> {
        val tskReqStr = project.gradle.startParameter.taskRequests.toString()
        val pattern = if (tskReqStr.contains("assemble"))
            Pattern.compile("assemble(\\w+)(Release|Debug)")
        else
            Pattern.compile("generate(\\w+)(Release|Debug)")

        val matcher = pattern.matcher(tskReqStr)

        return if (matcher.find()) {
            arrayOf(matcher.group(1), matcher.group(2))
        } else {
            arrayOf("", if (tskReqStr.contains("release")) "release" else "debug")
        }
    }

    @TaskAction
    fun publicEnvironment() {

        project.gradle.buildFinished { envGenerators.forEach { it.generate() } }
        releaseExt = project.extensions.findByName("releaseEnv") as EnvironmentExtension
        devExt = project.extensions.findByName("devEnv") as EnvironmentExtension
        testExt = project.extensions.findByName("testEnv") as EnvironmentExtension
        demoExt = project.extensions.findByName("demoEnv") as EnvironmentExtension
        if (releaseExt.alias.isEmpty()) {
            releaseExt.alias = "Release"
        }
        if (release) {
            initEnvironment = "release"
        }
        if (releaseExt.variables.isEmpty()) {
            throw IllegalArgumentException("you must have release environment, you need called the releaseEnv method!")
        } else {
            if (!release) {
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

            app?.all { variant ->
                println("${"$channel$type".toLowerCase()}<<<<<<<>>>>>${variant.name.toLowerCase()}")
                if ("$channel$type".toLowerCase() == variant.name.toLowerCase()) {
                    println("\nGenerate placeholder for ${variant.name} variant:\n")
                    when (initEnvironment) {
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
                            println("${it.key} | ${it.value.value}")
                            variant.mergedFlavor.manifestPlaceholders[it.key] = it.value.value
                        }
                    }
                    println("\n\n")

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
                                return "Boolean.parseBoolean(\"${if (release) "false" else "true"}\")"
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
                            release,
                            variant.versionName ?: "",
                            releaseExt,
                            devExt,
                            testExt,
                            demoExt
                        )
                    )
                }
            }
        }
    }
}
