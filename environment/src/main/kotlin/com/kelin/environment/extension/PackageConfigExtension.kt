package com.kelin.environment.extension

import com.kelin.environment.EnvValue
import com.kelin.environment.VariableExtension
import org.gradle.api.tasks.Input
import java.util.HashMap

/**
 * **描述:** 用来配置包信息的Extension.
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2019-09-20  22:25
 *
 * **版本:** v 1.0.0
 */
open class PackageConfigExtension(val name: String) : VariableExtension {
    @get:Input
    var appIcon = ""

    @get:Input
    var appRoundIcon = ""

    @get:Input
    var appName = ""

    @get:Input
    var versionCode = -1

    @get:Input
    var versionName = ""

    @get:Input
    var applicationId = ""

    @get:Input
    internal val variables = HashMap<String, EnvValue>()


    override fun variable(name: String, value: EnvValue) {
        variables[name] = value
    }

    fun getVariable(name: String): String? {
        return variables[name]?.value
    }
}