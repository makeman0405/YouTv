package com.goldenmelon.youtv.datas

import android.os.Parcel
import android.os.Parcelable

data class PlayContent(val videoId:String?, val title:String?, val thumbUrl:String?, val url:String?):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(videoId)
        parcel.writeString(title)
        parcel.writeString(thumbUrl)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlayContent> {
        override fun createFromParcel(parcel: Parcel): PlayContent {
            return PlayContent(parcel)
        }

        override fun newArray(size: Int): Array<PlayContent?> {
            return arrayOfNulls(size)
        }
    }
}
