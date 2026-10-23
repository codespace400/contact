package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity

@Composable
fun CategoryBadge(
    category: CategoryEntity?,
    modifier: Modifier = Modifier
) {
    if (category == null) return

    val (bg, fg) = remember(category.name, category.colorHex) {
        val parsed = try {
            Color(android.graphics.Color.parseColor(category.colorHex))
        } catch (_: Exception) {
            Color(0xFF3B82F6)
        }
        val bgTint = parsed.copy(alpha = 0.14f)
        Pair(bgTint, parsed)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = category.name,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
