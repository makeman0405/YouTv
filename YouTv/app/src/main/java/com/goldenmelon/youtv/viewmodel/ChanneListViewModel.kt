package com.goldenmelon.youtv.viewmodel

import android.app.Application
import android.os.AsyncTask
import android.webkit.CookieManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.goldenmelon.youtv.datas.Content
import com.goldenmelon.youtv.utils.CHANNEL_URL
import com.goldenmelon.youtv.utils.MAIN_URL
import com.goldenmelon.youtv.utils.SEARCH_URL
import com.goldenmelon.youtv.utils.json.contentData
import com.goldenmelon.youtv.utils.json.searchContentData
import com.goldenmelon.youtv.utils.json.thumbnailOverlay
import com.google.gson.GsonBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class ChanneListViewModel(application: Application) : AndroidViewModel(application),
    ContentListViewModel {
    private var contents: MutableLiveData<MutableList<Content>>? = null

    override fun getContents(param: String?): LiveData<MutableList<Content>>? {
        if (contents == null) {
            contents = MutableLiveData()
            loadContents(param)
        }

        return contents
    }

    override fun loadContents(param: String?) {
        if (!param.isNullOrBlank()) {
            YoutubeCrawlingTask(param).execute()
        }
    }

    override fun clearContents() {
        contents!!.value = mutableListOf<Content>()
    }

    private inner class YoutubeCrawlingTask(private val channelWebpage: String) :
        AsyncTask<Void, Void, MutableList<Content>>() {

        override fun doInBackground(vararg params: Void?): MutableList<Content>? {
            var list: MutableList<Content>? = contents!!.value
            if (list == null) {
                list = mutableListOf<Content>()
            }

            //video filter
            val connection = Jsoup.connect(CHANNEL_URL + channelWebpage + "/videos?view=0")

            CookieManager.getInstance().getCookie(MAIN_URL)?.let {
                connection.cookie("Cookie", it)
            }

            val document: Document
            try {
                document = connection.get()
            } catch (e: java.io.IOException) {
                return list
            }

            val elements = document.getElementsByTag("script")

            for (element in elements) {
                if (element.html().contains("window[\"ytInitialData\"] = ")) {
                    val json = element.html().trim()
                        .split("window[\"ytInitialData\"] = ")[1].split(";\n")[0]

                    val gson = GsonBuilder().create()
                    val data =
                        gson.fromJson(json, contentData::class.java)

                    //fatal
                    data.contents?.twoColumnBrowseResultsRenderer?.let {
                        val items =
                            it.tabs[1].tabRenderer.content.sectionListRenderer.contents[0].itemSectionRenderer.contents[0].gridRenderer?.items
                        items?.forEach { content ->
                            content.gridVideoRenderer?.let {
                                val content = Content(it.videoId)
                                if (!list.contains(content)) {
                                    content.thumbnail = it.thumbnail.thumbnails.last().url

                                    if (it.title.simpleText != null) {
                                        content.title = it.title.simpleText
                                    } else {
                                        if (!it.title.runs.isNullOrEmpty()) {
                                            content.title = it.title.runs[0].text
                                        }
                                    }

                                    //content.lengthText = "${it.lengthText?.simpleText}"

                                    content.subTitle =
                                        if (it.publishedTimeText?.simpleText != null) "${it.viewCountText?.simpleText} • ${it.publishedTimeText.simpleText}"
                                        else "${
                                            it.viewCountText?.runs?.get(
                                                0
                                            )?.text
                                        }${
                                            it.viewCountText?.runs?.let { array ->
                                                if (array.size > 1) array[1].text else ""
                                            }
                                        }"


                                    if (!it.thumbnailOverlays.isNullOrEmpty()) {
                                        for (thumbnailOverlay in it.thumbnailOverlays) {
                                            if (thumbnailOverlay.thumbnailOverlayTimeStatusRenderer != null) {
                                                content.lengthText =
                                                    thumbnailOverlay.thumbnailOverlayTimeStatusRenderer.text.simpleText
                                                break
                                            }
                                        }
                                    }

                                    list.add(content)
                                }
                            }
                        }
                    }
                }

            }
            return list
        }

        override fun onPostExecute(list: MutableList<Content>) {
            contents?.value = list
        }
    }

    companion object {
        const val TAG = "SearchContentViewModel"
    }
}