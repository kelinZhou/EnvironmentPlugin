package com.kelin.environmenttoolsdemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateEnvValue()
    }

    @SuppressLint("SetTextI18n")
    private fun updateEnvValue() {
        val env = EnvConfig.getEnv()
        tvVariable.text =
            env.javaClass.fields.joinToString("\n") { "${it.name}:${it.get(env)}" } + "\nApplicationId:${BuildConfig.APPLICATION_ID}"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return if (!EnvConfig.IS_RELEASE) {
            menuInflater.inflate(R.menu.menu_switch, menu)
            EnvConfig.Type.values().forEachIndexed { index, type ->
                menu.getItem(index)?.apply {
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
        val type = EnvConfig.Type.values().find { it.alias == item.title }
        if (type != null) {
            val success = EnvConfig.setEnvType(type)
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
