package com.goldenmelon.youtv.preference

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PointF
import com.goldenmelon.youtv.datas.PlayContent
import com.goldenmelon.youtv.utils.Quality
import com.google.gson.Gson


class Prefs(context: Context) {
    private val sharedPreferences by lazy {
        context.getSharedPreferences("com.goldenmelon.youtv", Context.MODE_PRIVATE)
    }

    fun getPlayContent(): PlayContent? {
        val json = sharedPreferences.getString(PREF_PLAYCONTENT, "")
        if (json.isNullOrBlank()) {
            return null
        }

        return Gson().fromJson(json, PlayContent::class.java)
    }

    fun setPlayContent(playContent: PlayContent?) {
        var json: String? = null

        playContent?.let {
            json = Gson().toJson(it)
        }

        sharedPreferences.edit().putString(PREF_PLAYCONTENT, json ?: "").apply()
    }

    fun getSortCutPosition(): PointF? {
        val json = sharedPreferences.getString(PREF_SHORTCUT_POSITION, "")
        if (json.isNullOrBlank()) {
            return null
        }

        return Gson().fromJson(json, PointF::class.java)
    }

    fun setSortCutPosition(position: PointF) {
        var json: String

        position.let {
            json = Gson().toJson(it)
        }

        sharedPreferences.edit().putString(PREF_SHORTCUT_POSITION, json).apply()
    }

    fun getLatestSearchWord(): String {
        return sharedPreferences.getString(PREF_LATEST_SEARCH_WORD, "")!!
    }

    fun setLatestSearchWord(word: String) {
        sharedPreferences.edit().putString(PREF_LATEST_SEARCH_WORD, word).apply()
    }

    fun getQuality(): Int {
        return sharedPreferences.getInt(PREF_QUALITY, Quality.Q_360P.intValue)
    }

    fun setQuality(quality: Quality) {
        sharedPreferences.edit().putInt(PREF_QUALITY, quality.intValue).apply()
    }

    companion object {
        private const val PREF_PLAYCONTENT = "pref_playcontent"
        private const val PREF_SHORTCUT_POSITION = "pref_shortcut_position"
        private const val PREF_LATEST_SEARCH_WORD = "pref_latest_search_word"
        private const val PREF_QUALITY = "pref_quality"
    }
}
