package com.example.workers

import com.example.appCoroutineScope
import com.example.dao.UserDao
import com.example.models.browse.BrowseRequest
import com.example.models.browse.Client
import com.example.models.browse.Context
import com.example.models.search.SearchRequest
import com.example.utils.ChannelTabs
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch

class StreamKeysAndChannelsWorker() {

    companion object {

        @Volatile
        private var instance: StreamKeysAndChannelsWorker? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: StreamKeysAndChannelsWorker().also { instance = it }
            }
    }

    private fun collectStreamsForKey(key: String) {

        appCoroutineScope.launch {

            val client = HttpClient()

            val response = client.post(
                "https://www.youtube.com/youtubei/v1/search?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8&prettyPrint=false"
            ) {

                val gson = Gson().toJson(
                    SearchRequest(
                        context = Context(
                            client = Client()
                        ),
                        query = key
                    )
                )

                contentType(ContentType.Application.Json)
                setBody(gson)

            }.let { response ->

                // TODO: create new data class for stream

                val streamsOnline = mutableListOf<JsonObject>()
                val jsonResponse = response.body<String>()
                val jsonParsedElement = JsonParser.parseString(jsonResponse)

                val contentsArray = jsonParsedElement.asJsonObject
                    .getAsJsonObject("contents")
                    .getAsJsonObject("twoColumnSearchResultsRenderer")
                    .getAsJsonObject("primaryContents")
                    .getAsJsonObject("sectionListRenderer")
                    .getAsJsonArray("contents")
                    .get(0).asJsonObject
                    .getAsJsonObject("itemSectionRenderer")
                    .getAsJsonArray("contents")

                contentsArray.forEach { content ->
                    val videoRenderer: JsonObject? = content.asJsonObject
                        ?.getAsJsonObject("videoRenderer")

                    videoRenderer?.let {

                        val badges: JsonArray? = videoRenderer
                            .getAsJsonArray("badges")
                            ?.get(0)?.asJsonArray

                        badges?.forEach { badge ->

                            val badgeStyle: String = badge.asJsonObject
                                .getAsJsonObject("metadataBadgeRenderer")
                                .get("style").asString

                            if (badgeStyle == "BADGE_STYLE_TYPE_LIVE_NOW") {
                                streamsOnline.add(videoRenderer)
                            }
                        }
                    }
                }
            }

            client.close()

            // TODO: save online streams to db
        }
    }

    private fun checkChannelForLiveStreams(channelId: String) {

        appCoroutineScope.launch {

            val client = HttpClient()

            val response = client.post(
                "https://www.youtube.com/youtubei/v1/browse?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8&prettyPrint=false"
            ) {

                val gson = Gson().toJson(
                    BrowseRequest(
                        context = Context(
                            client = Client()
                        ),
                        browseId = channelId,
                        params = ChannelTabs.STREAMS
                    )
                )

                contentType(ContentType.Application.Json)
                setBody(gson)

            }.let { response ->

                val streamsOnline = mutableListOf<JsonObject>()
                val jsonResponse = response.body<String>()
                val jsonParsedElement = JsonParser.parseString(jsonResponse)

                val contentsArray = jsonParsedElement.asJsonObject
                    .getAsJsonObject("contents")
                    .getAsJsonObject("twoColumnBrowseResultsRenderer")
                    .getAsJsonArray("tabs")
                    .get(3).asJsonObject
                    .getAsJsonObject("tabRenderer")
                    .getAsJsonObject("content")
                    .getAsJsonObject("richGridRenderer")
                    .getAsJsonArray("contents")


                // TODO: take only first video for stream?

                contentsArray.forEach { content ->

                    val videoRenderer: JsonObject? = content.asJsonObject
                        .getAsJsonObject("richItemRenderer")
                        .getAsJsonObject("content")
                        .getAsJsonObject("videoRenderer")

                    videoRenderer?.let {

                        val videoOverlayStyle = videoRenderer
                            .getAsJsonArray("thumbnailOverlays")
                            .get(0).asJsonObject
                            .getAsJsonObject("thumbnailOverlayTimeStatusRenderer")
                            .get("style").asString

                        if (videoOverlayStyle == "LIVE") {
                            streamsOnline.add(videoRenderer)
                        }
                    }
                }

            }

            client.close()

            // TODO: save online streams to db
        }
    }
}

val streamKeysAndChannelsWorker = StreamKeysAndChannelsWorker.getInstance()