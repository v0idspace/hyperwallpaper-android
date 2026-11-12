package com.v0idspace.hyperwallpaper.ui.screens.detail

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.v0idspace.hyperwallpaper.util.DetailNavigationState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    sessionId: String,
    initialWallpaperId: String,
    initialIndex: Int,
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onOpenAuthor: (String) -> Unit,
    onOpenRelated: (String, Wallpaper, Int) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showSetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId, initialWallpaperId) {
        viewModel.initSession(sessionId, initialWallpaperId, initialIndex)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    val swipeList = state.swipeableList
    val pageCount = if (swipeList.isNotEmpty()) swipeList.size else 1
    val startPage = if (swipeList.isNotEmpty()) initialIndex.coerceIn(0, swipeList.size - 1) else 0
    val pagerState = rememberPagerState(initialPage = startPage) { pageCount }

    // Sync HorizontalPager page changes with ViewModel
    LaunchedEffect(pagerState.currentPage, swipeList) {
        if (swipeList.isNotEmpty() && pagerState.currentPage < swipeList.size) {
            viewModel.onPageChanged(pagerState.currentPage)
        }
    }

    // Infinite scroll listener for 2-column similar wallpapers grid
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                val chunkedCount = (state.relatedWallpapers.size + 1) / 2
                if (lastIndex != null && lastIndex >= chunkedCount - 2 && state.relatedWallpapers.isNotEmpty()) {
                    viewModel.loadMoreRelated()
                }
            }
    }

    val currentWp = state.currentWallpaper

    if (showSetDialog && currentWp != null) {
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
        if (currentWp == null) {
            CircularProgressIndicator(
                color = XiaomiOrange,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // Vertically scrollable page: Hero Swipeable Wallpaper -> Metadata & Actions -> 2-Column Similar Wallpapers Grid
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp
                )
            ) {
                // Section 1: Hero Swipable Horizontal Pager (Swipes left-to-right between wallpapers)
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
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val wp = swipeList.getOrNull(page) ?: currentWp
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Fast Image Loading: Use lightweight thumbnail directly from cache for 0ms lag!
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(wp.thumbnailUrl)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                        .crossfade(false)
                                        .build(),
                                    contentDescription = wp.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Scroll Down Cue (Only visible when UI is toggled on)
                        AnimatedVisibility(
                            visible = state.isUiVisible,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Scroll for details & recommendations",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = XiaomiOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Wallpaper Metadata & Action Buttons
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentWp.title,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onOpenAuthor(currentWp.author) }
                                ) {
                                    Text(
                                        text = "By ${currentWp.author}",
                                        color = Color(0xFFD1D5DB),
                                        fontSize = 14.sp
                                    )
                                    if (currentWp.verifiedStudio) {
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
                                    text = "${currentWp.width}x${currentWp.height} Master",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Tags pill row
                        if (currentWp.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                currentWp.tags.take(4).forEach { tag ->
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
                            text = "Fresh recommendations derived from this style",
                            color = Color(0xFF9CA3AF),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Section 3: 2-Column Vertical Grid of Recommended Wallpapers
                val pairs = state.relatedWallpapers.chunked(2)
                itemsIndexed(pairs) { rowIndex, rowWallpapers ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for ((itemIndex, wp) in rowWallpapers.withIndex()) {
                            val absoluteIndex = (rowIndex * 2) + itemIndex
                            WallpaperCard(
                                wallpaper = wp,
                                isDense = false,
                                onClick = {
                                    // Save current recommendation list as session for Depth 2
                                    val nextSessionId = "rec_${wp.id}_${System.currentTimeMillis()}"
                                    DetailNavigationState.saveSession(nextSessionId, state.relatedWallpapers)
                                    onOpenRelated(nextSessionId, wp, absoluteIndex)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
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

                    // Index Pill Indicator (e.g. "14 / 60")
                    if (swipeList.size > 1) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${swipeList.size}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
