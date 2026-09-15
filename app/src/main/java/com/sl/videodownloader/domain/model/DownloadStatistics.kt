package com.sl.videodownloader.domain.model

import com.sl.videodownloader.data.database.DownloadEntity
import com.sl.videodownloader.data.database.DownloadStatus

fun List<DownloadEntity>.statistics(): DownloadStatistics = DownloadStatistics(
    total = size,
    completed = count { it.status == DownloadStatus.COMPLETED },
    failed = count { it.status == DownloadStatus.FAILED },
    cancelled = count { it.status == DownloadStatus.CANCELLED },
    totalBytes = filter { it.status == DownloadStatus.COMPLETED }.sumOf { it.downloadedBytes }
)

data class DownloadStatistics(
    val total: Int,
    val completed: Int,
    val failed: Int,
    val cancelled: Int,
    val totalBytes: Long
)
