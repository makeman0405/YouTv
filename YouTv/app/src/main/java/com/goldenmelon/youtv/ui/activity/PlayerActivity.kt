package com.goldenmelon.youtv.ui.activity

import android.content.*
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.goldenmelon.youtv.R
import com.goldenmelon.youtv.datas.PlayContent
import com.goldenmelon.youtv.service.MediaService
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.activity_player.*

class PlayerActivity : AppCompatActivity() {
    //Dash URL Test
    //url = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd"
    var isFullScreen = false

    //MediaService
    private var mBound: Boolean = false
    private var serviceRef: MediaService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceRef = (service as MediaService.MediaBinder).getService().also { it ->
                video_view.player = it.getPlayer()
                //init
                video_view.keepScreenOn =
                    !(video_view.player!!.playbackState == Player.STATE_IDLE
                            || video_view.player!!.playbackState == Player.STATE_ENDED ||
                            !video_view.player!!.playWhenReady)
                video_view.player!!.addListener(object : Player.EventListener {
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        this@PlayerActivity.video_view.keepScreenOn =
                            !(playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED ||
                                    !playWhenReady)
                    }
                })

                it.load(
                    intent.getParcelableExtra("playContent")
                )
            }

            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            video_view.player = null
            serviceRef = null
            mBound = false
        }
    }

    private val br = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MediaService.ACTION_QUIT -> {
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        video_title.text = intent.getParcelableExtra<PlayContent>("playContent")?.title ?: ""

        fullscreen.setOnClickListener {
            requestedOrientation = if (isFullScreen) {
                showSystemUI()
                it.setBackgroundResource(R.drawable.exo_controls_fullscreen_enter)
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } else {
                hideSystemUI()
                it.setBackgroundResource(R.drawable.exo_controls_fullscreen_exit)
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
            }
            isFullScreen = !isFullScreen
        }

        video_view.setControllerVisibilityListener {
            video_bar.visibility = it
            fullscreen.visibility = it

            //Fixed Bug - VideoView Controller 가 사라질때 FullScreen 인 경우 사용에 의해 나타난 상태바와 소프트키를 사라지도록함.
            if(it != View.VISIBLE && isFullScreen) {
                hideSystemUI()
            }
        }

        registerReceivers()
    }

    override fun onStart() {
        super.onStart()

        Intent(this, MediaService::class.java).also { intent ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        showSystemUI()
    }

    override fun onResume() {
        super.onResume()

        //Fixed Bug - FullScreen 상태에서 상태바, 소프트키가 보여짐. 경로: FullScreen 설정 -> 홈키 -> 재진입
        requestedOrientation = if (!isFullScreen) {
            showSystemUI()
            fullscreen.setBackgroundResource(R.drawable.exo_controls_fullscreen_enter)
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else {
            hideSystemUI()
            fullscreen.setBackgroundResource(R.drawable.exo_controls_fullscreen_exit)
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        }

    }


    private fun registerReceivers() {
        registerReceiver(br, IntentFilter().apply {
            addAction(MediaService.ACTION_PLAY)
            addAction(MediaService.ACTION_QUIT)
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    override fun onBackPressed() {
        if (isFullScreen) {
            fullscreen.callOnClick()
        } else {
            if (isTaskRoot) {
                val intent = Intent(
                    this,
                    MainActivity::class.java
                )
                startActivity(intent)
            }
            super.onBackPressed()
        }
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE /*(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)*/
        //window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        //window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)

//        if (mBound) unbindService(connection)
//        video_view.player = null
    }

    override fun onStop() {
        super.onStop()

        if (mBound) unbindService(connection)
        video_view.player = null
    }

    override fun onDestroy() {
        unregisterReceiver(br)
        super.onDestroy()
    }

    companion object {
        const val TAG = "PlayerActivity"

        public fun startActivity(context: Context, playContent: PlayContent) {
            Intent(
                context,
                PlayerActivity::class.java
            ).putExtra("playContent", playContent).let {
                context.startActivity(it)
            }
        }
    }
}