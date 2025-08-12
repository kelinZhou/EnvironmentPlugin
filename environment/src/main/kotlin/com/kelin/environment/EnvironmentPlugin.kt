package com.kelin.environment

import com.kelin.environment.extension.EnvironmentExtension
import com.kelin.environment.extension.TaskExtension
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
        val extension = project.extensions.create("environment", TaskExtension::class.java, project)
        val envTask = project.tasks.create("environmentTask", EnvironmentTask::class.java) {
            extension.online = true
            extension.initEnvironment = EnvType.RELEASE
        }
        project.tasks.findByName("preBuild")?.dependsOn(envTask)//每当cleanTask执行之后就执行环境配置的Task

        project.extensions.create("releaseEnv", EnvironmentExtension::class.java)
        project.extensions.create("devEnv", EnvironmentExtension::class.java)
        project.extensions.create("testEnv", EnvironmentExtension::class.java)
        project.extensions.create("demoEnv", EnvironmentExtension::class.java)
    }
}

fun Project.environment(configure: TaskExtension.() -> Unit) {
    val extension = extensions.getByType(TaskExtension::class.java)
    extension.configure()
}