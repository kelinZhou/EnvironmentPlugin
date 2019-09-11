package com.kelin.environment

import com.squareup.javapoet.*
import java.io.File
import javax.lang.model.element.Modifier

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
    private val environment: String,
    private val isRelease: Boolean,
    private val version: String,
    private val release: HashMap<String, Variable>,
    private val dev: HashMap<String, Variable>,
    private val test: HashMap<String, Variable>,
    private val demo: HashMap<String, Variable>
) {

    internal fun generate() {
        val parameters = writeEnvironmentInterface(release, packageName, filePath)
        val typeType = ClassName.get(packageName, CONFIG_NAME, "Type")
        val nameOf = MethodSpec.methodBuilder("nameOf")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addParameter(String::class.java, "typeName")
            .returns(typeType)
            .addStatement(
                "for (Type value : values()) {\n" +
                        "    if (value.toString().toLowerCase().equals(typeName)) {\n" +
                        "        return value;\n" +
                        "    }\n" +
                        "}\n" +
                        "return RELEASE"
            )
            .build()

        val typeEnum = TypeSpec.enumBuilder("Type")
            .addModifiers(Modifier.PUBLIC)
            .addEnumConstant("RELEASE")
            .apply {
                if (!isRelease) {
                    if (dev.isNotEmpty()) {
                        addEnumConstant("DEV")
                    }
                    if (test.isNotEmpty()) {
                        addEnumConstant("TEST")
                    }
                    if (demo.isNotEmpty()) {
                        addEnumConstant("DEMO")
                    }
                }
            }
            .addMethod(nameOf)
            .build()

        val environmentType = ClassName.get(packageName, ENVIRONMENT_NAME)
        val environmentImplConstructor = MethodSpec.constructorBuilder()
        parameters.forEach { environmentImplConstructor.addParameter(String::class.java, it) }
        environmentImplConstructor.addStatement("super(${parameters.joinToString(", ")})")
        val environmentImpl = TypeSpec.classBuilder("${ENVIRONMENT_NAME}Impl")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .superclass(environmentType)
            .addMethod(environmentImplConstructor.build())
            .build()

        val getEnvironment = MethodSpec.methodBuilder("getEnvironment")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(typeType, "type")
            .addCode(getEnvironmentParameterFormat(isRelease, release, dev, test, demo))
            .returns(environmentType)
            .build()

        val configSpec = TypeSpec.classBuilder(CONFIG_NAME)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addJavadoc("**Description:** configure the environment.(Automatically generated file. DO NOT MODIFY)。\n<p>\n**Version:** v $version\n")
            .addType(typeEnum)
            .addField(
                FieldSpec.builder(Boolean::class.java, "IS_RELEASE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("Boolean.parseBoolean(\"$isRelease\")")
                    .build()
            )
            .addField(
                FieldSpec.builder(typeType, "PLACEHOLDER_ENVIRONMENT", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("Type.nameOf(\"$environment\")")
                    .build()
            )
            .addField(
                FieldSpec.builder(typeType, "curEnvironment", Modifier.PUBLIC, Modifier.STATIC)
                    .initializer("PLACEHOLDER_ENVIRONMENT")
                    .build()
            )
            .addMethod(getEnvironment)
            .addType(environmentImpl)
            .build()


        JavaFile.builder(packageName, configSpec).build().writeTo(File(filePath))
    }

    private fun getEnvironmentParameterFormat(
        isRelease: Boolean,
        release: HashMap<String, Variable>,
        dev: HashMap<String, Variable>,
        test: HashMap<String, Variable>,
        demo: HashMap<String, Variable>
    ): String {
        println("========Release Environment:")
        release.forEach {
            println("========variable: ${it.key} | ${it.value.value}")
        }
        if (!isRelease) {
            return "switch (curEnvironment) {\n" +
                    "    case RELEASE:\n" +
                    "        return new EnvironmentImpl(${release.values.joinToString(", ") { "\"${it.value}\"" }});\n" +
                    if (dev.isNotEmpty()) {
                        println("========Dev Environment:")
                        dev.forEach {
                            println("========variable: ${it.key} | ${it.value.value}")
                        }
                        "    case DEV:\n" +
                                "        return new EnvironmentImpl(${dev.values.joinToString(", ") { "\"${it.value}\"" }});\n"
                    } else {
                        ""
                    } +
                    if (test.isNotEmpty()) {
                        println("========Test Environment:")
                        test.forEach {
                            println("========variable: ${it.key} | ${it.value.value}")
                        }
                        "    case TEST:\n" +
                                "        return new EnvironmentImpl(${test.values.joinToString(", ") { "\"${it.value}\"" }});\n"
                    } else {
                        ""
                    } +
                    if (demo.isNotEmpty()) {
                        println("========Demo Environment:")
                        demo.forEach {
                            println("========variable: ${it.key} | ${it.value.value}")
                        }
                        "    case DEMO:\n" +
                                "        return new EnvironmentImpl(${demo.values.joinToString(", ") { "\"${it.value}\"" }});\n"
                    } else {
                        ""
                    } +
                    "    default:\n" +
                    "        throw new RuntimeException(\"the type:\" + type.toString() + \" is unkonwn !\");\n" +
                    "}"
        } else {
            return "return new EnvironmentImpl(${release.values.joinToString(", ") { "\"${it.value}\"" }});\n"
        }
    }

    private fun writeEnvironmentInterface(
        release: HashMap<String, Variable>,
        packageName: String,
        filePath: String
    ): List<String> {
        val parameters = ArrayList<String>(release.size)
        val fields = ArrayList<FieldSpec>(release.size)
        val constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PROTECTED)
        val entries = release.entries
        for (i: Int in 0 until entries.size) {
            val entry = entries.elementAt(i)
            fields.add(FieldSpec.builder(String::class.java, entry.key, Modifier.PUBLIC, Modifier.FINAL).build())
            constructor.addParameter(String::class.java, "var$i")
                .addStatement("\$N = \$N", entry.key, "var$i")
            parameters.add("var$i")
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