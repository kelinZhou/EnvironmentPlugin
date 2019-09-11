package com.kelin.environmenttoolsdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        BuildConfig.APPLICATION_ID
        val env = EnvConfig.getEnvironment(EnvConfig.Type.RELEASE)
        env.GRPC_API_HOST
        env.FILE_HOST
        env.J_PUSH_APP_KEY
        env.TC_APP_SECRET_KEY
    }
}
