package com.example.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

class TokenManager(appConfig: ApplicationConfig) {

    private val audience = appConfig.property("ktor.security.jwt.audience").getString()
    private val secret = appConfig.property("ktor.security.jwt.secret").getString()
    private val issuer = appConfig.property("ktor.security.jwt.issuer").getString()
    private val expirationPeriod = appConfig.property("ktor.security.jwt.expiration_time").getString().toLong()
    private val algorithm = Algorithm.HMAC512(secret)

    fun generateToken(username: String): String =
        JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", username)
            .withExpiresAt(getExpirationTime())
            .sign(algorithm)

    private fun getExpirationTime() = Date(System.currentTimeMillis() + expirationPeriod)
}