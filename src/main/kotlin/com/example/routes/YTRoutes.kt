package com.example.routes

import com.example.utils.V1_API_PATH
import com.example.models.YTUser
import com.example.services.UserService
import com.example.workers.ytUserDataCollectWorker
import com.example.utils.prettyPrint
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.ytRouting(config: ApplicationConfig, userService: UserService) {

    post("$V1_API_PATH/add_yt_users") {

        val multipartData = call.receiveMultipart()

        multipartData.forEachPart { part ->

            when (part) {

                is PartData.FileItem -> {

                    val fileName = part.originalFileName as String
                    val fileBytes = part.streamProvider().readBytes()
                    val file = File("uploads/$fileName").apply {
                        writeBytes(fileBytes)
                    }

                    val userDataList = mutableListOf<YTUser>()

                    file.forEachLine { line ->

                        println("Reading line: $line")

                        val parts = line.split(":")

                        if (parts.size >= 3) {

                            val userEmail = parts[0]
                            val password = parts[1]
                            val recoveryEmail = parts[2]

                            userDataList.add(
                                YTUser(
                                    email = userEmail,
                                    password = password,
                                    recoveryEmail = recoveryEmail
                                )
                            )
                        }
                    }

                    userDataList.forEach { user ->

                        println(user.prettyPrint())

                        userService.createYTUser(
                            ytUser = user
                        )
                     }

                }

                else -> {}
            }
            part.dispose()
        }

        call.respond(hashMapOf("success" to "true"))
    }

    post("$V1_API_PATH/update_all_yt_users_data") {
        if (ytUserDataCollectWorker.isWorking.get())
            call.respond(HttpStatusCode.BadRequest, "Service is not available right now")
        else {
            call.respond(hashMapOf("success" to "true"))
            ytUserDataCollectWorker.collectYTUsersData()
        }
    }
}