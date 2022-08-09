package com.kelin.environment

import com.kelin.environment.extension.EnvironmentExtension
import com.squareup.javapoet.*
import java.io.File
import javax.lang.model.element.Modifier
import com.squareup.javapoet.MethodSpec
import java.lang.reflect.Type


/**
 * **描述:** 制造环境的配置
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2019-09-10  16:29
 *
 * **版本:** v 1.0.0
 */
class GeneratedEnvConfig(
    private val filePath: String,
    private val packageName: String,
    private val environment: EnvType,
    private val isRelease: Boolean,
    private val version: String,
    private val allVariables: Map<String, Variable>,
    private val release: EnvironmentExtension,
    private val dev: EnvironmentExtension,
    private val test: EnvironmentExtension,
    private val demo: EnvironmentExtension
) {

    internal fun generate() {
        val parameters = writeEnvironmentInterface(allVariables, packageName, filePath)
        val typeType = ClassName.get(packageName, CONFIG_NAME, "Type")
        val nameOf = MethodSpec.methodBuilder("nameOf")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addParameter(String::class.java, "typeName")
            .returns(typeType)
            .addStatement(
                if (isRelease) {
                    "return RELEASE"
                } else {
                    "if (typeName != null) {\n" +
                            "    for (Type value : values()) {\n" +
                            "        if (value.name().toLowerCase().equals(typeName.toLowerCase())) {\n" +
                            "            return value;\n" +
                            "        }\n" +
                            "    }\n" +
                            "}\n" +
                            "return RELEASE"
                }
            )
            .build()

        val environmentType = ClassName.get(packageName, ENVIRONMENT_NAME)
        val contextType = ClassName.get("android.content", "Context")
        val applicationType = ClassName.get("android.app", "Application")


        val environmentImpl = TypeSpec.classBuilder("${ENVIRONMENT_NAME}Impl")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .superclass(environmentType)
            .addMethod(
                MethodSpec.constructorBuilder()
                    .apply {
                        parameters.forEach { addParameter(it.first, it.second) }
                    }
                    .addStatement("super(${parameters.joinToString { it.second }})")
                    .build()
            )
            .build()


        val configSpec = TypeSpec.classBuilder(CONFIG_NAME)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addJavadoc("**Description:** configure the environment.(Automatically generated file. DO NOT MODIFY)。\n<p>\n**Version:** v $version\n")
            .addField(
                FieldSpec.builder(
                    Boolean::class.java,
                    "IS_RELEASE",
                    Modifier.PUBLIC,
                    Modifier.STATIC,
                    Modifier.FINAL
                )
                    .initializer("Boolean.parseBoolean(\"$isRelease\")")
                    .build()
            )
            .addField(
                FieldSpec.builder(
                    Boolean::class.java,
                    "IS_DEBUG",
                    Modifier.PUBLIC,
                    Modifier.STATIC,
                    Modifier.FINAL
                )
                    .initializer("Boolean.parseBoolean(\"${!isRelease}\")")
                    .build()
            )
            .addField(
                FieldSpec.builder(
                    typeType,
                    "INIT_ENV",
                    Modifier.PUBLIC,
                    Modifier.STATIC,
                    Modifier.FINAL
                )
                    .initializer("Type.nameOf(\"$environment\")")
                    .build()
            )
            .apply {
                addField(
                    FieldSpec.builder(
                        environmentType,
                        "RELEASE_ENV",
                        Modifier.PRIVATE,
                        Modifier.STATIC,
                        Modifier.FINAL
                    )
                        .initializer("new EnvironmentImpl(${release.getEnvironmentArgs("Release", allVariables)})")
                        .build()
                )
                if (!isRelease) {
                    if (dev.variables.isNotEmpty()) {
                        addField(
                            FieldSpec.builder(
                                environmentType,
                                "DEV_ENV",
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL
                            )
                                .initializer("new EnvironmentImpl(${dev.getEnvironmentArgs("Dev", allVariables)})")
                                .build()
                        )
                    }
                    if (test.variables.isNotEmpty()) {
                        addField(
                            FieldSpec.builder(
                                environmentType,
                                "TEST_ENV",
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL
                            )
                                .initializer("new EnvironmentImpl(${test.getEnvironmentArgs("Test", allVariables)})")
                                .build()
                        )
                    }
                    if (demo.variables.isNotEmpty()) {
                        addField(
                            FieldSpec.builder(
                                environmentType,
                                "DEMO_ENV",
                                Modifier.PRIVATE,
                                Modifier.STATIC,
                                Modifier.FINAL
                            )
                                .initializer("new EnvironmentImpl(${demo.getEnvironmentArgs("Demo", allVariables)})")
                                .build()
                        )
                    }
                    addField(
                        FieldSpec.builder(contextType, "context", Modifier.PRIVATE, Modifier.STATIC)
                            .build()
                    )
                }
            }
            .addField(
                FieldSpec.builder(typeType, "curEnvType", Modifier.PRIVATE, Modifier.STATIC)
                    .apply {
                        if (isRelease) {
                            initializer("Type.RELEASE")
                        }
                    }
                    .build()
            )
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addStatement("throw new RuntimeException(\"EnvConfig can't be constructed\")")
                    .build()
            )
            .addMethod(
                MethodSpec.methodBuilder("init")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(applicationType, "app")
                    .apply {
                        if (!isRelease) {
                            addStatement(
                                getInitMethodCode(),
                                ClassName.get("android.preference", "PreferenceManager")
                            )
                        }
                    }
                    .build()
            )
            .addMethod(
                MethodSpec.methodBuilder("setEnvType")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(typeType, "type")
                    .addCode(setEnvironmentMethodCode())
                    .returns(Boolean::class.java)
                    .build()
            )
            .addMethod(
                MethodSpec.methodBuilder("getEnvType")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(typeType)
                    .addStatement("return curEnvType")
                    .build()
            )
            .addMethod(
                MethodSpec.methodBuilder("getEnv")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addCode(getEnvironmentMethodCode())
                    .returns(environmentType)
                    .build()
            )
            .addType(
                TypeSpec.enumBuilder("Type")
                    .addModifiers(Modifier.PUBLIC)
                    .addEnumConstant(
                        "RELEASE",
                        TypeSpec.anonymousClassBuilder("\"${release.alias}\"").build()
                    )
                    .apply {
                        if (!isRelease) {
                            if (dev.variables.isNotEmpty()) {
                                addEnumConstant(
                                    "DEV",
                                    TypeSpec.anonymousClassBuilder("\"${dev.alias}\"").build()
                                )
                            }
                            if (test.variables.isNotEmpty()) {
                                addEnumConstant(
                                    "TEST",
                                    TypeSpec.anonymousClassBuilder("\"${test.alias}\"").build()
                                )
                            }
                            if (demo.variables.isNotEmpty()) {
                                addEnumConstant(
                                    "DEMO",
                                    TypeSpec.anonymousClassBuilder("\"${demo.alias}\"").build()
                                )
                            }
                        }
                    }
                    .addField(String::class.java, "alias", Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(
                        MethodSpec.constructorBuilder()
                            .addParameter(String::class.java, "alias")
                            .addStatement("this.\$N = \$N", "alias", "alias")
                            .build()
                    )
                    .addMethod(nameOf)
                    .build()
            )
            .addType(environmentImpl)
            .build()


        JavaFile.builder(packageName, configSpec)
            .build().writeTo(File(filePath))
    }

    private fun setEnvironmentMethodCode(): String {
        return if (isRelease) {
            return "return true;\n"
        } else {
            "if (type != curEnvType) {\n" +
                    "    curEnvType = type;\n" +
                    "   if (context != null) {\n" +
                    "       PreferenceManager.getDefaultSharedPreferences(context).edit().putString(\"current_environment_type_string_name\", type.name()).apply();\n" +
                    "   }\n" +
                    "   return true;\n" +
                    "} else {\n" +
                    "   return false;\n" +
                    "}\n"
        }
    }

    private fun getInitMethodCode(): String {
        return "context = app.getApplicationContext();\ncurEnvType = Type.nameOf(\$T.getDefaultSharedPreferences(context).getString(\"current_environment_type_string_name\", INIT_ENV.name()))"
    }

    private fun getEnvironmentMethodCode(): String {
        if (!isRelease) {
            return "switch (curEnvType) {\n" +
                    "    case RELEASE:\n" +
                    "        return RELEASE_ENV;\n" +
                    if (dev.variables.isNotEmpty()) {
                        "    case DEV:\n" +
                                "        return DEV_ENV;\n"
                    } else {
                        ""
                    } +
                    if (test.variables.isNotEmpty()) {
                        "    case TEST:\n" +
                                "        return TEST_ENV;\n"
                    } else {
                        ""
                    } +
                    if (demo.variables.isNotEmpty()) {
                        println()
                        "    case DEMO:\n" +
                                "        return DEMO_ENV;\n"
                    } else {
                        ""
                    } +
                    "    default:\n" +
                    "        throw new RuntimeException(\"the type:\" + curEnvType.toString() + \" is unkonwn !\");\n" +
                    "}"
        } else {
            return "return RELEASE_ENV;\n"
        }
    }

    private fun writeEnvironmentInterface(
        variables: Map<String, Variable>,
        packageName: String,
        filePath: String
    ): List<Pair<Type, String>> {
        val parameters = ArrayList<Pair<Type, String>>(variables.size)
        val fields = ArrayList<FieldSpec>(variables.size)
        val constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PROTECTED)
        val entries = variables.entries
        for (i: Int in entries.indices) {
            val entry = entries.elementAt(i)
            fields.add(
                FieldSpec.builder(
                    entry.value.type,
                    entry.key,
                    Modifier.PUBLIC,
                    Modifier.FINAL
                ).build()
            )
            constructor.addParameter(entry.value.type, "var$i")
                .addStatement("\$N = \$N", entry.key, "var$i")
            parameters.add(Pair(entry.value.type, "var$i"))
        }


        val typeSpec = TypeSpec.classBuilder(ENVIRONMENT_NAME)
            .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
            .addFields(fields)
            .addMethod(constructor.build())
            .build()

        JavaFile.builder(packageName, typeSpec).build().writeTo(File(filePath))
        return parameters
    }

    companion object {
        private const val ENVIRONMENT_NAME = "Environment"
        private const val CONFIG_NAME = "EnvConfig"
    }
}