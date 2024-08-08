package com.bamsecoin.voffcloud

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform