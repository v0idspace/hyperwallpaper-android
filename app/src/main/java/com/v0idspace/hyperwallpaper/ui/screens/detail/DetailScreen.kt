package com.v0idspace.hyperwallpaper.ui.screens.detail

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.ui.components.SetWallpaperDialog
import com.v0idspace.hyperwallpaper.ui.components.WallpaperCard
import com.v0idspace.hyperwallpaper.ui.theme.DarkBackground
import com.v0idspace.hyperwallpaper.ui.theme.DarkSurface
import com.v0idspace.hyperwallpaper.ui.theme.XiaomiOrange
import com.v0idspace.hyperwallpaper.util.DetailNavigationState

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    val state by viewModel.uiState.collectAsState()
    var showSetDialog by remember { mutableStateOf(false) }
    var showDetailsSheet by remember { mutableStateOf(false) }

    // Synchronously resolve session list to prevent initial 0-page race conditions
    val sessionList = remember(sessionId) {
        DetailNavigationState.getSession(sessionId).orEmpty()
    }

    val targetInitialIndex = remember(sessionId, initialWallpaperId, initialIndex) {
        val found = sessionList.indexOfFirst { it.id == initialWallpaperId }
        if (found >= 0) found else initialIndex.coerceIn(0, (sessionList.size - 1).coerceAtLeast(0))
    }

    LaunchedEffect(sessionId, initialWallpaperId) {
        viewModel.initSession(sessionId, initialWallpaperId, targetInitialIndex)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    val swipeList = if (state.swipeableList.isNotEmpty()) state.swipeableList else sessionList
    val pageCount = if (swipeList.isNotEmpty()) swipeList.size else 1
    val safeStartPage = if (swipeList.isNotEmpty()) targetInitialIndex.coerceIn(0, swipeList.size - 1) else 0

    // Key pagerState uniquely to this session so it never defaults to 0
    val pagerState = rememberPagerState(initialPage = safeStartPage) { pageCount }

    // Sync only when page has settled to avoid transient clobbering
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { settledPage ->
                if (swipeList.isNotEmpty() && settledPage < swipeList.size) {
                    viewModel.onPageChanged(settledPage)
                }
            }
    }

    val currentWp = state.currentWallpaper ?: swipeList.getOrNull(pagerState.currentPage)

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

    // Modal Bottom Sheet for Details & Recommendations
    if (showDetailsSheet && currentWp != null) {
        val sheetListState = rememberLazyGridState()

        // Infinite recommendations scroll listener
        LaunchedEffect(sheetListState) {
            snapshotFlow { sheetListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastIdx ->
                    if (lastIdx != null && lastIdx >= state.relatedWallpapers.size - 4 && state.relatedWallpapers.isNotEmpty()) {
                        viewModel.loadMoreRelated()
                    }
                }
        }

        ModalBottomSheet(
            onDismissRequest = { showDetailsSheet = false },
            containerColor = DarkSurface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.35f))
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Title & Author Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentWp.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                showDetailsSheet = false
                                onOpenAuthor(currentWp.author)
                            }
                        ) {
                            Text(
                                text = "By ${currentWp.author}",
                                color = XiaomiOrange,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (currentWp.verifiedStudio) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Studio",
                                    tint = XiaomiOrange,
                                    modifier = Modifier.size(15.dp)
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
                            text = "1080p Master",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Tags Pill Row
                if (currentWp.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        currentWp.tags.take(5).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    color = Color(0xFFD1D5DB),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Similar Wallpapers Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = XiaomiOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Similar Wallpapers",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recommendations Grid inside Bottom Sheet
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = sheetListState,
                    contentPadding = PaddingValues(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    itemsIndexed(state.relatedWallpapers, key = { _, it -> it.id }) { relIdx, relWp ->
                        WallpaperCard(
                            wallpaper = relWp,
                            isDense = false,
                            onClick = {
                                showDetailsSheet = false
                                val nextSessionId = "rec_${relWp.id}_${System.currentTimeMillis()}"
                                DetailNavigationState.saveSession(nextSessionId, state.relatedWallpapers)
                                onOpenRelated(nextSessionId, relWp, relIdx)
                            }
                        )
                    }

                    if (state.isLoadingRelatedMore) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (currentWp == null && swipeList.isEmpty()) {
            CircularProgressIndicator(
                color = XiaomiOrange,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // Fullscreen Swipeable Hero Wallpaper Viewer
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val wp = swipeList.getOrNull(page) ?: currentWp
                if (wp != null) {
                    var scale by remember { mutableFloatStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }

                    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
                        scale = (scale * zoomChange).coerceIn(1f, 4f)
                        if (scale > 1f) {
                            offset += offsetChange
                        } else {
                            offset = Offset.Zero
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        scale = if (scale > 1.2f) 1f else 2.5f
                                        offset = Offset.Zero
                                    },
                                    onTap = {
                                        viewModel.toggleUiVisibility()
                                    }
                                )
                            }
                            .transformable(state = transformState)
                    ) {
                        // Progressive Loading: Instant cached thumbnail placeholder -> Crisp 1080p preview
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(wp.previewUrl) // High-definition 1080p preview!
                                .placeholderMemoryCacheKey(wp.thumbnailUrl)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .crossfade(300)
                                .build(),
                            contentDescription = wp.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = offset.x
                                    translationY = offset.y
                                }
                        )
                    }
                }
            }

            // Top Floating Navigation Bar
            AnimatedVisibility(
                visible = state.isUiVisible,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back Button
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        // Index Pill Indicator (e.g. "5 / 24")
                        if (swipeList.size > 1) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${pagerState.currentPage + 1} / ${swipeList.size}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Action Icons: Details Sheet & Favorite
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { showDetailsSheet = true },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Details & Similar",
                                    tint = Color.White
                                )
                            }

                            IconButton(
                                onClick = { viewModel.toggleFavorite() },
                                modifier = Modifier
                                    .size(42.dp)
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

            // Bottom Floating Action Dock
            AnimatedVisibility(
                visible = state.isUiVisible,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Apply Wallpaper Button (Primary)
                        Button(
                            onClick = { showSetDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = XiaomiOrange),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Apply",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Download Master Button (Secondary)
                        Button(
                            onClick = { viewModel.downloadMaster(context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.18f)
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            enabled = !state.isDownloading
                        ) {
                            if (state.isDownloading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Download",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Quick Similar Button
                        IconButton(
                            onClick = { showDetailsSheet = true },
                            modifier = Modifier
                                .size(54.dp)
                                .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Similar Wallpapers",
                                tint = XiaomiOrange
                            )
                        }
                    }
                }
            }
        }
    }
}
