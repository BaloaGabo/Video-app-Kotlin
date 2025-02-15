package com.example.video_app_kotlin

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        verSplash()
    }

    private fun verSplash() {
        object : CountDownTimer(3000,1000){
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finishAffinity()
            }

        }.start()
    }
}