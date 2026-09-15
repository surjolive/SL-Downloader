package com.sl.videodownloader.util

import kotlin.math.roundToLong

fun progressPercent(downloadedBytes: Long, totalBytes: Long): Int =
    if (totalBytes <= 0) 0 else ((downloadedBytes.toDouble() / totalBytes) * 100).coerceIn(0.0, 100.0).roundToLong().toInt()

fun etaSeconds(downloadedBytes: Long, totalBytes: Long, bytesPerSecond: Long): Long? =
    if (bytesPerSecond <= 0 || totalBytes <= downloadedBytes) null
    else ((totalBytes - downloadedBytes).toDouble() / bytesPerSecond).roundToLong()
