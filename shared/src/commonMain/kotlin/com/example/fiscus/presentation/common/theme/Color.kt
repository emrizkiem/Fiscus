package com.example.fiscus.presentation.common.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.fiscus.domain.model.Category

internal val LightColors = lightColorScheme(
    primary = Color(0xFF2B7BD9),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E6FB),
    onPrimaryContainer = Color(0xFF1A4F91),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1A),
    onSurfaceVariant = Color(0xFF75736F),
    surfaceContainerLow = Color(0xFFF7F6F3),
    surfaceContainer = Color(0xFFF1F0EC),
    outlineVariant = Color(0xFFE4E2DD),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB8F0),
    onPrimary = Color(0xFF0B2F57),
    primaryContainer = Color(0xFF1C3B60),
    onPrimaryContainer = Color(0xFFD6E6FB),
    background = Color(0xFF121314),
    onBackground = Color(0xFFE6E5E2),
    surface = Color(0xFF121314),
    onSurface = Color(0xFFE6E5E2),
    onSurfaceVariant = Color(0xFFA19F9A),
    surfaceContainerLow = Color(0xFF1C1D1F),
    surfaceContainer = Color(0xFF242528),
    outlineVariant = Color(0xFF34363A),
)

@Immutable
data class CategoryTint(val container: Color, val content: Color)

@Immutable
data class CategoryColors(
    val food: CategoryTint,
    val shopping: CategoryTint,
    val transport: CategoryTint,
    val bills: CategoryTint,
    val health: CategoryTint,
    val entertainment: CategoryTint,
    val other: CategoryTint,
) {
    fun of(category: Category): CategoryTint = when (category) {
        Category.FOOD -> food
        Category.SHOPPING -> shopping
        Category.TRANSPORT -> transport
        Category.BILLS -> bills
        Category.HEALTH -> health
        Category.ENTERTAINMENT -> entertainment
        Category.OTHER -> other
    }
}

internal val LightCategoryColors = CategoryColors(
    food = CategoryTint(Color(0xFFFBE3B4), Color(0xFF8A5A00)),
    shopping = CategoryTint(Color(0xFFD3EDD1), Color(0xFF2E7D32)),
    transport = CategoryTint(Color(0xFFD6E6FB), Color(0xFF1A5FB4)),
    bills = CategoryTint(Color(0xFFFADADA), Color(0xFFB3261E)),
    health = CategoryTint(Color(0xFFCDEEEA), Color(0xFF00796B)),
    entertainment = CategoryTint(Color(0xFFE6DDF8), Color(0xFF6A3FB5)),
    other = CategoryTint(Color(0xFFECEAE6), Color(0xFF5F5D59)),
)

internal val DarkCategoryColors = CategoryColors(
    food = CategoryTint(Color(0xFF4A3812), Color(0xFFF6C768)),
    shopping = CategoryTint(Color(0xFF1F3D21), Color(0xFF8FD18F)),
    transport = CategoryTint(Color(0xFF1C3B60), Color(0xFF8AB8F0)),
    bills = CategoryTint(Color(0xFF4D1F1D), Color(0xFFF2A39D)),
    health = CategoryTint(Color(0xFF113E3A), Color(0xFF7FD5CA)),
    entertainment = CategoryTint(Color(0xFF35284F), Color(0xFFC5B0F0)),
    other = CategoryTint(Color(0xFF2E2F31), Color(0xFFB9B7B2)),
)
