package com.sl.videodownloader.domain.resolver

import com.sl.videodownloader.domain.model.MediaInfo

interface MediaResolver {
    fun canHandle(url: String): Boolean
    suspend fun resolve(url: String): Result<MediaInfo>
    fun getPlatformName(): String
}

interface AuthorizedExtractor {
    fun supports(url: String): Boolean
    suspend fun extract(url: String): Result<MediaInfo>
}
