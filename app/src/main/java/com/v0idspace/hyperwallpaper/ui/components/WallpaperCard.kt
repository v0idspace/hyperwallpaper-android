package com.v0idspace.hyperwallpaper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.ui.theme.CardBorder
import com.v0idspace.hyperwallpaper.ui.theme.DarkSurfaceVariant
import com.v0idspace.hyperwallpaper.ui.theme.XiaomiOrange

@Composable
fun WallpaperCard(
    wallpaper: Wallpaper,
    isDense: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .aspectRatio(9f / 19.5f)
            .clip(RoundedCornerShape(if (isDense) 12.dp else 16.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, CardBorder, RoundedCornerShape(if (isDense) 12.dp else 16.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(wallpaper.thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for bottom title and author readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(if (isDense) 8.dp else 10.dp)
        ) {
            Text(
                text = wallpaper.title,
                color = Color.White,
                fontSize = if (isDense) 12.sp else 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = wallpaper.author,
                color = Color(0xFFD1D5DB),
                fontSize = if (isDense) 10.sp else 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 4K / HD Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(if (isDense) 6.dp else 8.dp)
                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "HD",
                color = XiaomiOrange,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
