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

        webview.webViewClient = MyWebViewClient()
        webview.settings.javaScriptEnabled = true

//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            //noinspection deprecation
//            CookieSyncManager.createInstance(this);
//        }

        webview.loadUrl(LOGIN_URL)
    }

    override fun onResume() {
        super.onResume()
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            //noinspection deprecation
//            CookieSyncManager.getInstance().startSync();
//        }
    }

    override fun onPause() {
        super.onPause()
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            //noinspection deprecation
//            CookieSyncManager.getInstance().stopSync();
//        }
    }

    private inner class MyWebViewClient : WebViewClient() {
        @Suppress("DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//            if (Uri.parse(url.toString()).host == LOGIN_URL) {
//                // This is my web site, so do not override; let my WebView load the page
//                return false
//            }
//            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
//            Intent(Intent.ACTION_VIEW, Uri.parse(url.toString())).apply {
//                startActivity(this)
//            }
//
//            return true

            return false
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
//            request?.let {
//                if (it.url.host == GOOGLE_LOGIN_HOST || it.url.host == YOUTUBE_LOGIN_SUPPORT_HOST) {
//                    // This is my web site, so do not override; let my WebView load the page
//                    return false
//                }
//                // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
//                Intent(Intent.ACTION_VIEW, it.url).apply {
//                    startActivity(this)
//                }
//            }
//
//            return true

            return false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//                //noinspection deprecation
//                CookieSyncManager.getInstance().sync();
//            } else {
                // 롤리팝 이상에서는 CookieManager의 flush를 하도록 변경됨.
                CookieManager.getInstance().flush();
//            }

            url?.let {
                if(it.startsWith("https://www.youtube.com/?app=")) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
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
