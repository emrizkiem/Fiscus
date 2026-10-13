package com.example.fiscus.presentation.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fiscus.shared.generated.resources.Res
import fiscus.shared.generated.resources.ic_arrow_back
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.launch

private val OverlayContent = Color.White
private val OverlayScrim = Color.Black.copy(alpha = 0.5f)
private val ShutterSize = 76.dp
private val ShutterBottomPadding = 40.dp
private val GuideHorizontalInset = 28.dp
private val GuideTopInset = 108.dp
private val GuideBottomInset = ShutterBottomPadding + ShutterSize + 24.dp

@Composable
internal fun CameraViewfinder(
    onBack: () -> Unit,
    onCaptured: (ByteArray) -> Unit,
) {
    val controller = rememberCameraController()
    val scope = rememberCoroutineScope()
    var isCapturing by remember { mutableStateOf(false) }
    var captureFailed by remember { mutableStateOf(false) }
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxSize().statusBarsPadding().background(Color.Black)) {
            CameraPreview(controller, Modifier.fillMaxSize())
            ReceiptGuideOverlay(bottomInset = GuideBottomInset + navigationBarBottom, modifier = Modifier.fillMaxSize())

            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp).clip(CircleShape).background(OverlayScrim),
            ) {
                Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Kembali", tint = OverlayContent)
            }

            Text(
                text = if (captureFailed) "Gagal memotret. Coba lagi." else "Posisikan struk di dalam bingkai",
                style = MaterialTheme.typography.bodyMedium,
                color = OverlayContent,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 64.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(OverlayScrim)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            )

            ShutterButton(
                isCapturing = isCapturing,
                onClick = {
                    isCapturing = true
                    captureFailed = false
                    scope.launch {
                        val bytes = controller.capture()
                        isCapturing = false
                        if (bytes != null) onCaptured(bytes) else captureFailed = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = ShutterBottomPadding),
            )
        }
    }
}

@Composable
private fun ReceiptGuideOverlay(bottomInset: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
        val left = GuideHorizontalInset.toPx()
        val top = GuideTopInset.toPx()
        val frameSize = Size(
            width = size.width - left * 2,
            height = size.height - top - bottomInset.toPx(),
        )
        if (frameSize.width <= 0f || frameSize.height <= 0f) return@Canvas
        val topLeft = Offset(left, top)
        val radius = CornerRadius(20.dp.toPx())

        drawRect(OverlayScrim)
        drawRoundRect(Color.Transparent, topLeft, frameSize, radius, blendMode = BlendMode.Clear)
        drawRoundRect(OverlayContent, topLeft, frameSize, radius, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
private fun ShutterButton(
    isCapturing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ShutterSize)
            .border(4.dp, OverlayContent, CircleShape)
            .padding(8.dp)
            .clip(CircleShape)
            .background(if (isCapturing) OverlayContent.copy(alpha = 0.6f) else OverlayContent)
            .clickable(enabled = !isCapturing, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Ambil foto" },
        contentAlignment = Alignment.Center,
    ) {
        if (isCapturing) {
            CircularProgressIndicator(modifier = Modifier.size(26.dp), color = Color.Black, strokeWidth = 3.dp)
        }
    }
}
