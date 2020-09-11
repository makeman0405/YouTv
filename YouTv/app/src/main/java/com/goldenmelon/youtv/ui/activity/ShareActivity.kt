package com.goldenmelon.youtv.ui.activity

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.SparseArray
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.goldenmelon.youtv.R
import com.goldenmelon.youtv.datas.PlayContent
import com.goldenmelon.youtv.ui.activity.base.BaseContentListActivity
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_share.*


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
                for (itag in BaseContentListActivity.SUPPORT_ITAG_LIST) {
                    if (ytFiles != null && ytFiles[itag] != null) {
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

                if (!isSuccess) {
                    Toast.makeText(
                        this@ShareActivity,
                        "This content is not supported",
                        Toast.LENGTH_SHORT
                    ).show()
                }

//                if (!isSuccess) {
//                    showAlertDialog(
//                        message = getString(R.string.popup_msg_not_supported_content_run_youtube),
//                        positionListener = DialogInterface.OnClickListener { dialog, which ->
//                            Intent(
//                                Intent.ACTION_VIEW,
//                                Uri.parse("https://www.youtube.com/watch?v=$videoId")
//                            )?.also { intent ->
//                                startActivity(
//                                    intent
//                                )
//                            }
//                        })
//                }
//
//                LoadingManager().dismissCenterLoading()
                finish()
            }
        }.extract("https://www.youtube.com/watch?v=$videoId", true, true)
    }
}