package com.goldenmelon.youtv.ui.activity.base

import android.content.*
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.bumptech.glide.request.RequestOptions
import com.goldenmelon.youtv.R
import com.goldenmelon.youtv.application.App
import com.goldenmelon.youtv.datas.PlayContent
import com.goldenmelon.youtv.preference.Prefs
import com.goldenmelon.youtv.service.MediaService
import com.goldenmelon.youtv.ui.activity.PlayerActivity
import com.goldenmelon.youtv.utils.isNetworkAvailable
import com.goldenmelon.youtv.utils.loadImage
import kotlinx.android.synthetic.main.activity_main.*


open class BaseContentListActivity : AppCompatActivity() {
    //ItemFragment OnListFragmentInteractionListener Callback method
    val loadingManager = LoadingManager()

    //preference
    val prefs: Prefs by lazy {
        App.prefs!!
    }

    //MediaService
    var mBound: Boolean = false
    var serviceRef: MediaService? = null
    val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceRef = (service as MediaService.MediaBinder).getService().also {
                toolbar_play.setBackgroundResource(
                    if (it.isPlaying()) {
                        R.drawable.exo_notification_pause
                    } else {
                        R.drawable.exo_notification_play
                    }
                )
            }

            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceRef = null
            mBound = false
        }
    }

    val br = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MediaService.ACTION_QUIT -> {
                    toolbar_play.visibility = View.INVISIBLE
                    updateShortcut()
                    unbindMediaService()
                }
                MediaService.ACTION_UPDATE_PLAY_UI -> {
                    toolbar_play.setBackgroundResource(
                        if (intent.getBooleanExtra("isPlaying", false)) {
                            R.drawable.exo_notification_pause
                        } else {
                            R.drawable.exo_notification_play
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (MediaService.isRunning) {
            Intent(this, MediaService::class.java).also { intent ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }

        updateShortcut()
        registerReceivers()
    }

    override fun onPause() {
        super.onPause()
        unbindMediaService()
        unregisterReceiver(br)
    }

    fun registerReceivers() {
        registerReceiver(br, IntentFilter().apply {
            addAction(MediaService.ACTION_QUIT)
            addAction(MediaService.ACTION_UPDATE_PLAY_UI)
        })
    }

    fun unbindMediaService() {
        if (mBound) unbindService(connection)
        mBound = false
        serviceRef = null
    }

    fun playContent(videoId: String) {
        val tempContext = this
        loadingManager.showCenterLoading()
        object : YouTubeExtractor(this) {
            override fun onExtractionComplete(
                ytFiles: SparseArray<YtFile>?,
                vMeta: VideoMeta?
            ) {
                var isSuccess = false
                if (ytFiles != null && vMeta != null) {
                    for (itag in SUPPORT_ITAG_LIST) {
                        if (ytFiles[itag] != null) {
                            PlayerActivity.startActivity(
                                tempContext, PlayContent(
                                    vMeta.videoId,
                                    vMeta.title,
                                    vMeta.hqImageUrl ?: vMeta.thumbUrl,
                                    ytFiles[itag].url
                                )
                            )

                            isSuccess = true
                            break
                        }
                    }
                }

                if (!isSuccess) {
                    if (isNetworkAvailable(this@BaseContentListActivity)) {
                        showAlertDialog(
                            message = getString(R.string.popup_msg_not_supported_content_run_youtube),
                            positionListener = DialogInterface.OnClickListener { _, _ ->
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.youtube.com/watch?v=$videoId")
                                ).also { intent ->
                                    startActivity(
                                        intent
                                    )
                                }
                            })
                    } else {
                        Toast.makeText(
                            this@BaseContentListActivity,
                            "Please check the network", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                LoadingManager().dismissCenterLoading()
            }
        }.extract("https://www.youtube.com/watch?v=$videoId", true, true)
    }

    fun updateShortcut() {
        shortcut.visibility = View.INVISIBLE
        if (MediaService.isRunning) {
            prefs.getPlayContent()?.let { it ->
                shortcut.visibility = View.VISIBLE
                img_shortcut.loadImage(it.thumbUrl, RequestOptions().circleCrop())
            }
        }
    }

    fun showAlertDialog(
        title: String? = null,
        message: String? = null,
        positionListener: DialogInterface.OnClickListener? = null,
        negativeListener: DialogInterface.OnClickListener? = null
    ) {
        var builder = AlertDialog.Builder(this@BaseContentListActivity)
        if (!title.isNullOrBlank()) {
            builder.setMessage(title)
        }
        if (!message.isNullOrBlank()) {
            builder.setMessage(message)
        }

        builder.setCancelable(true)
            .setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialog, id ->
                    positionListener?.onClick(dialog, id)
                    dialog.cancel()
                })
            .setNegativeButton(
                "No",
                DialogInterface.OnClickListener { dialog, id ->
                    negativeListener?.onClick(dialog, id)
                    dialog.cancel()
                })
            .create().show()
    }


    inner class LoadingManager {
        fun showCenterLoading() {
            center_loading.visibility = View.VISIBLE
            //start blocking
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        fun dismissCenterLoading() {
            center_loading.visibility = View.INVISIBLE
            //finish blocking
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        fun showBottomLoading() {
            bottom_loading.visibility = View.VISIBLE
        }

        fun dismissBottomLoading() {
            bottom_loading.visibility = View.INVISIBLE
        }
    }

    companion object {
        //not dash
        //18(360), 22(720)
        //dash
        //136(720), 137(1080)
        val SUPPORT_ITAG_LIST = /*listOf(140)*/ listOf(18, 22)
    }
}