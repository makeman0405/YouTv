package com.goldenmelon.youtv.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

const val MAIN_URL = "https://m.youtube.com/"
const val CHANNEL_URL = "https://m.youtube.com"

const val SEARCH_URL = "https://m.youtube.com/results?search_query="

//확장 함수(추가 Study: 확장 프로퍼티)
public fun ImageView.loadImage(url: String?, requestOptions: RequestOptions) {
    Glide.with(context).load(url).apply(requestOptions).into(this)
}

public fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    } else {
        val nwInfo = connectivityManager.activeNetworkInfo ?: return false
        return nwInfo.isConnected
    }
}

public fun shareContent(context: Context, videoId: String?) {
    videoId?.let {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "https://youtu.be/$it")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, null))
    }
}


//not dash
//18(360), 22(720)
//dash
//136(720), 137(1080)
val SUPPORT_ITAG_LIST = /*listOf(140)*/ listOf(18, 22)