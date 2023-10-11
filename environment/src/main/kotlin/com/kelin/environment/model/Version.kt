package com.kelin.environment.model

data class Version(val version: String) {
    val versionArray = version.split(".")
}

infix fun Version.lessThan(other: Version): Boolean {
    var lessThen = false
    for (i in versionArray.indices) {
        val v = versionArray[i].toPositiveNumber()
        val otherV = other.versionArray[i].toPositiveNumber()
        if (v < 0 && otherV < 0) {
            break
        } else if (v < otherV) {
            lessThen = true
            break
        }
    }
    return lessThen
}

private fun String.toPositiveNumber(): Int {
    return try {
        toInt()
    } catch (e: Exception) {
        -1
    }
}