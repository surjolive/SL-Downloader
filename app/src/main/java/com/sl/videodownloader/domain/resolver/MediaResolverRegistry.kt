package com.sl.videodownloader.domain.resolver

import com.sl.videodownloader.domain.model.MediaInfo

class MediaResolverRegistry(resolvers: List<MediaResolver> = emptyList()) {
    private val resolvers = resolvers.toMutableList()

    fun register(resolver: MediaResolver) {
        if (resolver !in resolvers) resolvers += resolver
    }

    fun matching(url: String): MediaResolver? = resolvers.firstOrNull { it.canHandle(url) }

    suspend fun resolve(url: String): Result<MediaInfo> = matching(url)?.resolve(url)
        ?: Result.failure(ResolverError(ResolverErrorCode.UNSUPPORTED_SOURCE, "This source is not supported."))
}
