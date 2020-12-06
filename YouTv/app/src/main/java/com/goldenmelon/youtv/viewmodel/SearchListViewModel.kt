package com.goldenmelon.youtv.viewmodel

import android.app.Application
import android.os.AsyncTask
import android.webkit.CookieManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.goldenmelon.youtv.datas.Content
import com.goldenmelon.youtv.utils.MAIN_URL
import com.goldenmelon.youtv.utils.SEARCH_URL
import com.goldenmelon.youtv.utils.json.searchContentData
import com.google.gson.GsonBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class SearchListViewModel(application: Application) : AndroidViewModel(application),
    ContentListViewModel {
    override val contents: MutableLiveData<MutableList<Content>> by lazy {
        MutableLiveData<MutableList<Content>>()
    }

    override fun loadContents(param: String?) {
        if (!param.isNullOrBlank()) {
            YoutubeCrawlingTask(param).execute()
        }
    }

    override fun refresh(param: String?) {
        clearContents()
        loadContents(param)
    }

    private inner class YoutubeCrawlingTask(private val keyword: String) :
        AsyncTask<Void, Void, MutableList<Content>>() {

        override fun doInBackground(vararg params: Void?): MutableList<Content>? {
            var list: MutableList<Content>? = contents!!.value
            if (list == null) {
                list = mutableListOf<Content>()
            }

            //video filter
            val connection = Jsoup.connect(SEARCH_URL + keyword + "&sp=EgIQAQ%253D%253D")

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
                if (element.html().contains("var ytInitialData = ")) {
                    val json = element.html().trim()
                        .split("var ytInitialData = ")[1].split("};")[0] + "}"
                    val gson = GsonBuilder().create()
                    val data =
                        gson.fromJson(json, searchContentData::class.java)

                    data.contents.twoColumnSearchResultsRenderer?.let {
                        for (content in it.primaryContents.sectionListRenderer.contents[0].itemSectionRenderer.contents) {
                            content.videoRenderer?.let {
                                var content = Content(it.videoId)
                                if (!list.contains(content)) {
                                    content.thumbnail = it.thumbnail.thumbnails[0].url

                                    if (it.title.simpleText != null) {
                                        content.title = it.title.simpleText
                                    } else {
                                        if (!it.title.runs.isNullOrEmpty()) {
                                            content.title = it.title.runs[0].text
                                        }
                                    }

                                    content.lengthText = "${it.lengthText?.simpleText}"
                                    content.ownerText = it.ownerText.runs[0].text
                                    content.subTitle =
                                        if (it.publishedTimeText?.simpleText != null) "${it.ownerText.runs[0].text} • ${it.viewCountText?.simpleText} • ${it.publishedTimeText?.simpleText}"
                                        else "${it.ownerText.runs[0].text} • ${
                                            it.viewCountText?.runs?.get(
                                                0
                                            )?.text
                                        }${
                                            it.viewCountText?.runs?.let { array ->
                                                if (array.size > 1) array[1].text else ""
                                            }
                                        }"

                                    content.channelThumbnail =
                                        it.channelThumbnailSupportedRenderers.channelThumbnailWithLinkRenderer.thumbnail.thumbnails[0].url

                                    content.channelWebpage =
                                        it.channelThumbnailSupportedRenderers.channelThumbnailWithLinkRenderer.navigationEndpoint.commandMetadata.webCommandMetadata.url

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