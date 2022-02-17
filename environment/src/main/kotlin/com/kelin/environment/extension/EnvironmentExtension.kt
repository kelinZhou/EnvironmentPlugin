package com.kelin.environment.extension

import com.kelin.environment.Variable
import com.kelin.environment.VariableExtension
import org.gradle.api.tasks.Input
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
open class EnvironmentExtension : VariableExtension {

    @get:Input
    var alias = ""

    val variables = HashMap<String, Variable>()

    override fun variable(name: String, variable: Variable) {
        variables[name] = variable
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

    fun createManifestPlaceholders(
        placeholders: MutableMap<String, Any>,
        reference: EnvironmentExtension? = null
    ) {
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