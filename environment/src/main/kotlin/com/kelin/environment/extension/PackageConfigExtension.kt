package com.kelin.environment.extension

/**
 * **描述:** 用来配置包信息的Extension.
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2019-09-20  22:25
 *
 * **版本:** v 1.0.0
 */
open class PackageConfigExtension {
    internal var appIcon = ""
    internal var appRoundIcon = ""
    internal var appName = ""
    internal var versionCode = -1
    internal var versionName = ""
    internal var applicationId = ""

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
}