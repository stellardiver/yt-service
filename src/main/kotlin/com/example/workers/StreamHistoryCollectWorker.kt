package com.example.workers

import com.example.appCoroutineScope
import com.example.models.browse.Client
import com.example.models.browse.Context
import com.example.models.next.NextRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch

class StreamHistoryCollectWorker() {

    companion object {

        @Volatile
        private var instance: StreamHistoryCollectWorker? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: StreamHistoryCollectWorker().also { instance = it }
            }
    }

    fun collectStreamHistory(stream: JsonObject) {

        appCoroutineScope.launch {

            val client = HttpClient()

            client.post(
                "https://www.youtube.com/youtubei/v1/next?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8&prettyPrint=false"
            ) {

                val gson = Gson().toJson(
                    NextRequest(
                        context = Context(
                            client = Client()
                        ),
                        videoId = stream.get("videoId").asString
                    )
                )

                contentType(ContentType.Application.Json)
                setBody(gson)

            }.let { response ->

                val jsonResponse = response.body<String>()
                val jsonParsedElement = JsonParser.parseString(jsonResponse)

                val contentsArray = jsonParsedElement.asJsonObject
                    .getAsJsonObject("contents")
                    .getAsJsonObject("twoColumnWatchNextResults")
                    .getAsJsonObject("results")
                    .getAsJsonObject("results")
                    .getAsJsonArray("contents")

                if (!contentsArray.isEmpty) {

                    val videoPrimaryInfoRenderer: JsonObject? = contentsArray.get(0).asJsonObject
                        ?.getAsJsonObject("videoPrimaryInfoRenderer")

                    videoPrimaryInfoRenderer?.let {

                        val isLive: Boolean? = videoPrimaryInfoRenderer
                            .getAsJsonObject("viewCount")
                            .getAsJsonObject("videoViewCountRenderer")
                            .get("isLive")?.asBoolean

                        isLive?.let {

                            // TODO: stream is online

                        }?: run {

                            // TODO: stream is offline
                        }
                    }
                }
            }
        }
    }
}

val streamHistoryCollectWorker = StreamHistoryCollectWorker.getInstance()