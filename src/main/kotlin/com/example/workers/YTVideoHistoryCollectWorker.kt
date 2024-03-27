package com.example.workers

import com.example.appCoroutineScope
import com.example.dao.YTVideoDao
import com.example.dao.ytVideoDao
import com.example.models.YTVideo
import com.example.models.YTVideoHistorySlice
import com.example.models.browse.BrowseRequest
import com.example.models.browse.Client
import com.example.models.browse.Context
import com.example.models.isTrending
import com.example.services.ytVideoService
import com.example.utils.findLinks
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class YTVideoHistoryCollectWorker private constructor(private val repository: YTVideoDao) {

    private val collectHistoryJobs: HashMap<String, Job> = hashMapOf()

    companion object {

        @Volatile
        private var instance: YTVideoHistoryCollectWorker? = null

        fun getInstance(repository: YTVideoDao) =
            instance ?: synchronized(this) {
                instance ?: YTVideoHistoryCollectWorker(repository).also { instance = it }
            }
    }

    private fun collectHistory(ytVideo: YTVideo) {

        val historySlice = YTVideoHistorySlice(
            videoId = ytVideo.videoId,
            historyTimeStamp = System.currentTimeMillis()
        )

        appCoroutineScope.launch {

            val client = HttpClient()

            client.get(ytVideo.videoUrl) {
                header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:85.0) Gecko/20100101 Firefox/85.0")
                header("Cookie", "CONSENT:YES+1;")
                header("Accept-Language", "en-US;q=0.5")
            }.let { response ->

                val htmlString = response.body<String>()
                val document: Document = Jsoup.parse(htmlString)

                val scriptElements = document.getElementsByTag("script")
                    .map { it.html() }

                for (scriptElement in scriptElements) {

                    if (scriptElement.contains("ytInitialData")) {

                        val start = scriptElement.indexOf('{')
                        val end = scriptElement.lastIndexOf('}')

                        if (start != -1 && end != -1 && start < end) {

                            runCatching {

                                val jsonString = scriptElement.substring(start, end + 1)
                                val jsonElement = JsonParser.parseString(jsonString)

                                val itemsArray = jsonElement.asJsonObject
                                    .getAsJsonArray("engagementPanels")
                                    .get(1).asJsonObject
                                    .getAsJsonObject("engagementPanelSectionListRenderer")
                                    .getAsJsonObject("content")
                                    .getAsJsonObject("structuredDescriptionContentRenderer")
                                    .getAsJsonArray("items")

                                var fullDescriptionText = ""

                                val continuationCommandToken = jsonElement.asJsonObject
                                    .getAsJsonArray("engagementPanels")
                                    .get(0).asJsonObject
                                    .getAsJsonObject("engagementPanelSectionListRenderer")
                                    .getAsJsonObject("header")
                                    .getAsJsonObject("engagementPanelTitleHeaderRenderer")
                                    .getAsJsonObject("menu")
                                    .getAsJsonObject("sortFilterSubMenuRenderer")
                                    .getAsJsonArray("subMenuItems")
                                    .get(0).asJsonObject
                                    .getAsJsonObject("serviceEndpoint")
                                    .getAsJsonObject("continuationCommand")
                                    .get("token").asString

                                val commentCountString = jsonElement.asJsonObject
                                    .getAsJsonObject("overlay")
                                    .getAsJsonObject("reelPlayerOverlayRenderer")
                                    .getAsJsonObject("viewCommentsButton")
                                    .getAsJsonObject("buttonRenderer")
                                    .getAsJsonObject("text")
                                    .get("simpleText").asString

                                val likeCount: Int? = jsonElement.asJsonObject
                                    .getAsJsonObject("overlay")
                                    .getAsJsonObject("reelPlayerOverlayRenderer")
                                    .getAsJsonObject("likeButton")
                                    .getAsJsonObject("likeButtonRenderer")
                                    .get("likeCount")?.asInt

                                val viewCountString = jsonElement.asJsonObject
                                    .getAsJsonArray("engagementPanels")
                                    .get(1).asJsonObject
                                    .getAsJsonObject("engagementPanelSectionListRenderer")
                                    .getAsJsonObject("content")
                                    .getAsJsonObject("structuredDescriptionContentRenderer")
                                    .getAsJsonArray("items")
                                    .get(0).asJsonObject
                                    .getAsJsonObject("videoDescriptionHeaderRenderer")
                                    .getAsJsonObject("views")
                                    .get("simpleText").asString

                                val regexForCommentCount = Regex("""(\d+(\.\d+)?)[KM]?""")

                                val matchResultForCommentCount = regexForCommentCount.find(commentCountString)

                                if (matchResultForCommentCount != null) {
                                    val numberString = matchResultForCommentCount.groupValues[1]
                                    val multiplier = when {
                                        "K" in commentCountString -> 1000
                                        "M" in commentCountString -> 1000000
                                        else -> 1
                                    }

                                    historySlice.commentCount = (numberString.toDouble() * multiplier).toInt()
                                } else {
                                    println("Число комментов не найдено.")
                                }

                                val regexForViewCount = Regex("""([\d,]+) views""")
                                val matchResultForViewCount = regexForViewCount.find(viewCountString)

                                if (matchResultForViewCount != null) {
                                    val numberString = matchResultForViewCount.groupValues[1]
                                    historySlice.viewCount = numberString.replace(",", "").toInt()
                                } else {
                                    println("Число просмотров не найдено.")
                                }

                                likeCount?.let {
                                    historySlice.likeCount = likeCount
                                }

                                println("commentsCount: ${historySlice.commentCount}, likesCount: ${historySlice.likeCount}, viewsCount: ${historySlice.viewCount}")


                                if (itemsArray.size() > 1) {

                                    val descriptionArray = itemsArray.get(1).asJsonObject
                                        .getAsJsonObject("expandableVideoDescriptionBodyRenderer")
                                        .getAsJsonObject("descriptionBodyText")
                                        .getAsJsonArray("runs")

                                    descriptionArray?.forEach { descriptionElement ->

                                        val descriptionText = descriptionElement.asJsonObject
                                            .get("text").asString

                                        fullDescriptionText = fullDescriptionText.plus(descriptionText)
                                    }

                                    println(fullDescriptionText)

                                }

                                val descriptionLinks = findLinks(fullDescriptionText)

                                if (descriptionLinks.isNotEmpty())
                                    historySlice.descriptionLinks = descriptionLinks.joinToString(separator = ";")

                                client.post(
                                    "https://www.youtube.com/youtubei/v1/browse?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8&prettyPrint=false"
                                ) {

                                    val gson = Gson().toJson(
                                        BrowseRequest(
                                            context = Context(
                                                client = Client(
                                                    originalUrl = ytVideo.videoUrl
                                                )
                                            ),
                                            continuation = continuationCommandToken
                                        )
                                    )

                                    contentType(ContentType.Application.Json)
                                    setBody(gson)
                                }.let { response ->

                                    val jsonResponse = response.body<String>()
                                    val jsonParsedElement = JsonParser.parseString(jsonResponse)

                                    val firstComment = jsonParsedElement.asJsonObject
                                        .getAsJsonArray("onResponseReceivedEndpoints")
                                        .get(1).asJsonObject
                                        .getAsJsonObject("reloadContinuationItemsCommand")
                                        .getAsJsonArray("continuationItems")
                                        .get(0).asJsonObject
                                        .getAsJsonObject("commentThreadRenderer")

                                    val renderingPriority = firstComment.get("renderingPriority").asString

                                    val commentTextArray = firstComment
                                        .getAsJsonObject("comment")
                                        .getAsJsonObject("commentRenderer")
                                        .getAsJsonObject("contentText")
                                        .getAsJsonArray("runs")

                                    var fullPinnedCommentText = ""

                                    if (renderingPriority == "RENDERING_PRIORITY_PINNED_COMMENT") {

                                        commentTextArray?.forEach { commentElement ->

                                            val commentText = commentElement.asJsonObject
                                                .get("text").asString

                                            fullPinnedCommentText = fullPinnedCommentText.plus(commentText)
                                        }

                                        val commentLinks = findLinks(fullPinnedCommentText)

                                        if (commentLinks.isNotEmpty())
                                            historySlice.pinnedCommentLinks = commentLinks.joinToString(separator = ";")
                                    }
                                }

                            }.onFailure {
                                println(it.message)
                            }

                        } else {
                            println("JSON data not found in the script content.")
                        }
                    }
                }
            }

            client.get(ytVideo.channelUrl) {
                header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:85.0) Gecko/20100101 Firefox/85.0")
                header("Cookie", "CONSENT:YES+1;")
                header("Accept-Language", "en-US;q=0.5")
            }.let { response ->

                val htmlString = response.body<String>()
                val document: Document = Jsoup.parse(htmlString)

                val scriptElements = document.getElementsByTag("script")
                    .map { it.html() }

                for (scriptElement in scriptElements) {

                    if (scriptElement.contains("ytInitialData")) {

                        val start = scriptElement.indexOf('{')
                        val end = scriptElement.lastIndexOf('}')

                        if (start != -1 && end != -1 && start < end) {

                            runCatching {

                                val jsonString = scriptElement.substring(start, end + 1)
                                val jsonElement = JsonParser.parseString(jsonString)

                                val channelSubscribersCount = jsonElement.asJsonObject
                                    .getAsJsonObject("header")
                                    .getAsJsonObject("c4TabbedHeaderRenderer")
                                    .getAsJsonObject("subscriberCountText")
                                    .get("simpleText").asString

                                val regex = Regex("""(\d+(\.\d+)?)[KM]?\s*subscribers""")
                                val matchResult = regex.find(channelSubscribersCount)

                                if (matchResult != null) {
                                    val numberString = matchResult.groupValues[1]
                                    val multiplier = when {
                                        "K" in channelSubscribersCount -> 1000
                                        "M" in channelSubscribersCount -> 1000000
                                        else -> 1
                                    }

                                    val subscribersCount = (numberString.toDouble() * multiplier).toInt()
                                    println("$channelSubscribersCount: $subscribersCount subscribers")

                                    historySlice.subscriberCount = subscribersCount

                                } else {
                                    println("No match found in $channelSubscribersCount")
                                }

                            }.onFailure {
                                println(it.message)
                            }
                        }
                    }
                }
            }

            client.close()

            ytVideoService.saveHistorySlice(
               historySlice
            )
        }
    }

    fun startToCollectHistoryForYTVideos() {

        appCoroutineScope.launch {

            val ytVideos = repository.getAllYTVideos()

            ytVideos.forEach { ytVideo ->
                if (ytVideo.isTrending) {

                    delay(1.seconds)

                    scheduleHistoryCollection(ytVideo)
                }
            }

            delay(1.minutes)

            while(true) {

                repository.getAllYTVideos().forEach { ytVideo ->
                    if (!ytVideo.isTrending) {
                        collectHistoryJobs[ytVideo.videoId]?.cancel()
                        collectHistoryJobs.remove(ytVideo.videoId)
                    }
                }

                delay(30.minutes)
            }
        }
    }

    fun scheduleHistoryCollection(ytVideo: YTVideo) {

        val job = appCoroutineScope.launch {

            while(true) {

                collectHistory(ytVideo)

                delay(30.minutes)
            }
        }

        collectHistoryJobs[ytVideo.videoId] = job
    }
}

val ytVideoHistoryCollectWorker = YTVideoHistoryCollectWorker.getInstance(ytVideoDao)