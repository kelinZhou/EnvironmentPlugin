package com.kelin.environment

import kotlin.reflect.full.createType

/**
 * **描述:** 定义一个Extension可以设置常量。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2021/1/24 9:47 PM
 *
 * **版本:** v 1.0.0
 */
interface ImmutableExtension {

    fun <T : Number> constant(name: String, value: T, placeholder: Boolean = false) {
        constant(name, EnvValue(value, placeholder, value.javaClass.kotlin.createType()))
    }

    fun constant(name: String, value: String, placeholder: Boolean = false) {
        constant(name, EnvValue(value, placeholder, String::class.createType()))
    }

    fun constant(name: String, value: Boolean, placeholder: Boolean = false) {
        constant(name, EnvValue(value, placeholder, Boolean::class.createType()))
    }

    fun constant(name: String, value: EnvValue)
}