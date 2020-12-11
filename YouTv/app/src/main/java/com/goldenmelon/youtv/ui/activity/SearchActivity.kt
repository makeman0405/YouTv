package com.goldenmelon.youtv.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.goldenmelon.youtv.databinding.ActivitySearchBinding
import com.goldenmelon.youtv.service.MediaService
import com.goldenmelon.youtv.ui.activity.base.BaseContentListActivity
import com.goldenmelon.youtv.ui.fragment.ContentListFragment
import com.goldenmelon.youtv.viewmodel.SearchListViewModel

class SearchActivity : BaseContentListActivity(),
    ContentListFragment.OnListFragmentInteractionListener {

    private lateinit var binding: ActivitySearchBinding

    private lateinit var contentListFragment: ContentListFragment

    private var searchWord: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //viewBinding...
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    override fun initUI() {
        super.initUI()
        // Note that the Toolbar defined in the layout has the id "toolbar"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        prefs.getLatestSearchWord().let {
            if (it.isNotBlank()) binding.toolbarSearchView.setQuery(it, false)
        }

        binding.toolbarSearchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (!query.isNullOrBlank()) {
                        if (searchWord.isNullOrBlank() || !searchWord.equals(query)) {
                            prefs.setLatestSearchWord(query)
                            contentListFragment.refreshData()
                            searchWord = query
                        }
                    }

                    binding.toolbarSearchView.clearFocus();
                    return true;
                }
            })
        }

        binding.toolbarPlay.setOnClickListener {
            serviceRef?.let {
                if (it.isPlaying()) {
                    it.pause()
                } else {
                    it.play()
                }
            }
        }

        binding.toolbarBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.toolbarPlay.visibility = if (MediaService.isRunning) {
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
