package com.example.fiscus.presentation.camera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.fiscus.domain.model.ExpenseSource
import com.example.fiscus.presentation.addexpense.AddExpenseScreen
import com.example.fiscus.presentation.common.util.PendingReceipt
import fiscus.shared.generated.resources.Res
import fiscus.shared.generated.resources.ic_arrow_back
import fiscus.shared.generated.resources.ic_photo_camera
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

class CameraScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val permission = rememberCameraPermissionState()
        val pendingReceipt = koinInject<PendingReceipt>()

        LaunchedEffect(Unit) {
            if (permission.status == CameraPermissionStatus.NotRequested) permission.request()
        }
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { permission.refresh() }

        CameraContent(
            status = permission.status,
            onBack = { navigator.pop() },
            onRequestPermission = permission::request,
            onOpenSettings = permission::openSettings,
            onCaptured = { bytes ->
                pendingReceipt.put(bytes, ExpenseSource.OCR_CAMERA)
                navigator.replace(AddExpenseScreen(isReceiptReview = true))
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraContent(
    status: CameraPermissionStatus,
    onBack: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onCaptured: (ByteArray) -> Unit,
) {
    if (status == CameraPermissionStatus.Granted) {
        CameraViewfinder(onBack = onBack, onCaptured = onCaptured)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Kembali")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            if (status == CameraPermissionStatus.NotRequested) {
                PermissionMessage(
                    title = "Izinkan akses kamera",
                    description = "Fiscus butuh kamera untuk memotret struk supaya nominal, merchant, dan tanggal terbaca otomatis.",
                    actionLabel = "Izinkan kamera",
                    onAction = onRequestPermission,
                )
            } else {
                PermissionMessage(
                    title = "Akses kamera ditolak",
                    description = "Aktifkan izin kamera untuk Fiscus di Pengaturan, lalu kembali ke sini. Kamu tetap bisa menambah struk lewat galeri atau isi manual.",
                    actionLabel = "Buka Pengaturan",
                    onAction = onOpenSettings,
                )
            }
        }
    }
}

@Composable
private fun PermissionMessage(
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.size(72.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painterResource(Res.drawable.ic_photo_camera), contentDescription = null, modifier = Modifier.size(32.dp))
            }
        }
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onAction, shape = MaterialTheme.shapes.medium, modifier = Modifier.height(44.dp)) {
            Text(actionLabel, style = MaterialTheme.typography.labelLarge)
        }
    }
}
