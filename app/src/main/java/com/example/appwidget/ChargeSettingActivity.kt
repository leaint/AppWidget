package com.example.appwidget

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity

class ChargeSettingActivity : ComponentActivity() {


    override fun onNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charge_setting)

        val editText = findViewById<EditText>(R.id.editTextTextMultiLine)

        intent.getStringExtra(MainActivity.SET_ALARM_INTERV)?.let {
            editText.setText(it)
        }

        findViewById<Button>(R.id.button2).setOnClickListener {
            val intent = Intent().apply {
                putExtra(MainActivity.SET_ALARM_INTERV, editText.text.toString())
            }
            setResult(RESULT_OK, intent)
            finish()
        }

//        onBackPressedDispatcher.hasEnabledCallbacks()
//        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                setResult(RESULT_CANCELED)
//                finish()
//            }
//        })
    }

}