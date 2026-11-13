package com.v0idspace.hyperwallpaper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v0idspace.hyperwallpaper.data.model.Category
import com.v0idspace.hyperwallpaper.ui.theme.XiaomiOrange

@Composable
fun CategoryPills(
    categories: List<Category>,
    selectedCategoryId: String,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories, key = { it.id }) { cat ->
            val isSelected = cat.id == selectedCategoryId
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) XiaomiOrange else Color(0xFF1E212B)
                    )
                    .border(
                        1.dp,
                        if (isSelected) XiaomiOrange else Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onCategorySelected(cat) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = cat.icon, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cat.name,
                        color = if (isSelected) Color.White else Color(0xFFD1D5DB),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SubtagChips(
    subtags: List<String>,
    selectedSubtag: String?,
    onSubtagSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(subtags) { tag ->
            val isAllTag = tag.startsWith("All", ignoreCase = true) || tag == "全部" || tag.startsWith("全部")
            val isSelected = (selectedSubtag == null && isAllTag) || (selectedSubtag == tag)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.22f) else Color.Transparent
                    )
                    .border(
                        0.8.dp,
                        if (isSelected) XiaomiOrange else Color.White.copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        if (isAllTag) {
                            onSubtagSelected(null)
                        } else {
                            onSubtagSelected(tag)
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tag,
                    color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
