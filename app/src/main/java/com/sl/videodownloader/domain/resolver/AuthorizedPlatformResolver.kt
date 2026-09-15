package com.sl.videodownloader.domain.resolver

import com.sl.videodownloader.domain.model.MediaInfo

class AuthorizedPlatformResolver(
    private val extractor: AuthorizedExtractor? = null
) : MediaResolver {
    override fun canHandle(url: String): Boolean = extractor?.supports(url) == true

    override suspend fun resolve(url: String): Result<MediaInfo> {
        val activeExtractor = extractor
            ?: return Result.failure(ResolverError(ResolverErrorCode.DOWNLOAD_NOT_PERMITTED, "This video cannot be downloaded through an authorized method."))
        return activeExtractor.extract(url).mapCatching { info ->
            if (!info.isDownloadAllowed) throw ResolverError(ResolverErrorCode.DOWNLOAD_NOT_PERMITTED, "This video cannot be downloaded through an authorized method.")
            info
        }
    }

    override fun getPlatformName(): String = "Authorized Platform"
}
