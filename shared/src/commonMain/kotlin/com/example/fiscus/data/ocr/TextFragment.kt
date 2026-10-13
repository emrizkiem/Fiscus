package com.example.fiscus.data.ocr

data class TextFragment(
    val text: String,
    val left: Float,
    val top: Float,
    val bottom: Float
) {
    val centerY: Float get() = (top + bottom) / 2
}
