package com.santiago.enidproyect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.common.SignInButton

class LogIn : AppCompatActivity() {
    lateinit var googleBtn: SignInButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        googleBtn = findViewById(R.id.sign_in_button)

        googleBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}