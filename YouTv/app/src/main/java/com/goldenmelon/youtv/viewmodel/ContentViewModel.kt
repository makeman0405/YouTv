package com.goldenmelon.youtv.viewmodel

import android.app.Application
import android.os.AsyncTask
import android.webkit.CookieManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.goldenmelon.youtv.datas.Content
import com.goldenmelon.youtv.utils.MAIN_URL
import com.goldenmelon.youtv.utils.json.contentData
import com.goldenmelon.youtv.utils.json.loginData
import com.goldenmelon.youtv.utils.json.tab
import com.google.gson.GsonBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class ContentViewModel(application: Application) : AndroidViewModel(application) {
    private var loginUrl: MutableLiveData<String>? = null
    private var contents: MutableLiveData<MutableList<Content>>? = null

    public fun getLoginUrl(): LiveData<String>? {
        if (loginUrl == null) {
            loginUrl = MutableLiveData()
        }
        return loginUrl
    }

    public fun getContents(): LiveData<MutableList<Content>>? {
        if (contents == null) {
            contents = MutableLiveData()
            loadContents()
        }

        return contents
    }

    public fun loadContents() {
        if (contents != null) {
            YoutubeCrawlingTask().execute()
        }
    }

    public fun clearContents() {
        contents!!.value = mutableListOf<Content>()
    }

    private inner class YoutubeCrawlingTask() :
        AsyncTask<Void, Void, MutableList<Content>>() {
        var tempLoginUrl: String? = null

        override fun doInBackground(vararg params: Void?): MutableList<Content>? {
            var list: MutableList<Content>? = contents!!.value
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
                if (element.html().contains("var ytInitialGuideData = ")) {
                    val json = element.html().trim()
                        .split("var ytInitialGuideData = ")[1].split(";\n")[0]
                    val gson = GsonBuilder().create()
                    val data =
                        gson.fromJson(json, loginData::class.java)

                    tempLoginUrl = null
                    for (item in data.items) {
                        var url =
                            item.guideSigninPromoRenderer?.signInButton?.buttonRenderer?.navigationEndpoint?.commandMetadata?.webCommandMetadata?.url
                        url?.let {
                            tempLoginUrl = url
                        }
                    }
                } else if (element.html().contains("window[\"ytInitialData\"] = ")) {
                    val json = element.html().trim()
                        .split("window[\"ytInitialData\"] = ")[1].split(";\n")[0]

                    val gson = GsonBuilder().create()
                    val data =
                        gson.fromJson(json, contentData::class.java)

                    //fatal
                    data.contents?.twoColumnBrowseResultsRenderer?.let {
                        for (content in it.tabs[0].tabRenderer.content.richGridRenderer.contents) {
                            content.richItemRenderer?.content?.videoRenderer?.let {
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
            loginUrl?.value = tempLoginUrl
        }
    }

    companion object {
        const val TAG = "ContentViewModel"
    }
}