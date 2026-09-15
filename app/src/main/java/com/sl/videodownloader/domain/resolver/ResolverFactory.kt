package com.sl.videodownloader.domain.resolver

object ResolverFactory {
    fun createDefault(): MediaResolverRegistry = MediaResolverRegistry().apply {
        register(DirectMediaResolver())
        register(AuthorizedPlatformResolver())
        register(GenericWebMediaResolver())
    }
}
