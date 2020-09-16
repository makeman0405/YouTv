package com.goldenmelon.youtv.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.goldenmelon.youtv.R
import com.goldenmelon.youtv.datas.Content
import com.goldenmelon.youtv.service.MediaService
import com.goldenmelon.youtv.ui.activity.base.BaseContentListActivity
import com.goldenmelon.youtv.ui.fragment.ContentListFragment
import com.goldenmelon.youtv.viewmodel.SearchContentViewModel
import kotlinx.android.synthetic.main.activity_main.shortcut
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_main.toolbar_play
import kotlinx.android.synthetic.main.activity_search.*


/*
    TODO:
    유투브 메이페이지 HTML 파싱...
    이미지 얻어오기 android glide...
 */

class SearchActivity : BaseContentListActivity(),
    ContentListFragment.OnListFragmentInteractionListener {

    private lateinit var searchViewModel: SearchContentViewModel

    private var contentListFragment: ContentListFragment? = null

    private var searchWord: String? = null

    private val gestureDetector: GestureDetectorCompat by lazy {
        GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return true
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Note that the Toolbar defined in the layout has the id "toolbar"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        prefs.getLatestSearchWord().let {
            if (!it.isBlank()) toolbar_searchView.setQuery(it, false)
        }

        toolbar_searchView?.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrBlank()) {
//                        Toast.makeText(applicationContext, "open windowPopup", Toast.LENGTH_SHORT)
//                            .show()
                        //showSearchedWordListPopup()
                    } else {
//                        Toast.makeText(applicationContext, "close windowPopup", Toast.LENGTH_SHORT)
//                            .show()
                        //dismissSearchedWordListPopup()
                    }
                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (!query.isNullOrBlank()) {
                        if (searchWord.isNullOrBlank() || !searchWord.equals(query)) {
                            searchViewModel.clearContents()
                            searchViewModel.loadContents(query)
                            prefs.setLatestSearchWord(query)
                            searchWord = query
                        }
                    }

                    toolbar_searchView.clearFocus();
                    return true;
                }
            })
        }

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

        searchViewModel = ViewModelProviders.of(this).get(
            SearchContentViewModel::class.java
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
            }
        }
    }

    override fun onItemClick(item: Content) {
//        prefs.getPlayContent()?.let { it ->
//            if (it.videoId == item.videoId) {
//                PlayerActivity.startActivity(SearchActivity@ this, it)
//            } else {
//                playContent(item.videoId)
//            }
//        } ?: playContent(item.videoId)
        playContent(item.videoId)
    }

    //ItemFragment OnListFragmentInteractionListener Callback method
    override fun onChannelInItemClick(item: Content) {
        item.let {
            ChannelActivity.startActivity(this, it.ownerText, it.channelWebpage)
        }
    }

    override fun onReachBottom() {
//not support
//        loadingManager.showBottomLoading()
//        searchViewModel.loadContents("아이유")
    }

    override fun onUpdated() {
        loadingManager.dismissBottomLoading()
    }

    companion object {
        const val TAG = "SearchActivity"

        public fun startActivity(context: Context) {
            Intent(
                context,
                SearchActivity::class.java
            ).let {
                context.startActivity(it)
            }
        }
    }
}