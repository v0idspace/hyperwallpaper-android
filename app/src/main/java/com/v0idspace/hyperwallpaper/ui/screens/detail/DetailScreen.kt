package com.v0idspace.hyperwallpaper.ui.screens.detail

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.ui.components.SetWallpaperDialog
import com.v0idspace.hyperwallpaper.ui.components.WallpaperCard
import com.v0idspace.hyperwallpaper.ui.theme.DarkBackground
import com.v0idspace.hyperwallpaper.ui.theme.XiaomiOrange

@Composable
fun DetailScreen(
    wallpaperId: String,
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onOpenAuthor: (String) -> Unit,
    onOpenRelated: (Wallpaper) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showSetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(wallpaperId) {
        viewModel.loadWallpaper(wallpaperId)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    // Infinite scroll listener for similar wallpapers as user scrolls down
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                val chunkedCount = (state.relatedWallpapers.size + 1) / 2
                // When approaching the bottom of the list
                if (lastIndex != null && lastIndex >= chunkedCount - 2 && state.relatedWallpapers.isNotEmpty()) {
                    viewModel.loadMoreRelated()
                }
            }
    }

    val wallpaper = state.wallpaper

    if (showSetDialog && wallpaper != null) {
        SetWallpaperDialog(
            isApplying = state.isApplying,
            onDismiss = { showSetDialog = false },
            onSelectTarget = { target ->
                viewModel.applyWallpaper(context, target) {
                    showSetDialog = false
                }
            },
            onLaunchCropPicker = {
                viewModel.launchCropPicker(context) { intent ->
                    showSetDialog = false
                    context.startActivity(intent)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (wallpaper == null) {
            CircularProgressIndicator(
                color = XiaomiOrange,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // Vertically scrollable page: Top hero preview -> Metadata & Actions -> 2-Column Similar Wallpapers Grid
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp
                )
            ) {
                // Section 1: Hero Wallpaper Image taking full screen height
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeight)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                viewModel.toggleUiVisibility()
                            }
                    ) {
                        // Progressive Loading: Instant thumbnail placeholder followed by full preview
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(wallpaper.previewUrl)
                                .placeholderMemoryCacheKey(wallpaper.thumbnailUrl)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = wallpaper.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Subtle bottom gradient hint to scroll down
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, DarkBackground)
                                    )
                                )
                        )
                    }
                }

                // Section 2: Wallpaper Metadata & Action Buttons
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = wallpaper.title,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onOpenAuthor(wallpaper.author) }
                                ) {
                                    Text(
                                        text = "By ${wallpaper.author}",
                                        color = Color(0xFFD1D5DB),
                                        fontSize = 14.sp
                                    )
                                    if (wallpaper.verifiedStudio) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified Studio",
                                            tint = XiaomiOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${wallpaper.width}x${wallpaper.height} Master",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Tags pill row
                        if (wallpaper.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                wallpaper.tags.take(4).forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            color = Color(0xFF9CA3AF),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large Action Buttons: Apply Wallpaper & Download Master
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showSetDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = XiaomiOrange),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                            ) {
                                Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Apply", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.downloadMaster(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.16f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                enabled = !state.isDownloading
                            ) {
                                if (state.isDownloading) {
                                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Download", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = "Similar Wallpapers",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Endless recommendations inspired by this style",
                            color = Color(0xFF9CA3AF),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Section 3: 2-Column Vertical Grid of Similar Wallpapers
                val pairs = state.relatedWallpapers.chunked(2)
                items(pairs) { rowWallpapers ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (wp in rowWallpapers) {
                            WallpaperCard(
                                wallpaper = wp,
                                isDense = false,
                                onClick = { onOpenRelated(wp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // If odd number in last row, pad with empty space
                        if (rowWallpapers.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (state.isLoadingRelatedMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = XiaomiOrange, strokeWidth = 2.5.dp)
                        }
                    }
                }
            }

            // Top Control Bar (Fades in/out on single tap)
            AnimatedVisibility(
                visible = state.isUiVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back Button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    // Favorite Button
                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (state.isFavorite) XiaomiOrange else Color.White
                        )
                    }
                }
            }
        }
    }
}
