package com.sl.videodownloader.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val fileName: String,
    val fileUri: String? = null,
    val mimeType: String = "video/mp4",
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

enum class DownloadStatus { QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED }
