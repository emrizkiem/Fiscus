package com.example.fiscus.presentation.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState {
    val context = LocalContext.current
    val state = remember { AndroidCameraPermissionState(context) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        state.onRequestResult(granted)
    }
    state.requestPermission = { launcher.launch(Manifest.permission.CAMERA) }
    return state
}

private class AndroidCameraPermissionState(private val context: Context) : CameraPermissionState {
    override var status: CameraPermissionStatus by mutableStateOf(
        if (isGranted()) CameraPermissionStatus.Granted else CameraPermissionStatus.NotRequested,
    )
        private set

    var requestPermission: () -> Unit = {}

    override fun request() = requestPermission()

    override fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun refresh() {
        status = when {
            isGranted() -> CameraPermissionStatus.Granted
            status == CameraPermissionStatus.Granted -> CameraPermissionStatus.Denied
            else -> status
        }
    }

    fun onRequestResult(granted: Boolean) {
        status = if (granted) CameraPermissionStatus.Granted else CameraPermissionStatus.Denied
    }

    private fun isGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}
