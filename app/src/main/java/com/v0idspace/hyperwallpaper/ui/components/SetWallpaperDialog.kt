package com.v0idspace.hyperwallpaper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.v0idspace.hyperwallpaper.ui.theme.DarkSurface
import com.v0idspace.hyperwallpaper.ui.theme.DarkSurfaceVariant
import com.v0idspace.hyperwallpaper.ui.theme.XiaomiOrange
import com.v0idspace.hyperwallpaper.util.WallpaperTarget

@Composable
fun SetWallpaperDialog(
    isApplying: Boolean,
    onDismiss: () -> Unit,
    onSelectTarget: (WallpaperTarget) -> Unit,
    onLaunchCropPicker: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isApplying) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Apply Wallpaper",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Select display destination",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isApplying) {
                    CircularProgressIndicator(
                        color = XiaomiOrange,
                        modifier = Modifier.padding(24.dp)
                    )
                    Text(
                        text = "Setting wallpaper...",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                } else {
                    DialogOptionRow(
                        icon = Icons.Default.Home,
                        title = "Home Screen",
                        onClick = { onSelectTarget(WallpaperTarget.HOME) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DialogOptionRow(
                        icon = Icons.Default.Lock,
                        title = "Lock Screen",
                        onClick = { onSelectTarget(WallpaperTarget.LOCK) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DialogOptionRow(
                        icon = Icons.Default.PhoneAndroid,
                        title = "Both Screens",
                        onClick = { onSelectTarget(WallpaperTarget.BOTH) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DialogOptionRow(
                        icon = Icons.Default.Crop,
                        title = "System Crop & Set",
                        subtitle = "Adjust pan and crop before applying",
                        onClick = onLaunchCropPicker
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancel", color = Color(0xFF9CA3AF))
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceVariant, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = XiaomiOrange,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp
                )
            }
        }
    }
}
