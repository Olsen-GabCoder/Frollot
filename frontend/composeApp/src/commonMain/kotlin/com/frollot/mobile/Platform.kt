package com.frollot.mobile

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
