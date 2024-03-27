package com.example.plugins

import com.example.routes.*
import com.example.services.marketService
import com.example.services.userService
import com.example.services.ytVideoService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(config: ApplicationConfig) {
    routing {
        get("/") {
            call.respondRedirect("/login")
        }
        authenticate("auth-jwt") {
            ytRouting(config, userService)
            marketParseRouting(config, marketService)
            ytVideosRoutes(config, ytVideoService)
        }
        authRouting(config, userService)
        uiRoutes(ytVideoService)
    }
}
