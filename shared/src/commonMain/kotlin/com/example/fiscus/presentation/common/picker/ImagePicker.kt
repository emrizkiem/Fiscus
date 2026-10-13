package com.example.fiscus.presentation.common.picker

import androidx.compose.runtime.Composable

class ImagePickerLauncher(private val onLaunch: () -> Unit) {
    fun launch() = onLaunch()
}

@Composable
expect fun rememberImagePickerLauncher(onResult: (ByteArray?) -> Unit): ImagePickerLauncher