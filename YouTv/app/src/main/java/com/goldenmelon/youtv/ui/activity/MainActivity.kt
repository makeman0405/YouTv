package com.goldenmelon.youtv.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.goldenmelon.youtv.R
import com.goldenmelon.youtv.service.MediaService
import com.goldenmelon.youtv.ui.activity.base.BaseContentListActivity
import com.goldenmelon.youtv.ui.fragment.ContentListFragment
import com.goldenmelon.youtv.viewmodel.MainListViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseContentListActivity(),
    ContentListFragment.OnListFragmentInteractionListener {

    private lateinit var contentListFragment: ContentListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()
    }

    override fun initUI() {
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
    }

    override fun onResume() {
        super.onResume()
        toolbar_play.visibility = if (MediaService.isRunning) {
            View.VISIBLE
        } else {
            View.INVISIBLE
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

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is ContentListFragment) {
            contentListFragment = fragment
            contentListFragment.viewModel =  ViewModelProviders.of(this).get(MainListViewModel::class.java)
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
                contentListFragment.refreshData()
            }
        }
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
}
