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
interface VariableExtension {

    fun variable(name: String, value: String) {
        variable(name, value, "String", false)
    }

    fun variable(name: String, value: String, placeholder: Boolean) {
        variable(name, value, "String", placeholder)
    }

    fun variable(name: String, value: String, type: String) {
        variable(name, value, type, false)
    }

    fun variable(name: String, value: String, type: String, placeholder: Boolean) {
        val fixedType = when {
            "Int".equals(type, true) -> Int::class.java
            "Boolean".equals(type, true) -> Boolean::class.java
            else -> String::class.java
        }
        variable(name, Variable(value, placeholder, fixedType))
    }

    fun variable(name: String, variable: Variable)
}