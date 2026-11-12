package com.v0idspace.hyperwallpaper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.v0idspace.hyperwallpaper.ui.navigation.AppNavigation
import com.v0idspace.hyperwallpaper.ui.theme.DarkBackground
import com.v0idspace.hyperwallpaper.ui.theme.HyperWallpaperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Modern full edge-to-edge support (zero black bars on modern Android 14+ displays)
        enableEdgeToEdge()

        setContent {
            HyperWallpaperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
