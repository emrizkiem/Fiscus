package com.example.fiscus.presentation.common.util

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals

class ThousandSeparatorTransformationTest {
    private fun transform(digits: String) = ThousandSeparatorTransformation.filter(AnnotatedString(digits))

    @Test
    fun insertsDotEveryThreeDigitsFromTheRight() {
        assertEquals("", transform("").text.text)
        assertEquals("500", transform("500").text.text)
        assertEquals("2.500", transform("2500").text.text)
        assertEquals("62.500", transform("62500").text.text)
        assertEquals("1.842.500", transform("1842500").text.text)
    }

    @Test
    fun cursorPositionsMapBothWays() {
        val mapping = transform("62500").offsetMapping

        assertEquals(listOf(0, 1, 3, 4, 5, 6), (0..5).map { mapping.originalToTransformed(it) })
        assertEquals(listOf(0, 1, 2, 2, 3, 4, 5), (0..6).map { mapping.transformedToOriginal(it) })
    }
}
