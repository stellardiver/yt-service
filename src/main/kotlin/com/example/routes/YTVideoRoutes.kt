package com.example.routes

import com.example.appCoroutineScope
import com.example.utils.V1_API_PATH
import com.example.models.YTVideo
import com.example.models.YTVideoHistorySlice
import com.example.services.YTVideoService
import com.example.utils.prettyPrint
import com.example.utils.safeReceive
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.regex.Pattern

fun Route.ytVideosRoutes(config: ApplicationConfig, ytVideoService: YTVideoService) {

    get("$V1_API_PATH/get_yt_videos") {

        val pageParam = call.parameters["page"]?.toIntOrNull()

        // TODO: handle query params and return json with yt videos

        println(pageParam)

        call.respond(hashMapOf("success" to true))
    }

    post("$V1_API_PATH/update_yt_videos") {

        val ytVideos = call.safeReceive<Array<YTVideo>>()

        call.respond(hashMapOf("success" to true))

        ytVideos.forEach { ytVideo ->
            ytVideoService.saveYTVideo(ytVideo)
        }
    }

    get("$V1_API_PATH/get_yt_video_history") {

        call.respond(hashMapOf("success" to true))

    }
}