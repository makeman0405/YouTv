package com.goldenmelon.youtv.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.goldenmelon.youtv.R
import com.goldenmelon.youtv.datas.Content
import com.goldenmelon.youtv.service.MediaService
import com.goldenmelon.youtv.ui.activity.base.BaseContentListActivity
import com.goldenmelon.youtv.ui.fragment.ContentListFragment
import com.goldenmelon.youtv.viewmodel.ChannelViewModel
import com.goldenmelon.youtv.viewmodel.SearchContentViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.shortcut
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_main.toolbar_play
import kotlinx.android.synthetic.main.activity_search.*

class ChannelActivity : BaseContentListActivity(),
    ContentListFragment.OnListFragmentInteractionListener {

    private lateinit var channelViewModel: ChannelViewModel

    private var contentListFragment: ContentListFragment? = null

    private val ownerText by lazy {
        intent.getStringExtra("ownerText")
    }

    private val channelWebpage by lazy {
        intent.getStringExtra("channelWebpage")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel)

        // Note that the Toolbar defined in the layout has the id "toolbar"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar_title.text = ownerText

        toolbar_play.setOnClickListener {
            //Toast.makeText(applicationContext, "actionPlay", Toast.LENGTH_SHORT).show()
            serviceRef?.let {
                if (it.isPlaying()) {
                    it.pause()
                } else {
                    it.play()
                }
            }
        }

        toolbar_back.setOnClickListener {
            finish()
        }

        channelViewModel = ViewModelProviders.of(this).get(
            ChannelViewModel::class.java
        )
    }

    override fun onResume() {
        super.onResume()

        toolbar_play.visibility = if (MediaService.isRunning) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    override fun onAttachFragment(fragment: Fragment) {
        when (fragment) {
            is ContentListFragment -> {
                contentListFragment = fragment
                contentListFragment!!.channelWebpage = channelWebpage
            }
        }
    }

    override fun onItemClick(item: Content) {
        playContent(item.videoId)
    }

    override fun onChannelInItemClick(item: Content) {
        // not working
    }

    override fun onReachBottom() {
        // not working
    }

    override fun onUpdated() {
        loadingManager.dismissBottomLoading()
    }

    companion object {
        const val TAG = "ChannelActivity"

        public fun startActivity(context: Context, ownerText: String?, channelWebpage: String?) {
            if (!ownerText.isNullOrBlank() && !channelWebpage.isNullOrBlank()) {
                Intent(
                    context,
                    ChannelActivity::class.java
                ).putExtra("ownerText", ownerText)
                    .putExtra("channelWebpage", channelWebpage).let {
                        context.startActivity(it)
                    }
            }
        }
    }
}