package com.sl.videodownloader.domain.resolver

import com.sl.videodownloader.domain.model.MediaInfo

class GenericWebMediaResolver : MediaResolver {
    override fun canHandle(url: String): Boolean = PlatformDetector.detect(url)?.isDirectMedia == false

    override suspend fun resolve(url: String): Result<MediaInfo> = Result.failure(
        ResolverError(ResolverErrorCode.DOWNLOAD_NOT_PERMITTED, "This video cannot be downloaded through an authorized method.")
    )

    override fun getPlatformName(): String = "Web Source"
}
