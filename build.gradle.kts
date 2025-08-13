import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply{
        set("kotlin_version", "2.2.0")
        set("packageName", "com.kelin.environmenttoolsdemo")
    }

    repositories {
        google()
        maven { url = uri("/Users/kelin/mavenRepo") }
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        maven {
            url = uri("https://maven.google.com/")
            name = "Google"
        }


        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
    }
    dependencies {
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra["kotlin_version"]}")
        classpath ("com.kelin.environment:environment:2.0.0.4")
        classpath ("com.android.tools.build:gradle:8.2.1")
        classpath ("gradle.plugin.com.dorongold.plugins:task-tree:1.3")
        classpath ("com.novoda:bintray-release:0.5.0")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}