package com.v0idspace.hyperwallpaper.ui.screens.author

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.ui.components.WallpaperCard
import com.v0idspace.hyperwallpaper.ui.theme.DarkBackground
import com.v0idspace.hyperwallpaper.ui.theme.XiaomiOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorScreen(
    authorName: String,
    viewModel: AuthorViewModel,
    onBack: () -> Unit,
    onWallpaperClick: (Wallpaper) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()

    LaunchedEffect(authorName) {
        viewModel.loadAuthor(authorName)
    }

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
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = authorName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Studio",
                            tint = XiaomiOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = XiaomiOrange,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.error != null && state.wallpapers.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.error ?: "Error", color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.loadAuthor(authorName) },
                        colors = ButtonDefaults.buttonColors(containerColor = XiaomiOrange)
                    ) {
                        Text("Retry")
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    state = gridState,
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.wallpapers, key = { it.id }) { wallpaper ->
                        WallpaperCard(
                            wallpaper = wallpaper,
                            isDense = true,
                            onClick = { onWallpaperClick(wallpaper) }
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
