package com.v0idspace.hyperwallpaper.ui.screens.detail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v0idspace.hyperwallpaper.HyperWallpaperApp
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.data.repository.FavoritesRepository
import com.v0idspace.hyperwallpaper.data.repository.WallpaperRepository
import com.v0idspace.hyperwallpaper.util.MediaStoreHelper
import com.v0idspace.hyperwallpaper.util.WallpaperManagerHelper
import com.v0idspace.hyperwallpaper.util.WallpaperTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val wallpaper: Wallpaper? = null,
    val isFavorite: Boolean = false,
    val isApplying: Boolean = false,
    val isDownloading: Boolean = false,
    val isUiVisible: Boolean = true,
    val relatedWallpapers: List<Wallpaper> = emptyList(),
    val isLoadingRelatedMore: Boolean = false,
    val message: String? = null
)

class DetailViewModel(
    private val wallpaperRepository: WallpaperRepository = HyperWallpaperApp.instance.wallpaperRepository,
    private val favoritesRepository: FavoritesRepository = HyperWallpaperApp.instance.favoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var relatedOffset = 0

    fun loadWallpaper(id: String) {
        val cached = wallpaperRepository.getCachedWallpaper(id)
        if (cached != null) {
            _uiState.value = _uiState.value.copy(
                wallpaper = cached,
                isFavorite = favoritesRepository.isFavorite(cached.id),
                relatedWallpapers = emptyList()
            )
            relatedOffset = 0
            loadRelated(cached, reset = true)
        }
    }

    fun toggleUiVisibility() {
        _uiState.value = _uiState.value.copy(isUiVisible = !_uiState.value.isUiVisible)
    }

    fun loadMoreRelated() {
        val wp = _uiState.value.wallpaper ?: return
        if (_uiState.value.isLoadingRelatedMore) return
        loadRelated(wp, reset = false)
    }

    private fun loadRelated(wallpaper: Wallpaper, reset: Boolean) {
        viewModelScope.launch {
            if (!reset) {
                _uiState.value = _uiState.value.copy(isLoadingRelatedMore = true)
            }
            try {
                val newRelated = wallpaperRepository.getRelatedWallpapers(
                    wallpaper = wallpaper,
                    offset = relatedOffset,
                    count = 12
                )
                relatedOffset += newRelated.size
                _uiState.value = _uiState.value.copy(
                    relatedWallpapers = if (reset) newRelated else _uiState.value.relatedWallpapers + newRelated,
                    isLoadingRelatedMore = false
                )
            } catch (ignored: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingRelatedMore = false)
            }
        }
    }

    fun toggleFavorite() {
        val wp = _uiState.value.wallpaper ?: return
        val newFav = favoritesRepository.toggleFavorite(wp)
        _uiState.value = _uiState.value.copy(
            isFavorite = newFav,
            message = if (newFav) "Added to Favorites" else "Removed from Favorites"
        )
    }

    fun applyWallpaper(context: Context, target: WallpaperTarget, onDone: () -> Unit) {
        val wp = _uiState.value.wallpaper ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplying = true)
            val result = WallpaperManagerHelper.setWallpaperDirect(context, wp.downloadUrl, target)
            _uiState.value = _uiState.value.copy(
                isApplying = false,
                message = if (result.isSuccess) "Wallpaper set successfully!" else "Failed to set: ${result.exceptionOrNull()?.message}"
            )
            onDone()
        }
    }

    fun launchCropPicker(context: Context, onIntentReady: (Intent) -> Unit) {
        val wp = _uiState.value.wallpaper ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplying = true)
            val result = WallpaperManagerHelper.launchSystemCropPicker(context, wp.downloadUrl)
            _uiState.value = _uiState.value.copy(isApplying = false)
            result.onSuccess { intent ->
                onIntentReady(intent)
            }.onFailure {
                _uiState.value = _uiState.value.copy(message = "Failed to launch crop picker: ${it.message}")
            }
        }
    }

    fun downloadMaster(context: Context) {
        val wp = _uiState.value.wallpaper ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDownloading = true)
            val result = MediaStoreHelper.saveWallpaperToGallery(
                context = context,
                downloadUrl = wp.downloadUrl,
                title = wp.title,
                hash = wp.hash
            )
            _uiState.value = _uiState.value.copy(
                isDownloading = false,
                message = if (result.isSuccess) "Master saved to Pictures/HyperWallpapers" else "Download error: ${result.exceptionOrNull()?.message}"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
