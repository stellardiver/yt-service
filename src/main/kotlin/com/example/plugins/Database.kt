package com.example.plugins

import com.example.dao.DatabaseFactory
import io.ktor.server.application.*

fun Application.configureDatabase() {
    DatabaseFactory.init(environment.config)
}