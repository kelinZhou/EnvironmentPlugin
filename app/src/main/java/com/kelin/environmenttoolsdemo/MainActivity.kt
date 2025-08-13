package com.kelin.environmenttoolsdemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateEnvValue()
    }

    @SuppressLint("SetTextI18n")
    private fun updateEnvValue() {
        EnvConfig.environment.also { env -> //通过EnvConfig.environment获取当前的环境变量
            //通过暴力反射获取所有的环境变量并拼接成字符串
            val values = env.javaClass.declaredFields.onEach {
                it.isAccessible = true
            }.joinToString("\n") { "${it.name}:${it.get(env)}" } + "\nApplicationId:${BuildConfig.APPLICATION_ID}" + "\nVersionName:${BuildConfig.VERSION_NAME}"

            //将获取到环境变量设置给TextView用于显示。
            findViewById<TextView>(R.id.tvVariable).text = values
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return if (!EnvConfig.IS_RELEASE) {
            menuInflater.inflate(R.menu.menu_switch, menu)
            EnvType.entries.forEachIndexed { index, type ->
                menu[index].apply {
                    title = type.alias
                    isVisible = true
                }
            }
            true
        } else {
            super.onCreateOptionsMenu(menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        EnvType.entries.find { it.alias == item.title }?.also { type ->
            val success = EnvConfig.switchEnv(type)
            if (success) {
                updateEnvValue()
                Toast.makeText(applicationContext, "已切换至${type.alias}环境", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "已经是${type.alias}环境", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }
}
