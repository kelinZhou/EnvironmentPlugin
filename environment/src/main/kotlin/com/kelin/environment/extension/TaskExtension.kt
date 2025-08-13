package com.kelin.environment.extension

import com.kelin.environment.EnvType
import com.kelin.environment.EnvValue
import com.kelin.environment.ImmutableExtension
import com.kelin.environment.VariableDelegateProvider
import com.kelin.environment.VariableExtension
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * **描述:** 任务的基础拓展。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2025/8/12 10:55
 *
 * **版本:** v 1.0.0
 */
open class TaskExtension(private val project: Project): VariableExtension, ImmutableExtension {

    internal val innerVariables = HashMap<String, EnvValue>()
    internal val innerConstants = HashMap<String, EnvValue>()

    private val releaseExt by lazy { project.extensions.findByName("releaseEnv") as EnvironmentExtension }
    private val devExt by lazy { project.extensions.findByName("devEnv") as EnvironmentExtension }
    private val testExt by lazy { project.extensions.findByName("testEnv") as EnvironmentExtension }
    private val demoExt by lazy { project.extensions.findByName("demoEnv") as EnvironmentExtension }

    /**
     * 配置EnvConfig文件的包名，适配多包名场景，在多包名项目中指定该参数可避免不同包名时使用EnvConfig需要导不同包的尴尬问题。
     */
    @get:Input
    var envPackage: String = ""

    @get:Input
    val release = EnvType.RELEASE

    @get:Input
    val dev = EnvType.DEV

    @get:Input
    val test = EnvType.TEST

    @get:Input
    val demo = EnvType.DEMO

    @get:Input
    var online = false

    @get:Input
    var initEnvironment = release

    @get:Input
    var enabledConfig: String = ""

    private val configs: NamedDomainObjectContainer<PackageConfigExtension> = project.container(PackageConfigExtension::class.java)

    internal val curConfig: PackageConfigExtension by lazy {
        configs.find {
            if (enabledConfig.isBlank()) {
                if (online) {
                    it.name.endsWith("Release", true)
                } else {
                    it.name.endsWith("Dev", true)
                }
            } else {
                it.name.equals("${enabledConfig}${if (online) "Release" else "Dev"}", true)
            }
        } ?: throw NullPointerException("The config of ${if (online) "Release" else "Dev"} must not be null! Please use 'dev' or 'release' with 'configs'.")
    }

    internal val allVariables: Map<String, EnvValue>
        get() {
            return LinkedHashMap<String, EnvValue>().apply {
                putAll(innerVariables)
                putAll(curConfig.variables)
                putAll(currentEnvVariables)
            }
        }

    private val currentEnvVariables: HashMap<String, EnvValue>
        get() = LinkedHashMap(releaseExt.variables).apply {
            if (online) {
                releaseExt
            } else {
                when (initEnvironment) {
                    EnvType.RELEASE -> releaseExt
                    EnvType.DEV -> devExt
                    EnvType.TEST -> testExt
                    EnvType.DEMO -> demoExt
                }
            }.variables.forEach {
                put(it.key, it.value)
            }
        }

    internal fun fixPlaceholder() {
        ArrayList(innerVariables.entries).apply {
            addAll(curConfig.variables.entries)
            addAll(currentEnvVariables.entries)
        }.groupBy { it.key }.forEach { group ->
            if (group.value.size > 1) {
                group.value.forEach { v ->
                    v.value.placeholder = group.value.any { entry -> entry.value.placeholder }
                }
            }
        }
    }

    @get:Input
    val manifestPlaceholders: MutableMap<String, Any>
        get() = LinkedHashMap<String, Any>().apply {
            if (curConfig.appIcon.isNotEmpty()) {
                this["APP_ICON"] = curConfig.appIcon
            }
            if (curConfig.appRoundIcon.isNotEmpty()) {
                this["APP_ROUND_ICON"] =
                    curConfig.appRoundIcon
            }
            if (curConfig.appName.isNotEmpty()) {
                this["APP_NAME"] = curConfig.appName
            }
            innerConstants.forEach {
                if (it.value.placeholder) {
                    this[it.key.uppercase(Locale.US)] = it.value.value
                }
            }
            when (initEnvironment) {
                EnvType.RELEASE -> releaseExt
                EnvType.DEV -> devExt
                EnvType.TEST -> testExt
                EnvType.DEMO -> demoExt
            }.createManifestPlaceholders(this, allVariables)
        }
    /**
     * 声明环境配置的方法。
     */
    fun configs(action: Action<NamedDomainObjectContainer<PackageConfigExtension>>) {
        action.execute(configs)
    }

    @get:Input
    val appName: String
        get() = curConfig.appName

    @get:Input
    val appIcon: String
        get() = curConfig.appIcon

    @get:Input
    val appRoundIcon: String
        get() = curConfig.appRoundIcon

    @get:Input
    val versionCode: Int
        get() {
            return when {
                curConfig.versionCode != -1 -> return curConfig.versionCode
                else -> Date().let { SimpleDateFormat("yyMMddHH", Locale.CHINA).format(it).toInt() }
            }
        }

    @get:Input
    val versionName: String
        get() {
            return when {
                curConfig.versionName.isNotEmpty() -> curConfig.versionName
                curConfig.versionCode != -1 -> {
                    val codeStr = curConfig.versionCode.toString()
                    if (codeStr.length > 3) {
                        throw RuntimeException("versionCode:${curConfig.versionCode} does not support.Your versionCode’s length must be <= 3, such as 100.")
                    } else {
                        when (codeStr.length) {
                            1 -> "0.0.$codeStr"
                            2 -> "0.${codeStr.toCharArray().joinToString(".")}"
                            3 -> codeStr.toCharArray().joinToString(".")
                            else -> throw RuntimeException("versionCode:${curConfig.versionCode} does not support.Your versionCode’s length must be <= 3, such as 100.")
                        }
                    }
                }

                else -> throw RuntimeException("You need set the versionName's value for ${if (online) "releaseConfig" else "devConfig"}.")
            }
        }

    @get:Input
    val applicationId: String
        get() = curConfig.applicationId

    override fun constant(name: String, value: EnvValue) {
        innerConstants[name] = value
    }

    override fun variable(name: String, value: EnvValue) {
        innerVariables[name] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getVariable(name: String): T {
        return ((innerVariables[name]?.value as? T) ?: curConfig.getVariable(name)
        ?: currentEnvVariables[name]?.value as? T)?: throw NullPointerException("Variable not found by name: $name in Environment.")
    }

    /**
     * 提供通过委托的方式获取变量。
     */
    fun<T> getVariable(): VariableDelegateProvider<T>{
        return VariableDelegateProvider(this, true)
    }

    /**
     * 判断一个变量是否为某个值。
     * @param name 变量的名字。
     * @param value 用于判断指定名字的变量的值是否为该值。
     */
    fun<T> isVariable(name: String, value: T): Boolean {
        return getVariable<T>(name) == value
    }

    @Suppress("UNCHECKED_CAST")
    fun<T> getConstant(name: String): T {
        return (innerConstants[name]?.value as? T)?: throw NullPointerException("Constant not found by name: $name in Environment.")
    }

    /**
     * 提供通过委托的方式获取常量。
     */
    fun<T> getConstant(): VariableDelegateProvider<T>{
        return VariableDelegateProvider(this, false)
    }

    /**
     * 判断一个常量是否为某个值。
     * @param name 常量的名字。
     * @param value 用于判断指定名字的常量的值是否为该值。
     */
    fun<T> isConstant(name: String, value: T): Boolean {
        return getConstant<T>(name) == value
    }
}