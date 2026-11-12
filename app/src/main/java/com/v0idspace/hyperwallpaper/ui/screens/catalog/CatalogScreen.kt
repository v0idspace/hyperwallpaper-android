package com.v0idspace.hyperwallpaper.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.ui.components.CategoryPills
import com.v0idspace.hyperwallpaper.ui.components.SubtagChips
import com.v0idspace.hyperwallpaper.ui.components.WallpaperCard
import com.v0idspace.hyperwallpaper.ui.theme.DarkBackground
import com.v0idspace.hyperwallpaper.ui.theme.XiaomiOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    onWallpaperClick: (Wallpaper, Int) -> Unit,
    onOpenFavorites: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()
    var searchActive by remember { mutableStateOf(false) }
    var regionMenuOpen by remember { mutableStateOf(false) }

    // Infinite scroll listener
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= state.wallpapers.size - 6 && state.wallpapers.isNotEmpty()) {
                    viewModel.loadMore()
                }
            }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .statusBarsPadding()
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!searchActive) {
                        Text(
                            text = "HyperWallpaper",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Shuffle / Discover Random toggle
                            IconButton(onClick = { viewModel.toggleShuffle() }) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Discover Random Pool",
                                    tint = if (state.isShuffleEnabled) XiaomiOrange else Color.White
                                )
                            }

                            // Region Selector
                            Box {
                                IconButton(onClick = { regionMenuOpen = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = "Region: ${state.currentRegion}",
                                        tint = Color.White
                                    )
                                }
                                DropdownMenu(
                                    expanded = regionMenuOpen,
                                    onDismissRequest = { regionMenuOpen = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("EEA (Europe / HyperOS Global)") },
                                        onClick = {
                                            viewModel.setRegion("EEA")
                                            regionMenuOpen = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("GLOBAL (International)") },
                                        onClick = {
                                            viewModel.setRegion("GLOBAL")
                                            regionMenuOpen = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("CN (China Mainland)") },
                                        onClick = {
                                            viewModel.setRegion("CN")
                                            regionMenuOpen = false
                                        }
                                    )
                                }
                            }

                            // Density Switcher
                            IconButton(onClick = { viewModel.toggleGridDensity() }) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Toggle Grid",
                                    tint = if (state.isDenseGrid) XiaomiOrange else Color.White
                                )
                            }

                            // Search
                            IconButton(onClick = { searchActive = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.White
                                )
                            }

                            // Favorites
                            IconButton(onClick = onOpenFavorites) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Favorites",
                                    tint = XiaomiOrange
                                )
                            }
                        }
                    } else {
                        // Search Mode
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.search(it) },
                            placeholder = { Text("Search catalog...", color = Color.Gray) },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    searchActive = false
                                    viewModel.clearSearch()
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = XiaomiOrange,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (!searchActive) {
                    CategoryPills(
                        categories = state.categories,
                        selectedCategoryId = state.selectedCategory.id,
                        onCategorySelected = { viewModel.selectCategory(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val activeSubtags = if (state.currentRegion.equals("CN", ignoreCase = true)) {
                        state.selectedCategory.cnSubtags
                    } else {
                        state.selectedCategory.subtags
                    }

                    SubtagChips(
                        subtags = listOf(if (state.currentRegion.equals("CN", ignoreCase = true)) "全部" else "All") + activeSubtags,
                        selectedSubtag = state.selectedSubtag,
                        onSubtagSelected = { viewModel.selectSubtag(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading && state.wallpapers.isEmpty()) {
                CircularProgressIndicator(
                    color = XiaomiOrange,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.error != null && state.wallpapers.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.error ?: "Error loading wallpapers", color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.loadMore() },
                        colors = ButtonDefaults.buttonColors(containerColor = XiaomiOrange)
                    ) {
                        Text("Retry")
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (state.isDenseGrid) 3 else 2),
                    state = gridState,
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(state.wallpapers, key = { _, item -> item.id }) { index, wallpaper ->
                        WallpaperCard(
                            wallpaper = wallpaper,
                            isDense = state.isDenseGrid,
                            onClick = { onWallpaperClick(wallpaper, index) }
                        )
                    }

                    if (state.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = XiaomiOrange, strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}
