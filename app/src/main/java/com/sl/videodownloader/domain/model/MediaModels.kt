package com.sl.videodownloader.domain.model

data class FormatInfo(
    val formatId: String,
    val extension: String,
    val mimeType: String,
    val width: Int? = null,
    val height: Int? = null,
    val fps: Double? = null,
    val fileSize: Long? = null,
    val bitrate: Long? = null,
    val videoCodec: String? = null,
    val audioCodec: String? = null,
    val downloadUrl: String,
    val isAudioOnly: Boolean = false,
    val isVideoOnly: Boolean = false
)

data class MediaInfo(
    val id: String,
    val platform: String,
    val title: String,
    val description: String? = null,
    val thumbnail: String? = null,
    val durationMs: Long? = null,
    val fileSize: Long? = null,
    val mimeType: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val fps: Double? = null,
    val availableFormats: List<FormatInfo> = emptyList(),
    val isDownloadAllowed: Boolean
)
