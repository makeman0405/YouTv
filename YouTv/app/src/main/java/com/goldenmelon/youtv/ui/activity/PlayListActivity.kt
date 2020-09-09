package com.goldenmelon.youtv.ui.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.goldenmelon.youtv.R

class PlayListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_list)
    }

    companion object {
        const val TAG = "PlayListActivity"

        public fun startActivity(context: Context) {
            Intent(
                context,
                PlayListActivity::class.java
            ).let {
                context.startActivity(it)
            }
        }
    }

}