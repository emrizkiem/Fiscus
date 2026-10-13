package com.example.fiscus.data.ocr

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class ReceiptScanner actual constructor() {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    actual suspend fun scanText(imageBytes: ByteArray): List<TextFragment> {
        val bitmap = withContext(Dispatchers.Default) {
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } ?: return emptyList()
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val image = InputImage.fromBitmap(bitmap, 0)

        val result = suspendCancellableCoroutine<Text> { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { text -> continuation.resume(text) }
                .addOnFailureListener { error -> continuation.resumeWithException(error) }
        }

        return result.textBlocks
            .flatMap { block -> block.lines }
            .mapNotNull { line ->
                val box = line.boundingBox ?: return@mapNotNull null
                TextFragment(
                    text = line.text,
                    left = box.left / width,
                    top = box.top / height,
                    bottom = box.bottom / height
                )
            }
    }
}