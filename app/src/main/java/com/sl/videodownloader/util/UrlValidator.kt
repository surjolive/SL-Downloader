package com.sl.videodownloader.util

import java.net.URI

object UrlValidator {
    private val supportedPlatforms = setOf(
        "youtube.com", "youtu.be", "facebook.com", "instagram.com", "tiktok.com", "vimeo.com"
    )

    fun isAuthorizedMediaUrl(value: String): Boolean {
        val trimmed = value.trim()
        return runCatching {
            val uri = URI(trimmed)
            val host = uri.host?.lowercase().orEmpty()
            val supportedExtension = listOf(".mp4", ".webm", ".mov", ".m4v", ".ogv")
                .any { uri.path?.lowercase()?.endsWith(it) == true }
            val blockedPageHost = host == "youtube.com" || host.endsWith(".youtube.com") ||
                host == "youtu.be" || host == "vimeo.com" || host.endsWith(".vimeo.com")
            (uri.scheme == "https" || uri.scheme == "http") && host.isNotBlank() &&
                !blockedPageHost && supportedExtension
        }.getOrDefault(false)
    }

    fun isSupportedDownloadUrl(value: String): Boolean {
        if (isAuthorizedMediaUrl(value)) return true
        return runCatching {
            val uri = URI(value.trim())
            val host = uri.host?.lowercase()?.removePrefix("www.").orEmpty()
            (uri.scheme == "https" || uri.scheme == "http") &&
                supportedPlatforms.any { host == it || host.endsWith(".$it") }
        }.getOrDefault(false)
    }
}

fun String.safeFileName(fallback: String = "video"): String {
    val cleaned = trim().replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), "_")
        .replace(Regex("\\.{2,}"), ".")
        .trim('.', ' ')
    return cleaned.ifBlank { fallback }
}
