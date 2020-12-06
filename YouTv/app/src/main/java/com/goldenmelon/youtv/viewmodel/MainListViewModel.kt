package com.goldenmelon.youtv.viewmodel

import android.app.Application
import android.os.AsyncTask
import android.webkit.CookieManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.goldenmelon.youtv.datas.Content
import com.goldenmelon.youtv.utils.MAIN_URL
import com.goldenmelon.youtv.utils.json.contentData
import com.google.gson.GsonBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class MainListViewModel(application: Application) : AndroidViewModel(application),
    ContentListViewModel {
    override val contents: MutableLiveData<MutableList<Content>> by lazy {
        MutableLiveData<MutableList<Content>>()
    }

    override fun loadContents(param: String?) {
        YoutubeCrawlingTask().execute()
    }

    override fun refresh(param: String?) {
        clearContents()
        loadContents()
    }

    private inner class YoutubeCrawlingTask() :
        AsyncTask<Void, Void, MutableList<Content>>() {

        override fun doInBackground(vararg params: Void?): MutableList<Content> {
            var list: MutableList<Content>? = contents.value
            if (list == null) {
                list = mutableListOf<Content>()
            }

            val connection = Jsoup.connect(MAIN_URL)
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
                var script = element.html()
                if (script.contains("var ytInitialData = ")) {
                    var json = script.trim()
                        .split("var ytInitialData = ")[1].split("};")[0]
                    json+="}"

                    val gson = GsonBuilder().create()
                    val data =
                        gson.fromJson(json, contentData::class.java)

                    data.contents?.twoColumnBrowseResultsRenderer?.let {
                        for (content in it.tabs[0].tabRenderer.content.richGridRenderer.contents) {
                            content.richItemRenderer?.content?.videoRenderer?.let {
                                var tempContent = Content(it.videoId)
                                if (!list.contains(tempContent)) {
                                    tempContent.thumbnail = it.thumbnail.thumbnails[0].url

                                    if (it.title.simpleText != null) {
                                        tempContent.title = it.title.simpleText
                                    } else {
                                        if (!it.title.runs.isNullOrEmpty()) {
                                            tempContent.title = it.title.runs[0].text
                                        }
                                    }

                                    tempContent.lengthText = "${it.lengthText?.simpleText}"
                                    tempContent.ownerText = it.ownerText.runs[0].text
                                    tempContent.subTitle =
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

                                    tempContent.channelThumbnail =
                                        it.channelThumbnailSupportedRenderers.channelThumbnailWithLinkRenderer.thumbnail.thumbnails[0].url
                                    tempContent.channelWebpage =
                                        it.channelThumbnailSupportedRenderers.channelThumbnailWithLinkRenderer.navigationEndpoint.commandMetadata.webCommandMetadata.url

                                    list.add(tempContent)
                                }
                            }
                        }
                    }
                    break;
                }
            }

            return list
        }

        override fun onPostExecute(list: MutableList<Content>) {
            contents.value = list
        }
    }

    companion object {
        const val TAG = "ContentViewModel"
    }
}