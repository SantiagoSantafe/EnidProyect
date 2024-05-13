package com.santiago.enidproyect

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = Color.parseColor("#FB6F2F")

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LogIn::class.java))
            finish()
        },3000)
    }
}