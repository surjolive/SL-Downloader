package com.sl.videodownloader.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Insert suspend fun insert(item: DownloadEntity): Long
    @Update suspend fun update(item: DownloadEntity)
    @Delete suspend fun delete(item: DownloadEntity)
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC") fun observeAll(): Flow<List<DownloadEntity>>
    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1") suspend fun getById(id: Long): DownloadEntity?
    @Query("SELECT * FROM downloads WHERE fileName LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun search(query: String): Flow<List<DownloadEntity>>
    @Query("DELETE FROM downloads WHERE status = 'COMPLETED'") suspend fun clearCompleted()
}
