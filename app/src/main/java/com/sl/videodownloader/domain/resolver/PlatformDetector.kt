package com.sl.videodownloader.domain.resolver

import java.net.URI

data class DetectedSource(
    val host: String,
    val platform: String,
    val videoId: String? = null,
    val isDirectMedia: Boolean = false
)

object PlatformDetector {
    fun detect(value: String): DetectedSource? = runCatching {
        val uri = URI(value.trim())
        val host = uri.host?.lowercase()?.removePrefix("www.") ?: return null
        val path = uri.path?.lowercase().orEmpty()
        val platform = when {
            host == "youtube.com" || host == "youtu.be" -> "YouTube"
            host == "facebook.com" || host.endsWith(".facebook.com") -> "Facebook"
            host == "instagram.com" || host.endsWith(".instagram.com") -> "Instagram"
            host == "tiktok.com" || host.endsWith(".tiktok.com") -> "TikTok"
            host == "vimeo.com" || host.endsWith(".vimeo.com") -> "Vimeo"
            mediaExtensions.any { path.endsWith(it) } -> "Direct Media"
            else -> "Unknown source"
        }
        val videoId = uri.getQueryParameter("v") ?: path.trim('/').substringAfterLast('/').takeIf { it.isNotBlank() }
        DetectedSource(host, platform, videoId, platform == "Direct Media")
    }.getOrNull()

    private fun URI.getQueryParameter(name: String): String? = rawQuery?.split('&')
        ?.mapNotNull { part -> part.split('=', limit = 2).takeIf { it.size == 2 } }
        ?.firstOrNull { it[0] == name }?.get(1)

    private val mediaExtensions = setOf(".mp4", ".webm", ".mov", ".m4v", ".ogv")
}
