package com.goldenmelon.youtv.utils

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.DisplayMetrics
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

const val MAIN_URL = "https://m.youtube.com/"
const val CHANNEL_URL = "https://m.youtube.com"

const val SEARCH_URL = "https://m.youtube.com/results?search_query="

//확장 함수(추가 Study: 확장 프로퍼티)
//fun ImageView.loadImage(url: String?) {
//    url?.let {
//        Glide.with(context).load(it).into(this)
//    }
//}

fun ImageView.loadImage(url: String?, requestOptions: RequestOptions) {
    url?.let {
        Glide.with(context).load(it).apply(requestOptions).into(this)
    }
}

fun shareContent(context: Context, videoId: String?) {
    videoId?.let {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "https://youtu.be/$it")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, null))
    }
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //for other device how are able to connect with Ethernet
            //actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //for check internet over Bluetooth
            //actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    } else {
        val nwInfo = connectivityManager.activeNetworkInfo ?: return false
        return nwInfo.isConnected
    }
}

fun isWIFIConnected(context: Context): Boolean {
    var result = false
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
    val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
    if (capabilities != null) {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            result = true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            result = false
        }
    }
    return result
}

/**
 * This method converts dp unit to equivalent pixels, depending on device density.
 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
 * @param context Context to get resources and device specific display metrics. If you don't have
 * access to Context, just pass null.
 * @return A float value to represent px equivalent to dp depending on device density
 */
fun convertDpToPixel(dp: Float, context: Context?): Float {
    return if (context != null) {
        val resources = context.resources
        val metrics = resources.displayMetrics
        dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    } else {
        val metrics = Resources.getSystem().displayMetrics
        dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}

/**
 * This method converts device specific pixels to density independent pixels.
 * @param px A value in px (pixels) unit. Which we need to convert into db
 * @param context Context to get resources and device specific display metrics. If you don't have
 * access to Context, just pass null.
 * @return A float value to represent dp equivalent to px value
 */
fun convertPixelsToDp(px: Float, context: Context?): Float {
    return if (context != null) {
        val resources = context.resources
        val metrics = resources.displayMetrics
        px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    } else {
        val metrics = Resources.getSystem().displayMetrics
        px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}

//not dash
//278(144) 242(240) webm
//160(144) 133(240), 18(360), 22(720) mp4
val SUPPORT_ITAG_LIST = listOf(278, 242, 18, 22)
var SUPPORT_ITAG_ONLY_AUDIO = 249

enum class Quality(val stringValue: String, val intValue: Int) {
    Q_144P_ONLY_VEDIO("144P", 144), Q_240P_ONLY_VEDIO("240P", 240), Q_360P("360P", 360), Q_720P("720P", 720);

    companion object {
        fun getStringValue(intValue: Int):String? {
            return values().find { value ->
                value.intValue == intValue
            }?.stringValue
        }
    }
}
