package com.v0idspace.hyperwallpaper.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v0idspace.hyperwallpaper.HyperWallpaperApp
import com.v0idspace.hyperwallpaper.data.model.Category
import com.v0idspace.hyperwallpaper.data.model.CategoryData
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.data.repository.WallpaperRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CatalogUiState(
    val categories: List<Category> = CategoryData.categories,
    val selectedCategory: Category = CategoryData.categories.first(),
    val selectedSubtag: String? = null,
    val wallpapers: List<Wallpaper> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isShuffleEnabled: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val isDenseGrid: Boolean = false,
    val currentRegion: String = "EEA"
)

class CatalogViewModel(
    private val repository: WallpaperRepository = HyperWallpaperApp.instance.wallpaperRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var currentOffset = 0

    init {
        loadWallpapers(reset = true)
    }

    fun selectCategory(category: Category) {
        if (_uiState.value.selectedCategory.id == category.id && !_uiState.value.isSearching) return
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            selectedSubtag = null,
            searchQuery = "",
            isSearching = false
        )
        loadWallpapers(reset = true)
    }

    fun selectSubtag(subtag: String?) {
        if (_uiState.value.selectedSubtag == subtag) return
        _uiState.value = _uiState.value.copy(selectedSubtag = subtag)
        loadWallpapers(reset = true)
    }

    fun setRegion(region: String) {
        if (_uiState.value.currentRegion == region) return
        _uiState.value = _uiState.value.copy(
            currentRegion = region,
            selectedSubtag = null
        )
        loadWallpapers(reset = true)
    }

    fun toggleGridDensity() {
        _uiState.value = _uiState.value.copy(isDenseGrid = !_uiState.value.isDenseGrid)
    }

    fun toggleShuffle() {
        val newShuffle = !_uiState.value.isShuffleEnabled
        _uiState.value = _uiState.value.copy(isShuffleEnabled = newShuffle)
        loadWallpapers(reset = true)
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearching = query.isNotBlank()
        )
        loadWallpapers(reset = true)
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "", isSearching = false)
        loadWallpapers(reset = true)
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore) return
        loadWallpapers(reset = false)
    }

    private fun loadWallpapers(reset: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (reset) {
                currentOffset = 0
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    wallpapers = if (reset) emptyList() else _uiState.value.wallpapers
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoadingMore = true, error = null)
            }

            try {
                val state = _uiState.value
                val newItems = if (state.isSearching) {
                    repository.search(state.searchQuery, currentOffset, 12, state.currentRegion)
                } else {
                    repository.getCatalog(
                        categoryName = state.selectedCategory.id,
                        subtag = state.selectedSubtag,
                        offset = currentOffset,
                        count = 12,
                        region = state.currentRegion,
                        randomizeSeed = state.isShuffleEnabled
                    )
                }

                currentOffset += newItems.size
                val rawCombined = if (reset) newItems else _uiState.value.wallpapers + newItems
                val updatedList = rawCombined.distinctBy { it.id }

                _uiState.value = _uiState.value.copy(
                    wallpapers = updatedList,
                    isLoading = false,
                    isLoadingMore = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = e.localizedMessage ?: "Network error. Tap retry."
                )
            }
        }
    }
}
