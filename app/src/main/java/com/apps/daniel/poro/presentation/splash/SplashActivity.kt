package com.apps.daniel.poro.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.apps.daniel.poro.R
import com.apps.daniel.poro.presentation.timer.TimerActivity
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            startTimerActivity()
        }, SPLASH_TIME_OUT)

    }

    private fun startTimerActivity(){
        val i = Intent(this, TimerActivity::class.java)
        startActivity(i)
    }
}