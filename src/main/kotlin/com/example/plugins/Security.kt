package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*

fun Application.configureSecurity(appConfig: ApplicationConfig) {
    install(Authentication) {
        jwt("auth-jwt") {

            realm = appConfig.property("ktor.security.jwt.realm").getString()

            val secret = appConfig.property("ktor.security.jwt.secret").getString()
            val issuer = appConfig.property("ktor.security.jwt.issuer").getString()
            val audience = appConfig.property("ktor.security.jwt.audience").getString()

            verifier(
                JWT.require(Algorithm.HMAC512(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )

            validate { credential ->
                if (!credential.payload.getClaim("username").asString().isNullOrEmpty())
                    JWTPrincipal(credential.payload)
                else null
            }
        }
    }
}