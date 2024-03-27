package com.example

import com.example.plugins.configureDatabase
import com.example.plugins.configureRouting
import com.example.plugins.configureSecurity
import com.example.plugins.configureSerialization
import com.example.workers.ytVideoHistoryCollectWorker
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import kotlin.coroutines.EmptyCoroutineContext

val appCoroutineScope = CoroutineScope(EmptyCoroutineContext)

fun main() {
    embeddedServer(Netty, commandLineEnvironment(emptyArray()))
        .start(wait = true)
}

fun Application.module() {
    configureDatabase()
    configureSecurity(environment.config)
    configureSerialization()
    configureRouting(environment.config)

    ytVideoHistoryCollectWorker.startToCollectHistoryForYTVideos()
}

fun generateHash(): String {
    val Qkb = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
    val a = CharArray(36)
    var b = 0
    var c: Int
    for (e in 0 until 36) {
        when (e) {
            8, 13, 18, 23 -> a[e] = '-'
            14 -> a[e] = '4'
            else -> {
                if (b <= 2) {
                    b = 33554432 + (16777216 * Math.random()).toInt()
                }
                c = b and 15
                b = b shr 4
                a[e] = if (e == 19) Qkb[c and 3 or 8] else Qkb[c]
            }
        }
    }
    return String(a)
}