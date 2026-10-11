package com.example.fiscus.presentation.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

private val LocalCategoryColors = staticCompositionLocalOf { LightCategoryColors }

internal val FiscusShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
)

@Composable
fun FiscusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalCategoryColors provides if (darkTheme) DarkCategoryColors else LightCategoryColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = FiscusTypography,
            shapes = FiscusShapes,
            content = content,
        )
    }
}

object FiscusTheme {
    val categoryColors: CategoryColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCategoryColors.current
}
