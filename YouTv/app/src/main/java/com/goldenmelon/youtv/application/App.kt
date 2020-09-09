package com.goldenmelon.youtv.application

import android.app.Application
import com.goldenmelon.youtv.preference.Prefs

class App :Application() {
    companion object {
        var prefs:Prefs? = null
    }

    override fun onCreate() {
        prefs = Prefs(this)
        super.onCreate()
    }
}