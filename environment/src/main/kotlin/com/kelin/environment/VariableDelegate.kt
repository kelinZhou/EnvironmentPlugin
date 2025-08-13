package com.kelin.environment

import com.kelin.environment.extension.TaskExtension
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * **描述:** 获取属性时的代理。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2025/8/13 17:23
 *
 * **版本:** v 1.0.0
 */
class VariableDelegate<T>(
    /**
     * TaskExtension实例。
     */
    private val environment: TaskExtension,
    /**
     * 变量名称。
     */
    private val variableName: String,
    /**
     * 是否为获取变量，true表示获取变量，false表示获取常量。
     */
    private val isMutable: Boolean
) : ReadOnlyProperty<Any?, T>{
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return if (isMutable) {
            environment.getVariable(variableName)
        } else {
            environment.getConstant(variableName)
        }
    }
}

class VariableDelegateProvider<T>(
    /**
     * TaskExtension实例。
     */
    private val environment: TaskExtension,
    /**
     * 是否为获取变量，true表示获取变量，false表示获取常量。
     */
    private val isMutable: Boolean
){
    operator fun provideDelegate(
        thisRef: Any?,
        property: KProperty<*>
    ): ReadOnlyProperty<Any?, T>{
        return VariableDelegate(environment, property.name, isMutable)
    }
}