package com.kelin.environment

/**
 * **描述:** 定义一个Extension可以设置变量。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2021/1/24 9:47 PM
 *
 * **版本:** v 1.0.0
 */
interface ImmutableExtension {

    fun constant(name: String, value: String) {
        constant(name, value, "String", false)
    }

    fun constant(name: String, value: String, placeholder: Boolean) {
        constant(name, value, "String", placeholder)
    }

    fun constant(name: String, value: String, type: String) {
        constant(name, value, type, false)
    }

    fun constant(name: String, value: String, type: String, placeholder: Boolean) {
        val fixedType = when {
            "Int".equals(type, true) -> Int::class.java
            "Boolean".equals(type, true) -> Boolean::class.java
            "Double".equals(type, true) -> Double::class.java
            else -> String::class.java
        }
        constant(name, EnvValue(value, placeholder, fixedType))
    }

    fun constant(name: String, value: EnvValue)
}