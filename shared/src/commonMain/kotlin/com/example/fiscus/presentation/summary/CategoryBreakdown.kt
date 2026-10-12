package com.example.fiscus.presentation.summary

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fiscus.domain.model.CategoryTotal
import com.example.fiscus.presentation.common.component.CategoryIcon
import com.example.fiscus.presentation.common.theme.FiscusTheme
import com.example.fiscus.presentation.common.util.formatRupiah
import com.example.fiscus.presentation.common.util.label

@Composable
fun CategoryBreakdown(
    categoryTotals: List<CategoryTotal>,
    modifier: Modifier = Modifier
) {
    val maxTotal = categoryTotals.maxOfOrNull { it.total }?.takeIf { it > 0 } ?: 1L

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        categoryTotals.forEach { item ->
            CategoryBar(item = item, fraction = item.total.toFloat() / maxTotal)
        }
    }
}

@Composable
private fun CategoryBar(item: CategoryTotal, fraction: Float) {
    val tint = FiscusTheme.categoryColors.of(item.category)
    val animatedFraction by animateFloatAsState(targetValue = fraction)

    Row(verticalAlignment = Alignment.CenterVertically) {
        CategoryIcon(item.category)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.category.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text(formatRupiah(item.total), style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(tint.container, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction)
                        .fillMaxHeight()
                        .background(tint.content, CircleShape)
                )
            }
        }
    }
}