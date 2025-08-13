package com.kelin.environment.extension

import com.kelin.environment.EnvValue
import com.kelin.environment.VariableExtension
import com.kelin.environment.tools.blue
import com.kelin.environment.tools.green
import com.kelin.environment.tools.yellow
import org.gradle.api.tasks.Input

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

    internal val variables = HashMap<String, EnvValue>()

    override fun variable(name: String, value: EnvValue) {
        variables[name] = value
    }

    internal fun getEnvironmentArgs(envName: String, allVariables: Map<String, EnvValue>): String {
        println("$envName Environment:".yellow())
        return allVariables.entries.joinToString(", ") { (name, value) ->
            val variable = variables[name] ?: value
            println("${name.blue()} : manifestEnable = ${variable.placeholder} -> \n${variable.toString().green()}\n")
            variable.typeValue
        }
    }

    fun createManifestPlaceholders(
        placeholders: MutableMap<String, Any>,
        allVariables: Map<String, EnvValue>
    ) {
        allVariables.entries.forEach { entry ->
            if (entry.value.placeholder) {
                placeholders[entry.key] = (variables[entry.key] ?: entry.value).value
            }
        }
    }
}