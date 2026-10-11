package com.example.fiscus.presentation.common.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val builder = StringBuilder()
        val originalToTransformed = IntArray(digits.length + 1)
        val transformedToOriginal = mutableListOf<Int>()

        digits.forEachIndexed { index, char ->
            if (index > 0 && (digits.length - index) % 3 == 0) {
                builder.append('.')
                transformedToOriginal += index
            }
            originalToTransformed[index] = builder.length
            builder.append(char)
            transformedToOriginal += index
        }
        originalToTransformed[digits.length] = builder.length
        transformedToOriginal += digits.length

        return TransformedText(
            AnnotatedString(builder.toString()),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = originalToTransformed[offset]
                override fun transformedToOriginal(offset: Int) = transformedToOriginal[offset]
            },
        )
    }
}
