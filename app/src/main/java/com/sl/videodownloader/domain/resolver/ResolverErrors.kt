package com.sl.videodownloader.domain.resolver

enum class ResolverErrorCode {
    INVALID_URL,
    UNSUPPORTED_SOURCE,
    DOWNLOAD_NOT_PERMITTED,
    NETWORK_ERROR,
    TIMEOUT,
    HTTP_ERROR,
    STORAGE_ERROR,
    RESUME_NOT_SUPPORTED,
    MEDIA_ERROR,
    UNKNOWN_ERROR
}

data class ResolverError(
    val code: ResolverErrorCode,
    val userMessage: String,
    val rootCause: Throwable? = null
) : Exception(userMessage, rootCause)
