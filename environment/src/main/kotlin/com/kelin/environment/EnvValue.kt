package com.kelin.environment

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * **描述:** 环境变量。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2019-09-09  10:09
 *
 * **版本:** v 1.0.0
 */
data class EnvValue(val value: Any, var placeholder: Boolean, private val type: KType) : Serializable {

    val typeValue: String by lazy {
        if (type.classifier == String::class) {
            return@lazy "\"${value}\""
        } else {
            value.toString()
        }
    }

    val typeName: TypeName = type.asTypeName()

    override fun toString(): String {
        return "{ value: ${value}, placeholder: ${placeholder}, type: ${type.simpleName} }"
    }
}

private val KType.simpleName: String
    get() = when (val classifier = classifier) {
        is KClass<*> -> classifier.simpleName ?: classifier.qualifiedName ?: "Unknown"
        else -> classifier.toString()
    }