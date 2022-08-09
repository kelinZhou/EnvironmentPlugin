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

    internal fun getEnvironmentArgs(envName: String, allVariables: Map<String, Variable>): String {
        println("$envName Environment:")
        return allVariables.entries.joinToString(", ") { entry ->
            val variable = variables[entry.key] ?: entry.value
            println("${entry.key} | $variable  (${variable.placeholder})")
            getValueByType(variable.value, entry.value.type)
        }
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
        allVariables: Map<String, Variable>
    ) {
        allVariables.entries.forEach { entry ->
            if (entry.value.placeholder) {
                placeholders[entry.key] = (variables[entry.key] ?: entry.value).value
            }
        }
    }
}