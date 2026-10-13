package com.example.fiscus.data.ocr

fun assembleLines(fragments: List<TextFragment>): List<String> {
    val rows = mutableListOf<MutableList<TextFragment>>()

    fragments
        .filter { it.text.isNotBlank() }
        .sortedBy { it.centerY }
        .forEach { fragment ->
            val row = rows.lastOrNull()
            if (row != null && isSameRow(row.first(), fragment)) {
                row += fragment
            } else {
                rows += mutableListOf(fragment)
            }
        }

    return rows.map { row -> row.sortedBy { it.left }.joinToString(" ") { it.text.trim() } }
}

private fun isSameRow(anchor: TextFragment, fragment: TextFragment): Boolean =
    fragment.centerY in anchor.top..anchor.bottom || anchor.centerY in fragment.top..fragment.bottom
