package com.example.moodflow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Use a layout-based splash to fully control image size
        setTheme(R.style.Theme_MoodFlow_Splash)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Brief delay so the splash is visible
        Handler(Looper.getMainLooper()).postDelayed({
            val next = Intent(this, MainActivity::class.java)
            // preserve any extras (e.g., open_log_water)
            next.putExtras(intent)
            startActivity(next)
            finish()
        }, 1200)
    }
}


