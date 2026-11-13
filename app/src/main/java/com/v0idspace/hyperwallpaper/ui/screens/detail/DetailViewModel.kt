package com.v0idspace.hyperwallpaper.ui.screens.detail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v0idspace.hyperwallpaper.HyperWallpaperApp
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.data.repository.FavoritesRepository
import com.v0idspace.hyperwallpaper.data.repository.WallpaperRepository
import com.v0idspace.hyperwallpaper.util.DetailNavigationState
import com.v0idspace.hyperwallpaper.util.MediaStoreHelper
import com.v0idspace.hyperwallpaper.util.WallpaperManagerHelper
import com.v0idspace.hyperwallpaper.util.WallpaperTarget
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val swipeableList: List<Wallpaper> = emptyList(),
    val currentWallpaper: Wallpaper? = null,
    val currentIndex: Int = 0,
    val isFavorite: Boolean = false,
    val isApplying: Boolean = false,
    val isDownloading: Boolean = false,
    val isUiVisible: Boolean = true,
    val isBottomSheetVisible: Boolean = false,
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

    private var loadRelatedJob: Job? = null
    private var relatedPage = 0
    private var isSessionInitialized = false

    fun initSession(sessionId: String, initialWallpaperId: String, initialIndex: Int) {
        if (isSessionInitialized) return
        isSessionInitialized = true

        val sessionList = DetailNavigationState.getSession(sessionId).orEmpty()
        val activeList: List<Wallpaper>
        val initialWp: Wallpaper?
        val calculatedIndex: Int

        if (sessionList.isNotEmpty()) {
            activeList = sessionList
            val foundIndex = sessionList.indexOfFirst { it.id == initialWallpaperId }
            calculatedIndex = if (foundIndex >= 0) {
                foundIndex
            } else {
                initialIndex.coerceIn(0, sessionList.size - 1)
            }
            initialWp = sessionList.getOrNull(calculatedIndex) ?: sessionList.firstOrNull()
        } else {
            val cached = wallpaperRepository.getCachedWallpaper(initialWallpaperId)
            activeList = listOfNotNull(cached)
            initialWp = cached
            calculatedIndex = 0
        }

        _uiState.value = _uiState.value.copy(
            swipeableList = activeList,
            currentWallpaper = initialWp,
            currentIndex = calculatedIndex,
            isFavorite = initialWp?.let { favoritesRepository.isFavorite(it.id) } ?: false
        )

        initialWp?.let {
            loadRelated(it, reset = true)
        }
    }

    fun onPageChanged(page: Int) {
        val list = _uiState.value.swipeableList
        val newWp = list.getOrNull(page) ?: return
        if (_uiState.value.currentWallpaper?.id == newWp.id) return

        _uiState.value = _uiState.value.copy(
            currentWallpaper = newWp,
            currentIndex = page,
            isFavorite = favoritesRepository.isFavorite(newWp.id),
            relatedWallpapers = emptyList(),
            isLoadingRelatedMore = false
        )
        relatedPage = 0
        loadRelated(newWp, reset = true)
    }

    fun toggleUiVisibility() {
        _uiState.value = _uiState.value.copy(isUiVisible = !_uiState.value.isUiVisible)
    }

    fun setBottomSheetVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isBottomSheetVisible = visible)
    }

    fun loadMoreRelated() {
        val wp = _uiState.value.currentWallpaper ?: return
        if (_uiState.value.isLoadingRelatedMore) return
        loadRelated(wp, reset = false)
    }

    private fun loadRelated(wallpaper: Wallpaper, reset: Boolean) {
        loadRelatedJob?.cancel()
        loadRelatedJob = viewModelScope.launch {
            if (!reset) {
                _uiState.value = _uiState.value.copy(isLoadingRelatedMore = true)
            }
            try {
                val newRelated = wallpaperRepository.getRelatedWallpapers(
                    wallpaper = wallpaper,
                    pageIndex = relatedPage,
                    count = 12
                )
                relatedPage++

                val currentIds = _uiState.value.relatedWallpapers.map { it.id }.toSet()
                val filtered = newRelated.filter { !currentIds.contains(it.id) && it.id != wallpaper.id }

                _uiState.value = _uiState.value.copy(
                    relatedWallpapers = if (reset) filtered else _uiState.value.relatedWallpapers + filtered,
                    isLoadingRelatedMore = false
                )
            } catch (ignored: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingRelatedMore = false)
            }
        }
    }

    fun filterRelatedByTag(tag: String) {
        val wp = _uiState.value.currentWallpaper ?: return
        loadRelatedJob?.cancel()
        loadRelatedJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                relatedWallpapers = emptyList(),
                isLoadingRelatedMore = true
            )
            try {
                val results = wallpaperRepository.search(tag, 0, 18, "EEA")
                    .filter { it.id != wp.id }
                _uiState.value = _uiState.value.copy(
                    relatedWallpapers = results,
                    isLoadingRelatedMore = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingRelatedMore = false)
            }
        }
    }

    fun toggleFavorite() {
        val wp = _uiState.value.currentWallpaper ?: return
        val newFav = favoritesRepository.toggleFavorite(wp)
        _uiState.value = _uiState.value.copy(
            isFavorite = newFav,
            message = if (newFav) "Added to Favorites" else "Removed from Favorites"
        )
    }

    fun applyWallpaper(context: Context, target: WallpaperTarget, onDone: () -> Unit) {
        val wp = _uiState.value.currentWallpaper ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplying = true)
            // Use downloadUrl for full uncompressed master resolution wallpaper setting
            val result = WallpaperManagerHelper.setWallpaperDirect(context, wp.downloadUrl, target)
            _uiState.value = _uiState.value.copy(
                isApplying = false,
                message = if (result.isSuccess) "Wallpaper applied successfully!" else "Failed: ${result.exceptionOrNull()?.message}"
            )
            onDone()
        }
    }

    fun launchCropPicker(context: Context, onIntentReady: (Intent) -> Unit) {
        val wp = _uiState.value.currentWallpaper ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplying = true)
            val result = WallpaperManagerHelper.launchSystemCropPicker(context, wp.downloadUrl)
            _uiState.value = _uiState.value.copy(isApplying = false)
            if (result.isSuccess) {
                onIntentReady(result.getOrThrow())
            } else {
                _uiState.value = _uiState.value.copy(message = "Crop picker failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun downloadMaster(context: Context) {
        val wp = _uiState.value.currentWallpaper ?: return
        if (_uiState.value.isDownloading) return

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
                message = if (result.isSuccess) "Original Master saved to Pictures/HyperWallpapers!" else "Download failed: ${result.exceptionOrNull()?.message}"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
