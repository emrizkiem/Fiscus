package com.example.fiscus.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.example.fiscus.platform.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.fileDataRepresentation
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.QuartzCore.CATransaction
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create
import kotlin.coroutines.resume

actual class CameraController {
    internal val session = AVCaptureSession()
    private val photoOutput = AVCapturePhotoOutput()
    private var captureDelegate: PhotoCaptureDelegate? = null
    private val sessionQueue = dispatch_queue_create("com.example.fiscus.camera", null)
    private var isConfigured = false

    internal fun start() {
        dispatch_async(sessionQueue) {
            if (!isConfigured) {
                configure()
                isConfigured = true
            }
            session.startRunning()
        }
    }

    internal fun stop() {
        dispatch_async(sessionQueue) { session.stopRunning() }
    }

    actual suspend fun capture(): ByteArray? {
        if (photoOutput.connectionWithMediaType(AVMediaTypeVideo) == null) return null

        return suspendCancellableCoroutine { continuation ->
            val delegate = PhotoCaptureDelegate { data ->
                captureDelegate = null
                continuation.resume(data?.toUprightJpeg())
            }
            captureDelegate = delegate
            photoOutput.capturePhotoWithSettings(AVCapturePhotoSettings.photoSettings(), delegate)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun configure() {
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) ?: return
        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, error = null) ?: return
        session.beginConfiguration()
        session.sessionPreset = AVCaptureSessionPresetPhoto
        if (session.canAddInput(input)) session.addInput(input)
        if (session.canAddOutput(photoOutput)) session.addOutput(photoOutput)
        session.commitConfiguration()
    }
}

private class PhotoCaptureDelegate(
    private val onFinish: (NSData?) -> Unit,
) : NSObject(), AVCapturePhotoCaptureDelegateProtocol {

    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?,
    ) {
        onFinish(if (error == null) didFinishProcessingPhoto.fileDataRepresentation() else null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toUprightJpeg(): ByteArray? {
    val image = UIImage.imageWithData(this) ?: return null
    val format = UIGraphicsImageRendererFormat().apply { scale = 1.0 }
    val renderer = UIGraphicsImageRenderer(size = image.size, format = format)
    val upright = renderer.imageWithActions { image.drawAtPoint(CGPointMake(0.0, 0.0)) }
    return UIImageJPEGRepresentation(upright, 0.9)?.toByteArray()
}

@Composable
actual fun rememberCameraController(): CameraController = remember { CameraController() }

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreview(controller: CameraController, modifier: Modifier) {
    DisposableEffect(controller) {
        controller.start()
        onDispose { controller.stop() }
    }

    UIKitView(
        factory = {
            val previewLayer = AVCaptureVideoPreviewLayer(session = controller.session).apply {
                videoGravity = AVLayerVideoGravityResizeAspectFill
            }
            PreviewContainerView(previewLayer)
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalForeignApi::class)
private class PreviewContainerView(
    private val previewLayer: AVCaptureVideoPreviewLayer,
) : UIView(frame = CGRectZero.readValue()) {

    init {
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        previewLayer.frame = bounds
        CATransaction.commit()
    }
}
