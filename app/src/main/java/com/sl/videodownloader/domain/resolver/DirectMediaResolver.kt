package com.sl.videodownloader.domain.resolver

import com.sl.videodownloader.domain.model.FormatInfo
import com.sl.videodownloader.domain.model.MediaInfo
import com.sl.videodownloader.util.UrlValidator
import java.net.URI

class DirectMediaResolver : MediaResolver {
    override fun canHandle(url: String): Boolean = UrlValidator.isAuthorizedMediaUrl(url)

    override suspend fun resolve(url: String): Result<MediaInfo> = runCatching {
        if (!canHandle(url)) throw ResolverError(ResolverErrorCode.INVALID_URL, "Enter a valid direct media URL.")
        val uri = URI(url.trim())
        val extension = uri.path.substringAfterLast('.', "mp4").lowercase()
        val mime = when (extension) {
            "webm" -> "video/webm"
            "mov" -> "video/quicktime"
            "m4v" -> "video/x-m4v"
            "ogv" -> "video/ogg"
            else -> "video/mp4"
        }
        val title = uri.path.substringAfterLast('/').ifBlank { "video.$extension" }
        val format = FormatInfo("direct", extension, mime, downloadUrl = url.trim())
        MediaInfo(title, "Direct Media", title, mimeType = mime, availableFormats = listOf(format), isDownloadAllowed = true)
    }

    override fun getPlatformName(): String = "Direct Media"
}
