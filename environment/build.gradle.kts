buildscript {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${rootProject.extra["kotlin_version"]}")
        classpath("com.gradle.publish:plugin-publish-plugin:1.2.1")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("kotlin")
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.2.1"
}

val pluginGroup = "com.kelin.environment"
val pluginVersion = "2.0.0"

group = pluginGroup
version = pluginVersion

dependencies {
    testImplementation("junit:junit:4.13.2")
    //gradle sdk
    implementation(gradleApi())
    //groovy sdk
    implementation(localGroovy())
//    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    implementation("com.android.tools.build:gradle:4.0.2")
    implementation("com.squareup:kotlinpoet:2.2.0")//用来生成kotlin代码文件的，避免字符串拼接的尴尬
}

gradlePlugin {
    website = "https://github.com/kelinZhou/EnvironmentPlugin"
    vcsUrl = "https://github.com/kelinZhou/EnvironmentPlugin"
    plugins {
        create("environmentPlugin") {
            id = "com.kelin.environment"
            displayName = "Environment build Plugin"
            description = "use this to configuration your api environment."
            implementationClass = "com.kelin.environment.EnvironmentPlugin"
            tags.set(setOf("Android", "Environment configuration"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = pluginGroup
            artifactId = "environment"
            version = pluginVersion

            from(components["java"])
        }
    }

    repositories {
        maven {
            url = uri("file:/Users/kelin/mavenRepo")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}