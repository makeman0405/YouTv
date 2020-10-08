package com.goldenmelon.youtv.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.os.*
import com.google.android.exoplayer2.audio.AudioAttributes

import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.NotificationTarget
import com.goldenmelon.youtv.R
import com.goldenmelon.youtv.application.App
import com.goldenmelon.youtv.datas.PlayContent
import com.goldenmelon.youtv.preference.Prefs
import com.goldenmelon.youtv.ui.activity.PlayerActivity
import com.goldenmelon.youtv.utils.isNetworkAvailable
import com.goldenmelon.youtv.utils.isWIFIConnected
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import kotlin.properties.Delegates
import kotlin.reflect.KProperty


class MediaService : Service() {
    private var player: SimpleExoPlayer? = null

    private val binder = MediaBinder()

    //preference
    val prefs: Prefs by lazy {
        App.prefs!!
    }

    private var currentPlayContent:
            PlayContent? by Delegates.observable(null,
        { _: KProperty<*>, _: PlayContent?, newValue: PlayContent? ->
            prefs.setPlayContent(newValue)
        })

    private val br = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_REWIND -> {
                    rewind()
                }

                ACTION_PLAY -> {
                    if (isPlaying()) {
                        pause()
                    } else {
                        play()
                    }

                    startForeground(
                        1, createControlBoxNotification()
                    )
                }

                ACTION_FASTFORWORD -> {
                    fastforword()
                }

                ACTION_QUIT -> {
                    isRunning = false
                    prefs.setPlayContent(null)
                    stopSelf()
                }

                Intent.ACTION_HEADSET_PLUG -> {
                    val state = intent.getIntExtra("state", -1);
                    if (state == 0 && isPlaying()) pause()
                }

            }

        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Handler(Looper.getMainLooper()).post { player?.retry() }
        }

        override fun onLost(network: Network?) {
           //nothing
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        initializePlayer()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //create channel
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "ControlBox",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                vibrationPattern = longArrayOf(0L)
                enableVibration(true)
                setShowBadge(false)
                setSound(null, null)
            }

            getSystemService(NotificationManager::class.java).createNotificationChannel(
                serviceChannel
            )
        }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        NotificationCompat.Builder(this, CHANNEL_ID).setVibrate(longArrayOf(0L))
            .setSound(null).priority =
            NotificationCompat.PRIORITY_DEFAULT

        //startForeground
        startForeground(
            1, notificationBuilder
                .setContentTitle("MediaService")
                .setContentText("Started...")
                .setSmallIcon(R.drawable.ic_notification)
                .build()
        )

        registerReceivers()
        registerNetworkCallback()
    }

    private fun registerReceivers() {
        registerReceiver(br, IntentFilter().apply {
            addAction(ACTION_REWIND)
            addAction(ACTION_PLAY)
            addAction(ACTION_FASTFORWORD)
            addAction(ACTION_QUIT)
            addAction(Intent.ACTION_HEADSET_PLUG)
        })
    }

    private fun registerNetworkCallback() {
        val cm = getSystemService(ConnectivityManager::class.java)
        val wifiNetworkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        cm.registerNetworkCallback(wifiNetworkRequest, networkCallback)
    }

    private fun unregisterNetworkCallback() {
        val cm = getSystemService(ConnectivityManager::class.java)
        cm.unregisterNetworkCallback(networkCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY // START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        //first time run
        return binder
    }

    override fun onDestroy() {
        isRunning = false
        releasePlayer()
        unregisterReceiver(br)
        unregisterNetworkCallback()
        prefs.setPlayContent(null)
        super.onDestroy()
    }

    private fun initializePlayer() {
//        var trackSelector = DefaultTrackSelector().apply {
//            setParameters(buildUponParameters().setMaxVideoSizeSd())
//        }

        player = SimpleExoPlayer.Builder(this.applicationContext)
            .build().apply {
                addListener(object : Player.EventListener {
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        startForeground(1, createControlBoxNotification())
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                    }

                    override fun onLoadingChanged(isLoading: Boolean) {
                        super.onLoadingChanged(isLoading)
                    }

                    override fun onPlayerError(error: ExoPlaybackException) {
                        super.onPlayerError(error)

                        if(isNetworkAvailable(applicationContext)) {
                            Toast.makeText(
                                applicationContext,
                                "Playback Error: An error has occurred(${error.type})",
                                Toast.LENGTH_LONG
                            ).show()

                            sendBroadcast(Intent(ACTION_QUIT))
                        } else {
                            Toast.makeText(
                                applicationContext,
                                R.string.popup_msg_please_check_the_network,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(C.CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build(), true
                )
            }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val defaultHttpDataSource =
            DefaultHttpDataSourceFactory("exoplayer-codelab", 15000, 15000, false)

        return ProgressiveMediaSource.Factory(defaultHttpDataSource)
            .createMediaSource(uri)
        //return buildMediaSource(uri)
    }

    private fun releasePlayer() {
        player?.let {
            it.release()
            player = null
        }
    }

    //control
    public fun load(playContent: PlayContent?) {
        // load
        if (currentPlayContent?.videoId.equals(playContent?.videoId)) {
            //no playing
            if (!isPlaying()) {
                //url
                player?.prepare(buildMediaSource(Uri.parse(playContent?.url)), false, true)

                // playing
                play()

                //update
                currentPlayContent = playContent
            }
        } else {
            //url
            player?.let {
                //it.seekTo(0, 0L)
                it.prepare(buildMediaSource(Uri.parse(playContent?.url)), true, true)
            }

            //play
            play()

            //update
            currentPlayContent = playContent
        }
    }

    private fun createControlBoxNotification(): Notification? {
        // Create an Intent for the activity you want to start
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("playContent", currentPlayContent)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val remoteViews = RemoteViews(packageName, R.layout.notification_control_box).apply {
            setTextViewText(R.id.title, currentPlayContent?.title)
            player?.let {
                setImageViewResource(
                    R.id.play,
                    if (isPlaying()) {
                        R.drawable.exo_notification_pause
                    } else {
                        if (it.playbackState != ExoPlayer.STATE_ENDED)
                            R.drawable.exo_notification_play
                        else R.drawable.baseline_refresh_white_24
                    }
                )

                //Main PLAY UI UPDATE
                sendBroadcast(
                    Intent(ACTION_UPDATE_PLAY_UI).putExtra(
                        "isPlaying",
                        isPlaying()
                    )
                )
            }

            setOnClickPendingIntent(
                R.id.rewind,
                PendingIntent.getBroadcast(
                    applicationContext,
                    0,
                    Intent(ACTION_REWIND),
                    0
                )
            )

            setOnClickPendingIntent(
                R.id.play,
                PendingIntent.getBroadcast(
                    applicationContext,
                    0,
                    Intent(ACTION_PLAY),
                    0
                )
            )

            setOnClickPendingIntent(
                R.id.fastforward,
                PendingIntent.getBroadcast(
                    applicationContext,
                    0,
                    Intent(ACTION_FASTFORWORD),
                    0
                )
            )

            setOnClickPendingIntent(
                R.id.close,
                PendingIntent.getBroadcast(
                    applicationContext,
                    0,
                    Intent(ACTION_QUIT),
                    0
                )
            )
        }

        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle(currentPlayContent?.title)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0L))
                .setSound(null)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCustomContentView(remoteViews)
                .build()

        Glide
            .with(applicationContext)
            .asBitmap()
            .load(currentPlayContent?.thumbUrl)
            .apply(RequestOptions().centerCrop())
            .into(
                NotificationTarget(
                    this, R.id.icon, remoteViews
                    , notification, 1
                )
            )

        return notification
    }


    public fun getPlayer(): SimpleExoPlayer? {
        return player
    }

    public fun isPlaying(): Boolean {
        var result = false
        player?.let {
            result = it.playWhenReady && (it.playbackState != ExoPlayer.STATE_IDLE && it.playbackState != ExoPlayer.STATE_ENDED)
        }
        return result
    }

    public fun rewind() {
        player?.let {
            var positionMs: Long = it.currentPosition - 5000
            it.seekTo(if (positionMs >= 0) positionMs else 0)
        }
    }

    public fun play() {
        player?.let {
            if (it.playbackState == ExoPlayer.STATE_ENDED) {
                it.seekTo(0, 0L)
            }
            it.playWhenReady = true
        }
    }

    public fun fastforword() {
        player?.let {
            var positionMs: Long = it.currentPosition + 5000
            it.seekTo(if (positionMs <= it.contentDuration) positionMs else it.contentDuration)
        }
    }

    public fun pause() {
        player?.playWhenReady = false
    }

    public inner class MediaBinder : Binder() {
        fun getService(): MediaService {
            return this@MediaService
        }
    }

    companion object {
        const val TAG = "MediaService"
        const val CHANNEL_ID = "ForegroundServiceChannel"

        const val ACTION_REWIND = "chutube.intent.action.YOUTUBE_REWIND"
        const val ACTION_PLAY = "chutube.intent.action.YOUTUBE_PLAY"
        const val ACTION_FASTFORWORD = "chutube.intent.action.YOUTUBE_FASTFORWORD"

        const val ACTION_QUIT = "chutube.intent.action.YOUTUBE_QUIT"

        const val ACTION_UPDATE_PLAY_UI = "chutube.intent.action.YOUTUBE_UPDATE_PLAY_UI"

        var isRunning = false
            private set
    }
}
