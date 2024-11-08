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
    internal var appIcon = ""

    @get:Input
    internal var appRoundIcon = ""

    @get:Input
    internal var appName = ""

    @get:Input
    internal var versionCode = -1

    @get:Input
    internal var versionName = ""

    @get:Input
    internal var applicationId = ""

    @get:Input
    internal val variables = HashMap<String, EnvValue>()

    fun appIcon(appIcon: String) {
        this.appIcon = appIcon
    }

    fun appRoundIcon(appRoundIcon: String) {
        this.appRoundIcon = appRoundIcon
    }

    fun appName(appName: String) {
        this.appName = appName
    }

    fun versionCode(versionCode: Int) {
        this.versionCode = versionCode
    }

    fun versionName(versionName: String) {
        this.versionName = versionName
    }

    fun applicationId(applicationId: String) {
        this.applicationId = applicationId
    }

    override fun variable(name: String, value: EnvValue) {
        variables[name] = value
    }

    fun getVariable(name: String): String? {
        return variables[name]?.value
    }
}