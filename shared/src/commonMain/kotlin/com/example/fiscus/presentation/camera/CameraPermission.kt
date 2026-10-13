package com.example.fiscus.presentation.camera

import androidx.compose.runtime.Composable

enum class CameraPermissionStatus { Granted, NotRequested, Denied }

interface CameraPermissionState {
    val status: CameraPermissionStatus
    fun request()
    fun openSettings()
    fun refresh()
}

@Composable
expect fun rememberCameraPermissionState(): CameraPermissionState
