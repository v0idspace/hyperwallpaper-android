package com.v0idspace.hyperwallpaper.ui.screens.author

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.v0idspace.hyperwallpaper.HyperWallpaperApp
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthorUiState(
    val authorName: String = "",
    val wallpapers: List<Wallpaper> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null
)

class AuthorViewModel(
    private val repository: WallpaperRepository = HyperWallpaperApp.instance.wallpaperRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthorUiState())
    val uiState: StateFlow<AuthorUiState> = _uiState.asStateFlow()

    private var currentOffset = 0

    fun loadAuthor(name: String) {
        if (_uiState.value.authorName == name && _uiState.value.wallpapers.isNotEmpty()) return
        _uiState.value = AuthorUiState(authorName = name, isLoading = true)
        currentOffset = 0

        viewModelScope.launch {
            try {
                val list = repository.getAuthorPortfolio(name, currentOffset, 18, "EEA")
                currentOffset += list.size
                _uiState.value = _uiState.value.copy(
                    wallpapers = list,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to load author portfolio"
                )
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || state.authorName.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            try {
                val list = repository.getAuthorPortfolio(state.authorName, currentOffset, 18, "EEA")
                currentOffset += list.size
                _uiState.value = _uiState.value.copy(
                    wallpapers = _uiState.value.wallpapers + list,
                    isLoadingMore = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingMore = false)
            }
        }
    }
}
