package com.example.fiscus.presentation.common.util

import com.example.fiscus.domain.model.Category

val Category.label: String
    get() = when (this) {
        Category.FOOD -> "Makanan"
        Category.SHOPPING -> "Belanja"
        Category.TRANSPORT -> "Transport"
        Category.BILLS -> "Tagihan"
        Category.HEALTH -> "Kesehatan"
        Category.ENTERTAINMENT -> "Hiburan"
        Category.OTHER -> "Lainnya"
    }
