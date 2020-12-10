package com.kelin.environment.extension

import com.kelin.environment.Variable
import java.lang.reflect.Type

/**
 * **描述:** 环境配置的基类。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2019-09-09  09:45
 *
 * **版本:** v 1.0.0
 */
open class EnvironmentExtension {

    val variables = HashMap<String, Variable>()

    var alias = ""

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
        variables[name] = Variable(value, placeholder, fixedType)
    }

    internal fun getEnvironmentArgs(reference: EnvironmentExtension? = null): String {
        return reference?.variables?.let { v ->
            v.entries.joinToString(", ") { entry ->
                variables[entry.key]?.let { getValueByType(it.value, entry.value.type) }
                    ?: entry.value.let { getValueByType(it.value, it.type) }
            }
        } ?: variables.values.joinToString(", ") { getValueByType(it.value, it.type) }
    }

    private fun getValueByType(value: String, type: Type): String {
        return when (String::class.java.typeName) {
            type.typeName -> {
                "\"${value}\""
            }
            else -> {
                value
            }
        }
    }

    fun createManifestPlaceholders(placeholders: MutableMap<String, Any>, reference: EnvironmentExtension? = null) {
        if (reference != null) {
            reference.variables.forEach { v ->
                (variables[v.key] ?: v.value).run {
                    if (v.value.placeholder) {
                        placeholders[v.key] = value
                    }
                }
            }
        } else {
            variables.forEach { v ->
                if (v.value.placeholder) {
                    placeholders[v.key] = v.value.value
                }
            }
        }
    }
}