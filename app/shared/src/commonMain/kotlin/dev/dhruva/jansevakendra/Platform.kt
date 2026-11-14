package dev.dhruva.jansevakendra

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform