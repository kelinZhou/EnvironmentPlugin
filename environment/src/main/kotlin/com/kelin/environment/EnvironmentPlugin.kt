package com.kelin.environment

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.options.StringOption
import com.google.common.collect.ImmutableMap
import com.squareup.javapoet.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.lang.IllegalArgumentException
import javax.lang.model.element.Modifier


/**
 * **描述:** 环境配置插件。
 *
 *
 * **创建人:** kelin
 *
 *
 * **创建时间:** 2019-09-06  11:54
 *
 *
 * **版本:** v 1.0.0
 */
class EnvironmentPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val envTask = project.tasks.create("environment", EnvironmentTask::class.java) {
            it.release = true
            it.placeholderEnvironment = "release"
            it.devVersionCode = -1
            it.devVersionName = ""
            it.releaseVersionCode = -1
            it.releaseVersionName = ""
        }

        project.tasks.findByName("preBuild")?.dependsOn(envTask)

        project.extensions.create("releaseEnv", EnvironmentExtension::class.java)
        project.extensions.create("devEnv", EnvironmentExtension::class.java)
        project.extensions.create("testEnv", EnvironmentExtension::class.java)
        project.extensions.create("demoEnv", EnvironmentExtension::class.java)
    }
}
