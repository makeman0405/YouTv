package com.goldenmelon.youtv.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.goldenmelon.youtv.datas.Content
import java.util.* //Date

@Entity
data class History private constructor(
    @PrimaryKey
    @ColumnInfo(name = "video_id") val videoId: String,

    @ColumnInfo(name = "thumbnail") val thumbnail: String?,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "sub_title") val subTitle: String? = null,
    @ColumnInfo(name = "length_text") val lengthText: String? = null,
    @ColumnInfo(name = "owner_text") val ownerText: String? = null,
    @ColumnInfo(name = "date") val date: Date? = null
) {
    companion object {
        fun create(content: Content, date: Date): History {
            return History(
                content.videoId,
                content.thumbnail,
                content.title,
                content.subTitle,
                content.lengthText,
                content.ownerText,
                date
            )
        }
    }
}
