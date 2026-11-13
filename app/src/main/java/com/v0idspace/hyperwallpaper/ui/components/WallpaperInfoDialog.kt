package com.v0idspace.hyperwallpaper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.v0idspace.hyperwallpaper.data.model.ProductDetail
import com.v0idspace.hyperwallpaper.data.model.Wallpaper
import com.v0idspace.hyperwallpaper.ui.theme.DarkSurface
import com.v0idspace.hyperwallpaper.ui.theme.DarkSurfaceVariant
import com.v0idspace.hyperwallpaper.ui.theme.XiaomiOrange

@Composable
fun WallpaperInfoDialog(
    wallpaper: Wallpaper,
    productDetail: ProductDetail?,
    isLoadingDetail: Boolean,
    onDismiss: () -> Unit,
    onOpenAuthor: (String) -> Unit,
    onTagClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wallpaper Details",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Author Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!productDetail?.designerIcon.isNullOrBlank()) {
                        AsyncImage(
                            model = productDetail?.designerIcon,
                            contentDescription = productDetail?.designerName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(1.dp, XiaomiOrange, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = wallpaper.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                onDismiss()
                                onOpenAuthor(wallpaper.author)
                            }
                        ) {
                            Text(
                                text = "By ${wallpaper.author}",
                                color = XiaomiOrange,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (wallpaper.verifiedStudio) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Studio",
                                    tint = XiaomiOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Specifications Grid
                Text(
                    text = "SPECIFICATIONS",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SpecRow(label = "Master Resolution", value = "1080 × 2400 (FHD+ AMOLED)")
                    SpecRow(label = "Aspect Ratio", value = "20:9 Full-Screen")
                    SpecRow(label = "Uncompressed Master Size", value = wallpaper.formattedFileSize)
                    SpecRow(label = "Content Delivery Host", value = "t17.market.mi-img.com (Xiaomi CDN)")
                    if (productDetail != null && productDetail.productCount > 0) {
                        SpecRow(label = "Artist Portfolio Scale", value = "${productDetail.productCount} Store Wallpapers")
                    }
                    if (wallpaper.downloadCount > 0) {
                        SpecRow(label = "Store Downloads", value = "${wallpaper.downloadCount}+")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Official Editorial Story / Description
                Text(
                    text = "STORE EDITORIAL STORY",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .padding(14.dp)
                ) {
                    if (isLoadingDetail && productDetail == null) {
                        CircularProgressIndicator(
                            color = XiaomiOrange,
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                        )
                    } else {
                        val storyText = productDetail?.editorialStory ?: (
                            "Officially cataloged in the Xiaomi Theme Store catalog, \"${wallpaper.title}\" is an authentic smartphone " +
                            "wallpaper designed by ${wallpaper.author}. Engineered with a portrait aspect ratio of 20:9 (1080×2400), " +
                            "it delivers calibrated color depth tailored for HyperOS lockscreen clocks and launcher icon readability."
                        )
                        Text(
                            text = storyText,
                            color = Color(0xFFD1D5DB),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }

                // Tags Section
                val activeTags = productDetail?.tags?.takeIf { it.isNotEmpty() } ?: wallpaper.tags
                if (activeTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SEMANTIC TAGS (TAP TO EXPLORE)",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activeTags.take(5).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .clickable {
                                        onDismiss()
                                        onTagClick(tag)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    color = XiaomiOrange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFF9CA3AF), fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
