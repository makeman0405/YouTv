package com.goldenmelon.youtv.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.goldenmelon.youtv.R
import com.goldenmelon.youtv.datas.Content
import com.goldenmelon.youtv.service.MediaService
import com.goldenmelon.youtv.ui.activity.base.BaseContentListActivity
import com.goldenmelon.youtv.ui.fragment.ContentListFragment
import com.goldenmelon.youtv.viewmodel.ContentViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseContentListActivity(),
    ContentListFragment.OnListFragmentInteractionListener {

    private lateinit var contentViewModel: ContentViewModel

    private var contentListFragment: ContentListFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Note that the Toolbar defined in the layout has the id "toolbar"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar_play.setOnClickListener {
            serviceRef?.let {
                if (it.isPlaying()) {
                    it.pause()
                } else {
                    it.play()
                }
            }
        }

        contentViewModel = ViewModelProviders.of(this).get(
            ContentViewModel::
            class.java
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
        if (fragment is ContentListFragment) {
            contentListFragment = fragment
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                contentViewModel.let {
                    it.clearContents()
                    it.loadContents()
                }
            }
        }
    }

    //ItemFragment OnListFragmentInteractionListener Callback method
    override fun onItemClick(item: Content) {
        playContent(item.videoId)
    }

    override fun onChannelInItemClick(item: Content) {
        item.let {
            ChannelActivity.startActivity(this, it.ownerText, it.channelWebpage)
        }
    }

    override fun onReachBottom() {
        loadingManager.showBottomLoading()
        contentViewModel.loadContents()
    }

    override fun onUpdated() {
        loadingManager.dismissBottomLoading()
    }

    override fun onMenuInItemClick(v: View, item: Content) {
        showListItemMenu(v, item)
    }

    companion object {
        const val TAG = "MainActivity"
        const val LOGIN_REQUEST_CODE = 1001

        fun startActivity(context: Context) {
            Intent(
                context,
                MainActivity::class.java
            ).let {
                context.startActivity(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                SearchActivity.startActivity(this)
                return true
            }

            R.id.action_login -> {
                LoginActivity.startActivityForResult(this, LOGIN_REQUEST_CODE)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
