package org.example.priroda_razuma

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform