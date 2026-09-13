package com.example.fiscus

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform