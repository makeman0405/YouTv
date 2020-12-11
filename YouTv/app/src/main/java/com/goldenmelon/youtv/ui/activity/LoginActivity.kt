package com.goldenmelon.youtv.ui.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import com.goldenmelon.youtv.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //viewBinding...
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    fun initUI() {
        // Note that the Toolbar defined in the layout has the id "toolbar"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbarBack.setOnClickListener {
            finish()
        }

        binding.webview.apply {
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
