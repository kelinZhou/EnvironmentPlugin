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

        project.extensions.create("devConfig", PackageConfigExtension::class.java)
        project.extensions.create("releaseConfig", PackageConfigExtension::class.java)

        project.extensions.create("releaseEnv", EnvironmentExtension::class.java)
        project.extensions.create("devEnv", EnvironmentExtension::class.java)
        project.extensions.create("testEnv", EnvironmentExtension::class.java)
        project.extensions.create("demoEnv", EnvironmentExtension::class.java)

        val envTask = project.tasks.create("environment", EnvironmentTask::class.java)
//        project.tasks.find {
//            println("TaskName:${it.name}") //打印所有task的名字
//            return@find false
//        }
        project.tasks.findByName("preBuild")?.dependsOn(envTask)//每当cleanTask执行之后就执行环境配置的Task
    }
}
