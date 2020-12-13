package com.goldenmelon.youtv.ui.activity

import android.content.*
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.goldenmelon.youtv.R
import com.goldenmelon.youtv.application.App
import com.goldenmelon.youtv.databinding.ActivityPlayerBinding
import com.goldenmelon.youtv.datas.PlayContent
import com.goldenmelon.youtv.preference.Prefs
import com.goldenmelon.youtv.service.MediaService
import com.goldenmelon.youtv.utils.Quality
import com.goldenmelon.youtv.utils.shareContent
import com.google.android.exoplayer2.Player


class PlayerActivity : AppCompatActivity() {
    //Dash URL Test
    //url = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd"

    private lateinit var binding: ActivityPlayerBinding

    private val playContent: PlayContent by lazy {
        intent.getParcelableExtra<PlayContent>("playContent")
    }

    private var isFullScreen = false

    //preference
    val prefs: Prefs by lazy {
        App.prefs!!
    }

    //해상도 선택 지
    private var qualitySelectPopup: AlertDialog? = null

    //MediaService
    private var mBound: Boolean = false
    private var serviceRef: MediaService? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceRef = (service as MediaService.MediaBinder).getService().also { it ->
                binding.videoView.player = it.getPlayer()
                //init
                binding.videoView.keepScreenOn =
                    !(binding.videoView.player!!.playbackState == Player.STATE_IDLE
                            || binding.videoView.player!!.playbackState == Player.STATE_ENDED ||
                            !binding.videoView.player!!.playWhenReady)
                binding.videoView.player!!.addListener(object : Player.EventListener {
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        this@PlayerActivity.binding.videoView.keepScreenOn =
                            !(playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED ||
                                    !playWhenReady)
                    }
                })

                it.load(
                    playContent
                )
            }

            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            unbindMediaService()
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

        //viewBinding...
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()
    }

    override fun onStart() {
        super.onStart()
        registerReceivers()
    }

    override fun onResume() {
        super.onResume()
        startBindMediaService()
        setRequestedOrientation()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
        unbindMediaService()
    }

    override fun onStop() {
        unRegisterReceivers()
        super.onStop()
    }

    private fun initUi() {
        binding.videoTitle.text = playContent.title ?: ""

        binding.share.setOnClickListener { _ ->
            //share
            playContent.let {
                shareContent(this, it.videoId)
            }
        }

        binding.fullscreen.setOnClickListener {
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

        binding.videoView.setControllerVisibilityListener {
            binding.videoBar.visibility = it
            binding.settingsLayout.visibility = it

            //Fixed Bug - VideoView Controller 가 사라질때 FullScreen 인 경우 사용에 의해 나타난 상태바와 소프트키를 사라지도록함.
            if (it != View.VISIBLE && isFullScreen) {
                hideSystemUI()
            }
        }

        binding.quality.run {
            text = Quality.getStringValue(prefs.getQuality())
            setOnClickListener {
                showQualityPopup()
            }
        }
    }

    private fun startBindMediaService() {
        Intent(this, MediaService::class.java).also { intent ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            bindService(intent, connection, BIND_AUTO_CREATE)
        }

    }

    private fun unbindMediaService() {
        if (mBound) unbindService(connection)
        mBound = false
        serviceRef = null
    }

    private fun setRequestedOrientation() {
        //Fixed Bug - FullScreen 상태에서 상태바, 소프트키가 보여짐. 경로: FullScreen 설정 -> 홈키 -> 재진입
        requestedOrientation = if (!isFullScreen) {
            showSystemUI()
            binding.fullscreen.setImageResource(R.drawable.exo_controls_fullscreen_enter)
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else {
            hideSystemUI()
            binding.fullscreen.setImageResource(R.drawable.exo_controls_fullscreen_exit)
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        }
    }

    private fun registerReceivers() {
        registerReceiver(br, IntentFilter().apply {
            addAction(MediaService.ACTION_PLAY)
            addAction(MediaService.ACTION_QUIT)
        })
    }

    private fun unRegisterReceivers() {
        unregisterReceiver(br)
    }

    override fun onBackPressed() {
        if (isFullScreen) {
            binding.fullscreen.callOnClick()
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

    private fun showQualityPopup() {
        qualitySelectPopup?.run {
            if (isShowing) dismiss()
        }

        val items = Quality.values().map {
            it.stringValue
        }.toTypedArray()

        qualitySelectPopup = AlertDialog.Builder(this)
            .setItems(
                items
            ) { _, i ->
                if( Quality.values()[i].intValue != prefs.getQuality()) {
                    //changed
                    binding.quality.text = Quality.values()[i].stringValue
                    prefs.setQuality(Quality.values()[i])
                    serviceRef?.changeQuality()
                }
            }
            .setTitle(
                getString(
                    R.string.popup_title_qualitys,
                    Quality.getStringValue(prefs.getQuality())
                )
            )
            .create().also {
                it.show()
            }
    }

    companion object {
        const val TAG = "PlayerActivity"

        fun startActivity(context: Context, playContent: PlayContent) {
            val intent = Intent(
                context,
                PlayerActivity::class.java
            ).apply { putExtra("playContent", playContent) }

            context.startActivity(intent)
        }
    }
}
