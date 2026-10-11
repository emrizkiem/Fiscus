package com.example.fiscus.presentation.common.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fiscus.domain.model.Category
import com.example.fiscus.presentation.common.theme.FiscusTheme
import fiscus.shared.generated.resources.Res
import fiscus.shared.generated.resources.ic_bolt
import fiscus.shared.generated.resources.ic_coffee
import fiscus.shared.generated.resources.ic_directions_car
import fiscus.shared.generated.resources.ic_more_horiz
import fiscus.shared.generated.resources.ic_movie
import fiscus.shared.generated.resources.ic_pill
import fiscus.shared.generated.resources.ic_shopping_cart
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

val Category.icon: DrawableResource
    get() = when (this) {
        Category.FOOD -> Res.drawable.ic_coffee
        Category.SHOPPING -> Res.drawable.ic_shopping_cart
        Category.TRANSPORT -> Res.drawable.ic_directions_car
        Category.BILLS -> Res.drawable.ic_bolt
        Category.HEALTH -> Res.drawable.ic_pill
        Category.ENTERTAINMENT -> Res.drawable.ic_movie
        Category.OTHER -> Res.drawable.ic_more_horiz
    }

@Composable
fun CategoryIcon(category: Category, modifier: Modifier = Modifier) {
    val tint = FiscusTheme.categoryColors.of(category)
    Surface(
        color = tint.container,
        contentColor = tint.content,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.size(34.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(category.icon), contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}
