package com.v0idspace.hyperwallpaper.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.ui.components.CategoryPills
import com.v0idspace.hyperwallpaper.ui.components.SubtagChips
import com.v0idspace.hyperwallpaper.ui.components.WallpaperCard
import com.v0idspace.hyperwallpaper.ui.theme.DarkBackground
import com.v0idspace.hyperwallpaper.ui.theme.DarkSurface
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
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Region Selector Pill with flag & highlight
                            val regionLabel = when (state.currentRegion) {
                                "CN" -> "🇨🇳 CN"
                                "GLOBAL" -> "🌐 GLOBAL"
                                else -> "🇪🇺 EEA"
                            }

                            Box {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(0.8.dp, XiaomiOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .clickable { regionMenuOpen = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = regionLabel,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Select Region",
                                            tint = XiaomiOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = regionMenuOpen,
                                    onDismissRequest = { regionMenuOpen = false }
                                ) {
                                    // EEA
                                    val isEea = state.currentRegion.equals("EEA", ignoreCase = true)
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "🇪🇺 EEA (Europe / Global)",
                                                color = if (isEea) XiaomiOrange else Color.White,
                                                fontWeight = if (isEea) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        trailingIcon = {
                                            if (isEea) {
                                                Icon(Icons.Default.Check, contentDescription = "Active", tint = XiaomiOrange)
                                            }
                                        },
                                        onClick = {
                                            viewModel.setRegion("EEA")
                                            regionMenuOpen = false
                                        }
                                    )

                                    // GLOBAL
                                    val isGlobal = state.currentRegion.equals("GLOBAL", ignoreCase = true)
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "🌐 GLOBAL (International)",
                                                color = if (isGlobal) XiaomiOrange else Color.White,
                                                fontWeight = if (isGlobal) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        trailingIcon = {
                                            if (isGlobal) {
                                                Icon(Icons.Default.Check, contentDescription = "Active", tint = XiaomiOrange)
                                            }
                                        },
                                        onClick = {
                                            viewModel.setRegion("GLOBAL")
                                            regionMenuOpen = false
                                        }
                                    )

                                    // CN
                                    val isCn = state.currentRegion.equals("CN", ignoreCase = true)
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "🇨🇳 CN (China Mainland)",
                                                color = if (isCn) XiaomiOrange else Color.White,
                                                fontWeight = if (isCn) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        trailingIcon = {
                                            if (isCn) {
                                                Icon(Icons.Default.Check, contentDescription = "Active", tint = XiaomiOrange)
                                            }
                                        },
                                        onClick = {
                                            viewModel.setRegion("CN")
                                            regionMenuOpen = false
                                        }
                                    )
                                }
                            }

                            // Shuffle / Discover Random toggle
                            IconButton(onClick = { viewModel.toggleShuffle() }) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Discover Random Pool",
                                    tint = if (state.isShuffleEnabled) XiaomiOrange else Color.White
                                )
                            }

                            // AI Filter Toggle
                            IconButton(onClick = { viewModel.toggleFilterAi() }) {
                                Icon(
                                    imageVector = if (state.filterAi) Icons.Default.FilterAlt else Icons.Default.FilterAltOff,
                                    contentDescription = "Filter AI Wallpapers",
                                    tint = if (state.filterAi) XiaomiOrange else Color.White
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
                            placeholder = { Text("Search Xiaomi catalog...", color = Color.Gray) },
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

                    val isCn = state.currentRegion.equals("CN", ignoreCase = true)
                    val activeSubtags = if (isCn) {
                        state.selectedCategory.cnSubtags
                    } else {
                        state.selectedCategory.subtags
                    }

                    val allLabel = if (isCn) "全部" else "All"
                    SubtagChips(
                        subtags = listOf(allLabel) + activeSubtags,
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
                // Fixed 3-column Grid for fast, immersive browsing
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    state = gridState,
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        end = 8.dp,
                        top = 6.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(state.wallpapers, key = { _, item -> item.id }) { index, wallpaper ->
                        WallpaperCard(
                            wallpaper = wallpaper,
                            isDense = true,
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
