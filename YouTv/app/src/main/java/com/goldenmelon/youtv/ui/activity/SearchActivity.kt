package com.goldenmelon.youtv.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.goldenmelon.youtv.R
import com.goldenmelon.youtv.datas.Content
import com.goldenmelon.youtv.service.MediaService
import com.goldenmelon.youtv.ui.activity.base.BaseContentListActivity
import com.goldenmelon.youtv.ui.fragment.ContentListFragment
import com.goldenmelon.youtv.viewmodel.SearchListViewModel
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_main.toolbar_play
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : BaseContentListActivity(),
    ContentListFragment.OnListFragmentInteractionListener {

    private lateinit var contentListFragment: ContentListFragment

    private var searchWord: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initUI()
    }

    override fun initUI() {
        // Note that the Toolbar defined in the layout has the id "toolbar"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        prefs.getLatestSearchWord().let {
            if (it.isNotBlank()) toolbar_searchView.setQuery(it, false)
        }

        toolbar_searchView?.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (!query.isNullOrBlank()) {
                        if (searchWord.isNullOrBlank() || !searchWord.equals(query)) {
                            prefs.setLatestSearchWord(query)
                            contentListFragment.refreshViewModel()
                            searchWord = query
                        }
                    }

                    toolbar_searchView.clearFocus();
                    return true;
                }
            })
        }

        toolbar_play.setOnClickListener {
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
                contentListFragment.viewModel =  ViewModelProviders.of(this).get(SearchListViewModel::class.java)
            }
        }
    }

    companion object {
        const val TAG = "SearchActivity"

        fun startActivity(context: Context) {
            context.startActivity(
                Intent(
                    context,
                    SearchActivity::class.java
                )
            )
        }
    }
}
