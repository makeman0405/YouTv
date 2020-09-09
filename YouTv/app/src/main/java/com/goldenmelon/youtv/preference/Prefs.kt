package com.goldenmelon.youtv.preference

import android.content.Context
import android.graphics.PointF
import com.goldenmelon.youtv.datas.PlayContent
import com.google.gson.Gson

class Prefs(context: Context) {
    private val sharedPreferences by lazy {
        context.getSharedPreferences("com.goldenmelon.youtv", Context.MODE_PRIVATE)
    }

    public fun getPlayContent(): PlayContent? {
        val json = sharedPreferences.getString(PREF_PLAYCONTENT, "")
        if (json.isNullOrBlank()) {
            return null
        }

        return Gson().fromJson(json, PlayContent::class.java)
    }

    public fun setPlayContent(playContent: PlayContent?) {
        var json: String? = null

        playContent?.let {
            json = Gson().toJson(it)
        }

        sharedPreferences.edit().putString(PREF_PLAYCONTENT, json ?: "").apply()
    }

    public fun getSortCutPosition(): PointF? {
        val json = sharedPreferences.getString(PREF_SHORTCUT_POSITION, "")
        if (json.isNullOrBlank()) {
            return null
        }

        return Gson().fromJson(json, PointF::class.java)
    }

    public fun setSortCutPosition(position: PointF) {
        var json: String

        position.let {
            json = Gson().toJson(it)
        }

        sharedPreferences.edit().putString(PREF_SHORTCUT_POSITION, json).apply()
    }

    public fun getLatestSearchWord(): String {
        return sharedPreferences.getString(PREF_LATEST_SEARCH_WORD, "")!!
    }

    public fun setLatestSearchWord(word: String) {
        sharedPreferences.edit().putString(PREF_LATEST_SEARCH_WORD, word).apply()
    }

    companion object {
        private const val PREF_PLAYCONTENT = "pref_playcontent"
        private const val PREF_SHORTCUT_POSITION = "pref_shortcut_position"
        private const val PREF_LATEST_SEARCH_WORD = "pref_latest_search_word"
    }
}