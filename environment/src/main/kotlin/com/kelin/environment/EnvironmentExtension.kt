package com.kelin.environment

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

    fun createVariable(name: String, value: String) {
        createVariable(name, value, false)
    }

    fun createVariable(name: String, value: String, placeholder: Boolean) {
        variables[name] = Variable(value, placeholder)
    }

    internal fun mergeVariables(vs: HashMap<String, Variable>) {
        if (variables.isNotEmpty()) {
            vs.forEach {
                if (variables.containsKey(it.key)) {
                    variables[it.key]!!.placeholder = it.value.placeholder
                } else {
                    variables[it.key] = it.value
                }
            }
        }
    }
}