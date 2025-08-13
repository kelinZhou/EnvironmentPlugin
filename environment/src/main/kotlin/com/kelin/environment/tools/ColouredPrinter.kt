package com.kelin.environment.tools

/**
 * **描述:** 带颜色的打印。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2025/8/13 15:21
 *
 * **版本:** v 1.0.0
 */
private object AnsiColor {
    const val RESET = "\u001B[0m"
    const val RED = "\u001B[31m"
    const val GREEN = "\u001B[32m"
    const val YELLOW = "\u001B[33m"
    const val BLUE = "\u001B[34m"
    const val PURPLE = "\u001B[35m"
    const val CYAN = "\u001B[36m"
    const val WHITE = "\u001B[37m"
}

internal fun String.red() = "${AnsiColor.RED}$this${AnsiColor.RESET}"
internal fun String.green() = "${AnsiColor.GREEN}$this${AnsiColor.RESET}"
internal fun String.yellow() = "${AnsiColor.YELLOW}$this${AnsiColor.RESET}"
internal fun String.blue() = "${AnsiColor.BLUE}$this${AnsiColor.RESET}"
internal fun String.purple() = "${AnsiColor.PURPLE}$this${AnsiColor.RESET}"
internal fun String.cyan() = "${AnsiColor.CYAN}$this${AnsiColor.RESET}"
internal fun String.white() = "${AnsiColor.WHITE}$this${AnsiColor.RESET}"
