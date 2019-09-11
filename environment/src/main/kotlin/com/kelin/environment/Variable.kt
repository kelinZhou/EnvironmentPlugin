package com.kelin.environment

import java.io.Serializable

/**
 * **描述:** 环境变量。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2019-09-09  10:09
 *
 * **版本:** v 1.0.0
 */
data class Variable(val value: String, var placeholder: Boolean) : Serializable