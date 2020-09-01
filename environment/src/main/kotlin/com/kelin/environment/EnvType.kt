package com.kelin.environment

/**
 * **描述:** 环境类型
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2020/9/1 10:36 AM
 *
 * **版本:** v 1.0.0
 */
enum class EnvType(private val type: String) {
    DEV("dev"), TEST("test"), DEMO("demo"), RELEASE("release");

    override fun toString(): String {
        return type
    }
}