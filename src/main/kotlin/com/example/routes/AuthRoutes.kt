package com.example.routes

import com.example.utils.V1_API_PATH
import com.example.models.ServiceUser
import com.example.services.UserService
import com.example.utils.TokenManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRouting(config: ApplicationConfig, userService: UserService) {

    val tokenManager = TokenManager(config)

    post("$V1_API_PATH/login") {

        val loginRequest = call.receive<ServiceUser>()

        val user = userService.loginUser(loginRequest.login, loginRequest.password)

        if (user != null) {
            if (user) call.respond(hashMapOf("token" to tokenManager.generateToken(loginRequest.login)))
            else {
                call.respond(HttpStatusCode.Unauthorized, "username/password is wrong")
            }
        } else {
            call.respond(HttpStatusCode.NotFound, "username not found")
        }
    }

    post("$V1_API_PATH/signup") {
        val serviceUser = call.receive<ServiceUser>()
        userService.createServiceUser(serviceUser)
        call.respond(HttpStatusCode.Created, "User singed up")
    }
}