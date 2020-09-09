package com.goldenmelon.youtv.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.goldenmelon.youtv.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed(
            {
                //throw RuntimeException("Test Crash")
                MainActivity.startActivity(this)
                overridePendingTransition(0, 0);
                finish()
            }, 300
        )
    }
}