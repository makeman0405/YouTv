package com.goldenmelon.youtv.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import androidx.annotation.RequiresApi
import com.goldenmelon.youtv.R
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.toolbar

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Note that the Toolbar defined in the layout has the id "toolbar"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar_back.setOnClickListener {
            finish()
        }

        webview.apply {
            webViewClient = MyWebViewClient()
            settings.javaScriptEnabled = true
            loadUrl(LOGIN_URL)
        }
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            CookieManager.getInstance().flush();
        }
    }

    companion object {
        const val TAG = "LoginActivity"
        private const val LOGIN_URL = "https://accounts.google.com/ServiceLogin"

        fun startActivityForResult(activity: Activity, requestCode: Int) {
            activity.startActivityForResult(
                Intent(
                    activity,
                    LoginActivity::class.java
                ), requestCode
            )
        }
    }
}
