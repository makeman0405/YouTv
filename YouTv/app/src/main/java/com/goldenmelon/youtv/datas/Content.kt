package com.goldenmelon.youtv.datas

data class Content(
    //unique
    var videoId: String,
    var thumbnail: String? = null,
    var title: String? = null,
    var subTitle: String? = null,
    var lengthText: String? = null,
    var ownerText: String? = null,

    var channelThumbnail: String? = null,
    var channelWebpage: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Content) return false
        return videoId == other.videoId
    }

    override fun toString(): String {
        return super.toString()
    }
}