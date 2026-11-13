package com.v0idspace.hyperwallpaper

import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.util.DetailNavigationState
import org.junit.Assert.*
import org.junit.Test

class NavigationAndRecommendationTest {

    @Test
    fun testDepthNavigationSessionIsolation() {
        val wpA = Wallpaper(id = "wp_A", title = "Nature Lake", author = "VCG", thumbnailUrl = "thumbA", previewUrl = "prevA", downloadUrl = "downA", hash = "hA")
        val wpB = Wallpaper(id = "wp_B", title = "Nature Forest", author = "VCG", thumbnailUrl = "thumbB", previewUrl = "prevB", downloadUrl = "downB", hash = "hB")
        val catalogList = listOf(wpA, wpB)

        val sessionDepth1 = "catalog_1"
        DetailNavigationState.saveSession(sessionDepth1, catalogList)

        assertEquals(2, DetailNavigationState.getSession(sessionDepth1)?.size)
        assertEquals("wp_A", DetailNavigationState.getSession(sessionDepth1)?.get(0)?.id)

        val rec1 = Wallpaper(id = "rec_1", title = "Deep Lake Reflection", author = "ArtistX", thumbnailUrl = "thumbR1", previewUrl = "prevR1", downloadUrl = "downR1", hash = "hR1")
        val rec2 = Wallpaper(id = "rec_2", title = "Mountain Mist", author = "ArtistY", thumbnailUrl = "thumbR2", previewUrl = "prevR2", downloadUrl = "downR2", hash = "hR2")
        val recList = listOf(rec1, rec2)

        val sessionDepth2 = "rec_${rec1.id}_1001"
        DetailNavigationState.saveSession(sessionDepth2, recList)

        val depth2List = DetailNavigationState.getSession(sessionDepth2)
        assertNotNull(depth2List)
        assertEquals(2, depth2List?.size)
        assertEquals("rec_1", depth2List?.get(0)?.id)
        assertEquals("rec_2", depth2List?.get(1)?.id)

        assertEquals("wp_A", DetailNavigationState.getSession(sessionDepth1)?.get(0)?.id)
        assertNotEquals(DetailNavigationState.getSession(sessionDepth1)?.get(0)?.id, depth2List?.get(0)?.id)
    }

    @Test
    fun testTargetIndexResolutionMatchesSelectedWallpaper() {
        val wallpapers = (0..10).map { i ->
            Wallpaper(
                id = "wp_$i",
                title = "Wallpaper $i",
                author = "Author $i",
                thumbnailUrl = "thumb_$i",
                previewUrl = "prev_$i",
                downloadUrl = "down_$i",
                hash = "hash_$i"
            )
        }

        val clickedId = "wp_7"
        val passedIndex = 7

        val foundIndex = wallpapers.indexOfFirst { it.id == clickedId }
        val resolvedIndex = if (foundIndex >= 0) foundIndex else passedIndex.coerceIn(0, wallpapers.size - 1)

        assertEquals(7, resolvedIndex)
        assertEquals(clickedId, wallpapers[resolvedIndex].id)
    }

    @Test
    fun testFileSizeFormattingFromBytes() {
        // Xiaomi API returns 3,680,883 bytes for 3.51 MB
        val wp = Wallpaper(
            id = "wp_bytes_test",
            title = "Test Wallpaper",
            author = "VCG",
            thumbnailUrl = "thumb",
            previewUrl = "prev",
            downloadUrl = "down",
            hash = "h",
            fileSizeBytes = 3680883L
        )

        assertEquals("3.51 MB Master", wp.formattedFileSize)
    }

    @Test
    fun testAiWallpaperDetection() {
        val wpAi1 = Wallpaper(
            id = "ai_1",
            title = "Ai anime cyberpunk portrait",
            author = "Designer",
            thumbnailUrl = "thumb",
            previewUrl = "prev",
            downloadUrl = "down",
            hash = "h1",
            tags = listOf("Anime", "Art")
        )

        val wpAi2 = Wallpaper(
            id = "ai_2",
            title = "Dreamy Landscape",
            author = "Midjourney Studio",
            thumbnailUrl = "thumb",
            previewUrl = "prev",
            downloadUrl = "down",
            hash = "h2",
            tags = listOf("Landscape", "AIGC壁纸")
        )

        val wpReal = Wallpaper(
            id = "real_1",
            title = "Alpine Mountain Sunlight",
            author = "VCG",
            thumbnailUrl = "thumb",
            previewUrl = "prev",
            downloadUrl = "down",
            hash = "h3",
            tags = listOf("Mountain", "Snow", "Nature")
        )

        assertTrue(wpAi1.isAiGenerated)
        assertTrue(wpAi2.isAiGenerated)
        assertFalse(wpReal.isAiGenerated)
    }

    @Test
    fun testDeterministicWallpaperSeedOffset() {
        val wp1 = Wallpaper(id = "wp_alpha_123", title = "W1", author = "A", thumbnailUrl = "t", previewUrl = "p", downloadUrl = "d", hash = "h1")
        val wp2 = Wallpaper(id = "wp_beta_987", title = "W2", author = "A", thumbnailUrl = "t", previewUrl = "p", downloadUrl = "d", hash = "h2")

        val seed1 = (wp1.id.hashCode() and 0x7FFFFFFF) % 25
        val seed2 = (wp2.id.hashCode() and 0x7FFFFFFF) % 25

        assertNotEquals(seed1, seed2)
    }
}
