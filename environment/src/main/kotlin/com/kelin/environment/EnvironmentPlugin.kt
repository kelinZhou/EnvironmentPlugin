package com.kelin.environment

import com.kelin.environment.extension.EnvironmentExtension
import com.kelin.environment.extension.PackageConfigExtension
import org.gradle.api.Plugin
import org.gradle.api.Project


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
            it.initEnvironment = "release"
        }
        project.tasks.findByName("preBuild")?.dependsOn(envTask)

        project.extensions.create("devConfig", PackageConfigExtension::class.java)
        project.extensions.create("releaseConfig", PackageConfigExtension::class.java)

        project.extensions.create("releaseEnv", EnvironmentExtension::class.java)
        project.extensions.create("devEnv", EnvironmentExtension::class.java)
        project.extensions.create("testEnv", EnvironmentExtension::class.java)
        project.extensions.create("demoEnv", EnvironmentExtension::class.java)
    }
}
