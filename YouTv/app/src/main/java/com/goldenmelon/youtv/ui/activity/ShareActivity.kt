package com.goldenmelon.youtv.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.goldenmelon.youtv.datas.PlayContent
import com.goldenmelon.youtv.datas.PlayUrl
import com.goldenmelon.youtv.utils.SUPPORT_ITAG_LIST
import com.goldenmelon.youtv.utils.SUPPORT_ITAG_ONLY_AUDIO

class ShareActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_share)

        // Note that the Toolbar defined in the layout has the id "toolbar"
        //setSupportActionBar(toolbar)
        //supportActionBar?.setDisplayShowTitleEnabled(false)

        intent.extras?.getString(Intent.EXTRA_TEXT)?.let {
            val list = it.split("/")
            playContent(list.last())
        } ?: finish()
    }

    private fun playContent(videoId: String) {
        val tempContext = this
        //loadingManager.showCenterLoading()
        object : YouTubeExtractor(this) {
            override fun onExtractionComplete(
                ytFiles: SparseArray<YtFile>?,
                vMeta: VideoMeta
            ) {
                var isSuccess = false
                if (ytFiles != null && vMeta != null) {
                    val playUrls = mutableListOf<PlayUrl>()
                    var onlyAudioUrl: String? = null

                    ytFiles.forEach { key, value ->
                        if (SUPPORT_ITAG_LIST.contains(value.format.itag)) {
                            playUrls.add(PlayUrl(value.format.height, value.url))
                        }

                        if (value.format.itag == SUPPORT_ITAG_ONLY_AUDIO) {
                            onlyAudioUrl = value.url
                        }
                    }

                    if (playUrls.isNotEmpty()) {
                        PlayerActivity.startActivity(
                            tempContext, PlayContent(
                                vMeta.videoId,
                                vMeta.title,
                                vMeta.hqImageUrl ?: vMeta.thumbUrl,
                                playUrls,
                                onlyAudioUrl
                            )
                        )
                        isSuccess = true
                    }
                }

                if (!isSuccess) {
                    Toast.makeText(
                        this@ShareActivity,
                        "This content is not supported",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                finish()
            }
        }.extract("https://www.youtube.com/watch?v=$videoId", true, true)
    }
}