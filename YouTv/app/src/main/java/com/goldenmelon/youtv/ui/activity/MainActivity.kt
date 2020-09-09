package com.goldenmelon.youtv.ui.activity

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.goldenmelon.youtv.*
import com.goldenmelon.youtv.datas.Content
import com.goldenmelon.youtv.service.MediaService
import com.goldenmelon.youtv.ui.activity.base.BaseContentListActivity
import com.goldenmelon.youtv.ui.fragment.ContentListFragment
import com.goldenmelon.youtv.viewmodel.ContentViewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*

/*
    TODO:
    유투브 메이페이지 HTML 파싱...
    이미지 얻어오기 android glide...
 */

class MainActivity : BaseContentListActivity(),
    ContentListFragment.OnListFragmentInteractionListener {

    private lateinit var contentViewModel: ContentViewModel

    private var contentListFragment: ContentListFragment? = null

    private lateinit var interstitialAd: InterstitialAd

    private val gestureDetector: GestureDetectorCompat by lazy {
        GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return true
                //return super.onSingleTapUp(e)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Note that the Toolbar defined in the layout has the id "toolbar"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // init fullScreen Ad
        MobileAds.initialize(this) {}
        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        interstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                interstitialAd.loadAd(AdRequest.Builder().build())
            }
        }
        interstitialAd.loadAd(AdRequest.Builder().build())

        toolbar_donate.setOnClickListener {
            if (interstitialAd.isLoaded) {
                interstitialAd.show()
            } else {
                //Log.d("TAG", "The interstitial wasn't loaded yet.")
            }
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


//        val screenSize = Point()
//        windowManager.defaultDisplay.getSize(screenSize);
//        Log.d(TAG, "screenSize: ${screenSize.toString()}")

        shortcut.setOnTouchListener { _, event ->
            if (gestureDetector.onTouchEvent(event)) {
//                prefs.getPlayContent()?.let {
//                    PlayerActivity.startActivity(SearchActivity@ this, it)
//                }
                prefs.getPlayContent()?.videoId?.let {
                    playContent(it)
                }
            }
//            else {
//                when (event.action) {
//                    MotionEvent.ACTION_MOVE -> {
//                        v.x = v.x + (event.x) - (v.width /2);
//                        v.y = v.y + (event.y) - (v.height /2);
//                    }
//                    MotionEvent.ACTION_UP -> {
//                        prefs.setSortCutPosition(PointF(v.x,v.y))
//                    }
//                }
//            }

            return@setOnTouchListener true
        }

        contentViewModel = ViewModelProviders.of(this).get(
            ContentViewModel::
            class.java
        )
        contentViewModel.getLoginUrl()!!.observe(this, Observer
        {
            invalidateOptionsMenu()
        })
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

    override fun onItemClick(item: Content) {
//        prefs.getPlayContent()?.let { it ->
//            if (it.videoId == item.videoId) {
//                PlayerActivity.startActivity(MainActivity@ this, it)
//            } else {
//                playContent(item.videoId)
//            }
//        } ?: playContent(item.videoId)

        playContent(item.videoId)
    }

    override fun onChannelInItemClick(item: Content) {
        item.let {
                ChannelActivity.startActivity(this, it.ownerText, it.channelWebpage)
        }
    }

    //ItemFragment OnListFragmentInteractionListener Callback method
    override fun onReachBottom() {
        loadingManager.showBottomLoading()
        contentViewModel.loadContents()
    }

    override fun onUpdated() {
        loadingManager.dismissBottomLoading()
    }

    companion object {
        const val TAG = "MainActivity"
        const val LOGIN_REQUEST_CODE = 1001

        public fun startActivity(context: Context) {
            Intent(
                context,
                MainActivity::class.java
            ).let {
                context.startActivity(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        /* Login Item */
        menu.findItem(R.id.action_login).isVisible =
            contentViewModel.getLoginUrl()!!.value != null
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
//            R.id.action_play_list -> {
//                PlayListActivity.startActivity(this)
//                true
//            }

            R.id.action_search -> {
                SearchActivity.startActivity(this)
                true
            }

            R.id.action_login -> {
                //Toast.makeText(applicationContext, "Try login", Toast.LENGTH_SHORT).show()
                startActivityForResult(
                    Intent(
                        this,
                        LoginActivity::class.java
                        // todo change login url
                        // test
                    ).putExtra("url", contentViewModel.getLoginUrl()!!.value),
                    LOGIN_REQUEST_CODE
                )

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}