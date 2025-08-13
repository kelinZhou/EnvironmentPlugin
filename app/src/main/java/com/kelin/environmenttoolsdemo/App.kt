package com.kelin.environmenttoolsdemo

import android.app.Application

/**
 * **创建人:** kelin
 *
 * **创建时间:** 2019-09-15  12:54
 *
 * **版本:** v 1.0.0
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        EnvConfig.initial(this)
    }
}