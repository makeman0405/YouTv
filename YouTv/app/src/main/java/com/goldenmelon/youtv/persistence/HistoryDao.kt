package com.goldenmelon.youtv.persistence

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY date DESC")
    fun getAll(): LiveData<List<History>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(history: History)

    @Update
    fun update(history: History)

    @Delete
    fun delete(history: History)

    @Query("DELETE FROM history WHERE video_id = :videoId")
    fun deleteByVideoId(videoId: String)
}
