package com.kelin.environment

import com.kelin.environment.model.Version
import com.kelin.environment.model.lessThan
import org.gradle.internal.impldep.org.junit.Test

class ExampleUnitTest {

    @Test
    fun testVersion() {
        println(Version("6.1.1").lessThan(Version("6.7.1")))
    }
}