package com.example.fiscus.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect class CameraController {
    suspend fun capture(): ByteArray?
}

@Composable
expect fun rememberCameraController(): CameraController

@Composable
expect fun CameraPreview(controller: CameraController, modifier: Modifier = Modifier)
