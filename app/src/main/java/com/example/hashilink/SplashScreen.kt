package com.example.hashilink

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.a_splash_screen)

        android.os.Handler().postDelayed({
            val i= Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        },4000)

    }
}