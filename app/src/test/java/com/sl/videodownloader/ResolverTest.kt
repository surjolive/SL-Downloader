package com.sl.videodownloader

import com.sl.videodownloader.domain.resolver.DirectMediaResolver
import com.sl.videodownloader.domain.resolver.MediaResolverRegistry
import com.sl.videodownloader.domain.resolver.PlatformDetector
import com.sl.videodownloader.domain.resolver.ResolverErrorCode
import com.sl.videodownloader.util.etaSeconds
import com.sl.videodownloader.util.progressPercent
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolverTest {
    @Test fun detectsPlatformsWithoutGrantingPermission() {
        assertEquals("YouTube", PlatformDetector.detect("https://www.youtube.com/watch?v=abc")?.platform)
        assertEquals("TikTok", PlatformDetector.detect("https://www.tiktok.com/@creator/video/123")?.platform)
        assertEquals("Instagram", PlatformDetector.detect("https://www.instagram.com/reel/abc")?.platform)
        assertEquals("Direct Media", PlatformDetector.detect("https://cdn.example.com/clip.mp4")?.platform)
    }

    @Test fun registryResolvesDirectMediaOnly() = runBlocking {
        val result = MediaResolverRegistry(listOf(DirectMediaResolver())).resolve("https://cdn.example.com/clip.webm")
        assertTrue(result.isSuccess)
        assertEquals("Direct Media", result.getOrThrow().platform)
    }

    @Test fun metricsRemainBounded() {
        assertEquals(50, progressPercent(50, 100))
        assertEquals(5L, etaSeconds(50, 100, 10))
        assertNotNull(ResolverErrorCode.DOWNLOAD_NOT_PERMITTED)
    }
}
