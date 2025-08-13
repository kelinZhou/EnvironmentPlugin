package com.kelin.environment

import com.kelin.environment.extension.EnvironmentExtension
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.io.File
import java.util.*


/**
 * **描述:** 制造环境的配置
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2019-09-10  16:29
 *
 * **版本:** v 1.0.0
 */
class EnvConfigGenerator(
    private val filePath: String,
    private val packageName: String,
    private val environment: EnvType,
    private val isRelease: Boolean,
    private val version: String,
    private val flavor: String,
    private val constants: Map<String, EnvValue>,
    private val allVariables: Map<String, EnvValue>,
    private val release: EnvironmentExtension,
    private val dev: EnvironmentExtension,
    private val test: EnvironmentExtension,
    private val demo: EnvironmentExtension,
) {

    internal fun generate() {
        val parameters = writeEnvironmentInterface(allVariables, packageName, filePath)
        val contextType = ClassName("android.content", "Context")
        val applicationType = ClassName("android.app", "Application")
        // EnvConfig核心类。
        val envConfig = TypeSpec.objectBuilder(CONFIG_NAME)
            .addKdoc("**Description:** configure the environment.(Automatically generated file. DO NOT MODIFY)。\n<p>\n**Version:** v $version\n")
        //环境类型的className
        val envTypeName = ClassName(packageName, "EnvType")
        //环境变量的className
        val environmentClassName = ClassName(packageName, ENVIRONMENT_NAME)
        //Environment的实现类
        val environmentImpl = TypeSpec.classBuilder("${ENVIRONMENT_NAME}Impl")
            .addKdoc("**Description:** The default implementation of Environment.(Automatically generated file. DO NOT MODIFY)。\n<p>\n**Version:** v $version\n")
            .addModifiers(KModifier.PRIVATE, KModifier.DATA)
            .addSuperinterface(environmentClassName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(
                        parameters.map { (name, type) ->
                            ParameterSpec.builder(name, type).build()
                        }
                    )
                    .build()
            )
            .addProperties(
                parameters.map { (name, type) ->
                    PropertySpec.builder(name, type, KModifier.OVERRIDE)
                        .initializer(name)
                        .build()
                }
            )
        with(envConfig) {
            if (!isRelease) {
                addProperty(
                    PropertySpec.builder("context", contextType.copy(true), KModifier.PRIVATE)
                        .mutable()
                        .initializer("null")
                        .build()
                )
            }

            //添加基本常量
            addProperties(
                listOf(
                    //FLAVOR字段
                    PropertySpec.builder("FLAVOR", String::class)
                        .initializer("\"$flavor\"")
                        .build(),
                    //用于区分是否是发布环境的字段
                    PropertySpec.builder("IS_RELEASE", Boolean::class)
                        .initializer("%S.toBoolean()", isRelease)
                        .build(),
                    //用于区分是否是Debug模式的字段
                    PropertySpec.builder("IS_DEBUG", Boolean::class)
                        .initializer("%S.toBoolean()", !isRelease)
                        .build(),
                    //获取初始化环境的字段。
                    PropertySpec.builder("INIT_ENV", envTypeName, KModifier.PRIVATE)
                        .initializer("EnvType.nameOf(\"$environment\")")
                        .build()
                )
            )
            //添加常量配置
            addProperties(
                constants.map { (name, value) ->
                    PropertySpec.builder(name.uppercase(Locale.US), value.typeName)
                        .initializer(value.typeValue)
                        .build()
                }
            )

            //添加生产环境。
            addProperty(
                PropertySpec.builder("RELEASE_ENV", environmentClassName, KModifier.PRIVATE)
                    .initializer("EnvironmentImpl(${release.getEnvironmentArgs("Release", allVariables)})")
                    .build(),
            )
            //除了生产环境外，其他环境需要根据情况而定。
            if (!isRelease) {
                //Dev环境
                if (dev.variables.isNotEmpty()) {
                    addProperty(
                        PropertySpec.builder("DEV_ENV", environmentClassName, KModifier.PRIVATE)
                            .initializer("EnvironmentImpl(${dev.getEnvironmentArgs("Dev", allVariables)})")
                            .build(),
                    )
                }
                //Test环境
                if (test.variables.isNotEmpty()) {
                    addProperty(
                        PropertySpec.builder("TEST_ENV", environmentClassName, KModifier.PRIVATE)
                            .initializer("EnvironmentImpl(${test.getEnvironmentArgs("Test", allVariables)})")
                            .build(),
                    )
                }
                //Demo环境
                if (demo.variables.isNotEmpty()) {
                    addProperty(
                        PropertySpec.builder("DEMO_ENV", environmentClassName, KModifier.PRIVATE)
                            .initializer("EnvironmentImpl(${demo.getEnvironmentArgs("Demo", allVariables)})")
                            .build(),
                    )
                }
            }

            addProperty(
                PropertySpec.builder("envType", envTypeName, KModifier.PRIVATE)
                    .mutable()
                    .setter(
                        FunSpec.setterBuilder().addModifiers(KModifier.PRIVATE).build()
                    )
                    .initializer("EnvType.RELEASE")
                    .build()
            )

            //添加init方法
            addFunction(
                FunSpec.builder("initial")
                    .addParameter("app", applicationType)
                    .apply {
                        if (!isRelease) {
                            addStatement(
                                getInitMethodCode(),
                                ClassName("android.preference", "PreferenceManager")
                            )
                        }
                    }
                    .build()
            )

            //添加envType的设置的函数
            addFunction(
                FunSpec.builder("switchEnv")
                    .addParameter("type", envTypeName.copy(nullable = true))
                    .apply {
                        if (isRelease) {
                            addStatement("return false")
                        } else {
                            beginControlFlow("return if(type != null)")
                                .addStatement("envType = type")
                                .beginControlFlow("context?.apply")
                                .addStatement(
                                    """
                                        getSharedPreferences("app_env_config", Context.MODE_PRIVATE).edit().putString("current_environment_type_string_name", type.name).apply()
                                    """.trimIndent()
                                )
                                .endControlFlow()
                                .addStatement("true")
                                .endControlFlow()
                                .beginControlFlow("else")
                                .addStatement("false")
                                .endControlFlow()
                        }
                    }
                    .returns(Boolean::class)
                    .build()
            )
            //添加获取当前环境配置的代码
            addProperty(
                PropertySpec.builder("environment", environmentClassName)
                    .getter(
                        FunSpec.getterBuilder()
                            .addStatement(
                                if (isRelease) {
                                    "return RELEASE_ENV"
                                } else {
                                    val sb = StringBuilder()
                                    sb.appendLine("\tEnvType.RELEASE -> RELEASE_ENV")
                                    if (dev.variables.isNotEmpty()) {
                                        sb.appendLine("\tEnvType.DEV -> DEV_ENV")
                                    }
                                    if (test.variables.isNotEmpty()) {
                                        sb.appendLine("\tEnvType.TEST -> TEST_ENV")
                                    }
                                    if (demo.variables.isNotEmpty()) {
                                        sb.appendLine("\tEnvType.DEMO -> DEMO_ENV")
                                    }
                                    "return when (envType) {\n$sb}"
                                }
                            )
                            .build()
                    )
                    .build()
            )

            //为EnvConfig实现Environment接口
            addProperties(
                allVariables.map { (name, value) ->
                    PropertySpec.builder(name, value.typeName)
                        .getter(
                            FunSpec.getterBuilder()
                                .addStatement("return environment.%N", name)
                                .build()
                        )
                        .build()
                }
            )
        }

        val enumType = TypeSpec.enumBuilder("EnvType")
            .addKdoc("**Description:** Declare the type of the environment.(Automatically generated file. DO NOT MODIFY)。\n<p>\n**Version:** v $version\n")
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("alias", String::class)
                    .build()
            ).addProperty(
                PropertySpec.builder("alias", String::class, KModifier.PUBLIC).initializer("alias").build()
            ).apply {
                addEnumConstant(
                    "RELEASE",
                    TypeSpec.anonymousClassBuilder()
                        .addSuperclassConstructorParameter("%S", release.alias)
                        .build()
                )
                if (!isRelease) {
                    if (dev.variables.isNotEmpty()) {
                        addEnumConstant(
                            "DEV",
                            TypeSpec.anonymousClassBuilder()
                                .addSuperclassConstructorParameter("%S", dev.alias)
                                .build()
                        )
                    }
                    if (test.variables.isNotEmpty()) {
                        addEnumConstant(
                            "TEST",
                            TypeSpec.anonymousClassBuilder()
                                .addSuperclassConstructorParameter("%S", test.alias)
                                .build()
                        )
                    }
                    if (demo.variables.isNotEmpty()) {
                        addEnumConstant(
                            "DEMO",
                            TypeSpec.anonymousClassBuilder()
                                .addSuperclassConstructorParameter("%S", demo.alias)
                                .build()
                        )
                    }
                }
            }
        val nameOfFun = TypeSpec.companionObjectBuilder()
            .addFunction(
                FunSpec.builder("nameOf")
                    .addParameter(
                        ParameterSpec.builder("typeName", String::class.asTypeName().copy(nullable = true))
                            .build()
                    )
                    .returns(envTypeName)
                    .addStatement(
                        if (isRelease) {
                            "return RELEASE"
                        } else {
                            """
                                return if(typeName != null){
                                    entries.find { it.name.equals(typeName, true) }?: RELEASE
                                }else{
                                    RELEASE
                                }
                            """.trimIndent()
                        }
                    )
                    .build()
            )
        enumType.addType(nameOfFun.build())

        FileSpec.builder(packageName, "${CONFIG_NAME}.kt")
            .addType(enumType.build())
            .addType(envConfig.build())
            .addType(
                environmentImpl.build()
            )
            .build()
            .writeTo(File(filePath))
    }

    private fun getInitMethodCode(): String {
        return """
            context = app.applicationContext.also{
                envType = EnvType.nameOf(it.getSharedPreferences("app_env_config", Context.MODE_PRIVATE).getString("current_environment_type_string_name", INIT_ENV.name))
            }
        """.trimIndent()
    }

    private fun writeEnvironmentInterface(
        variables: Map<String, EnvValue>,
        packageName: String,
        filePath: String,
    ): Map<String, TypeName> {
        //用有序Map保存所有参数，必须要有序的。
        val parameters = LinkedHashMap<String, TypeName>()
        val fields = ArrayList<PropertySpec>(variables.size)
        for ((key, value) in variables) {
            fields.add(
                PropertySpec.builder(key, value.typeName).build()
            )
            parameters.put(key, value.typeName)
        }

        FileSpec.builder(packageName, "${ENVIRONMENT_NAME}.kt").addType(
            TypeSpec.interfaceBuilder(ENVIRONMENT_NAME)
                .addProperties(fields)
                .build()
        ).build().writeTo(File(filePath))
        return parameters
    }

    companion object {
        private const val ENVIRONMENT_NAME = "Environment"
        private const val CONFIG_NAME = "EnvConfig"
    }
}