package com.goldenmelon.youtv.datas

import android.os.Parcel
import android.os.Parcelable

data class PlayUrl(
    val quality: Int,
    val url: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(quality)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlayUrl> {
        override fun createFromParcel(parcel: Parcel): PlayUrl {
            return PlayUrl(parcel)
        }

        override fun newArray(size: Int): Array<PlayUrl?> {
            return arrayOfNulls(size)
        }
    }
}
