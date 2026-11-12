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

        // Depth 1: User is in Catalog
        val sessionDepth1 = "catalog"
        DetailNavigationState.saveSession(sessionDepth1, catalogList)

        assertEquals(2, DetailNavigationState.getSession(sessionDepth1)?.size)
        assertEquals("wp_A", DetailNavigationState.getSession(sessionDepth1)?.get(0)?.id)

        // Depth 2: User clicks a recommendation from wp_A's similar grid
        val rec1 = Wallpaper(id = "rec_1", title = "Deep Lake Reflection", author = "ArtistX", thumbnailUrl = "thumbR1", previewUrl = "prevR1", downloadUrl = "downR1", hash = "hR1")
        val rec2 = Wallpaper(id = "rec_2", title = "Mountain Mist", author = "ArtistY", thumbnailUrl = "thumbR2", previewUrl = "prevR2", downloadUrl = "downR2", hash = "hR2")
        val recList = listOf(rec1, rec2)

        val sessionDepth2 = "rec_${rec1.id}_1001"
        DetailNavigationState.saveSession(sessionDepth2, recList)

        // Verify Depth 2 gets its exact recommendation list and NEVER defaults to Catalog's wp_A
        val depth2List = DetailNavigationState.getSession(sessionDepth2)
        assertNotNull(depth2List)
        assertEquals(2, depth2List?.size)
        assertEquals("rec_1", depth2List?.get(0)?.id)
        assertEquals("rec_2", depth2List?.get(1)?.id)

        // Depth 1 remains completely intact
        assertEquals("wp_A", DetailNavigationState.getSession(sessionDepth1)?.get(0)?.id)
        assertNotEquals(DetailNavigationState.getSession(sessionDepth1)?.get(0)?.id, depth2List?.get(0)?.id)
    }

    @Test
    fun testSemanticRecommendationExclusion() {
        val wp = Wallpaper(
            id = "lake_101",
            title = "Why is namtso lake regarded as a sacred lake",
            author = "VCG",
            thumbnailUrl = "thumb",
            previewUrl = "prev",
            downloadUrl = "down",
            hash = "h",
            tags = listOf("Landscape", "Refreshing", "Pleasant")
        )

        // Extract query seeds logic
        val candidateTags = wp.tags.filter { it != "VCG" && it != "Xiaomi" }
        assertTrue(candidateTags.contains("Landscape"))
        assertTrue(candidateTags.contains("Refreshing"))
        assertTrue(candidateTags.contains("Pleasant"))

        // Title words extracted
        val words = wp.title.split(" ").filter { it.length > 3 && it != "with" && it != "that" }
        assertTrue(words.contains("namtso"))
        assertTrue(words.contains("lake"))
        assertTrue(words.contains("regarded"))
        assertTrue(words.contains("sacred"))
    }
}
