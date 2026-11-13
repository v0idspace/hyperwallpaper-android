package com.v0idspace.hyperwallpaper.util

import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe navigation session store to manage wallpaper lists
 * across navigation levels and swipeable views without state corruption or index desync bugs.
 */
object DetailNavigationState {

    private val sessionStore = ConcurrentHashMap<String, List<Wallpaper>>()

    fun saveSession(sessionId: String, wallpapers: List<Wallpaper>) {
        if (wallpapers.isNotEmpty()) {
            sessionStore[sessionId] = ArrayList(wallpapers)
        }
    }

    fun getSession(sessionId: String): List<Wallpaper>? {
        return sessionStore[sessionId]
    }

    fun removeSession(sessionId: String) {
        sessionStore.remove(sessionId)
    }

    fun clear() {
        sessionStore.clear()
    }
}
