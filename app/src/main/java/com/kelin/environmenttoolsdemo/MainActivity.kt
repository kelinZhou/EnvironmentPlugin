package com.kelin.environmenttoolsdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val env = EnvConfig.getEnv()
        env.GRPC_API_HOST
        env.FILE_HOST
        env.J_PUSH_APP_KEY
        env.TC_APP_SECRET_KEY
    }
}
