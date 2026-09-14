package com.sl.videodownloader

import com.sl.videodownloader.util.UrlValidator
import com.sl.videodownloader.util.safeFileName
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlValidatorTest {
    @Test fun acceptsHttpAndHttpsUrls() {
        assertTrue(UrlValidator.isAuthorizedMediaUrl("https://example.com/video.mp4"))
        assertTrue(UrlValidator.isAuthorizedMediaUrl("http://example.com/video.webm"))
    }

    @Test fun rejectsNonHttpUrls() {
        assertFalse(UrlValidator.isAuthorizedMediaUrl("javascript:alert(1)"))
        assertFalse(UrlValidator.isAuthorizedMediaUrl("not a url"))
    }

    @Test fun sanitizesPathSeparatorsAndTraversal() {
        assertEquals("_my_video.mp4", "../my/video.mp4".safeFileName())
    }
}
