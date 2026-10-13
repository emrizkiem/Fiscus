package com.example.fiscus.data.ocr

import com.example.fiscus.platform.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Vision.VNImageRequestHandler
import platform.Vision.VNRecognizeTextRequest
import platform.Vision.VNRecognizedText
import platform.Vision.VNRecognizedTextObservation
import platform.Vision.VNRequestTextRecognitionLevelAccurate
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class ReceiptScanner actual constructor() {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun scanText(imageBytes: ByteArray): List<TextFragment> = withContext(Dispatchers.IO) {
        if (imageBytes.isEmpty()) return@withContext emptyList()

        suspendCancellableCoroutine { continuation ->
            val request = VNRecognizeTextRequest { request, error ->
                if (error != null) {
                    continuation.resumeWithException(IllegalStateException(error.localizedDescription))
                    return@VNRecognizeTextRequest
                }
                val fragments = request?.results.orEmpty()
                    .filterIsInstance<VNRecognizedTextObservation>()
                    .mapNotNull { observation ->
                        val text = (observation.topCandidates(1u).firstOrNull() as? VNRecognizedText)?.string
                            ?: return@mapNotNull null
                        observation.boundingBox.useContents {
                            TextFragment(
                                text = text,
                                left = origin.x.toFloat(),
                                top = (1.0 - origin.y - size.height).toFloat(),
                                bottom = (1.0 - origin.y).toFloat(),
                            )
                        }
                    }
                continuation.resume(fragments)
            }
            request.recognitionLevel = VNRequestTextRecognitionLevelAccurate
            request.usesLanguageCorrection = true

            val handler = VNImageRequestHandler(data = imageBytes.toNSData(), options = emptyMap<Any?, Any?>())
            handler.performRequests(listOf(request), error = null)
        }
    }
}
