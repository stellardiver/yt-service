package com.example.ui

import com.example.models.YTVideo
import com.example.models.YTVideoHistorySlice
import com.example.models.isTrending
import com.example.utils.FormattedNumberType
import com.example.utils.GEOS_LIST
import com.example.utils.formatNumber
import com.example.utils.parseLinks
import kotlinx.html.*
import kotlinx.html.dom.append
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

fun org.w3c.dom.Element.appendYTVideos(
    videos: List<YTVideo>,
    historySlices: LinkedHashMap<String, ArrayList<YTVideoHistorySlice>>
) {

    this.append {

        for (ytVideo in videos) {

            val videoHistorySlices = historySlices[ytVideo.videoId]!!

            val firstAppearedHumanReadable = if (ytVideo.firstAppeared == 0L) {
                "Нет данных"
            } else {
                SimpleDateFormat(
                    "dd MMMM yyyy, HH:mm:ss",
                    Locale.forLanguageTag("ru")
                ).format(ytVideo.firstAppeared)
            }

            val lastUpdatedHumanReadable = SimpleDateFormat(
                "dd MMMM yyyy, HH:mm:ss",
                Locale.forLanguageTag("ru")
            ).format(ytVideo.lastUpdated)

            val trendingLocationResult = if ((System.currentTimeMillis() - ytVideo.lastUpdated) > 3600000) {
                " ❌ Видео пропало из трендов"
            } else {
                "✅ Видео в трендах"
            }

            val proxyGeo = GEOS_LIST[ytVideo.proxyGeo] ?: "Неизвестно"

            div(classes = "card mt-3") {
                div(classes = "card-body mb-2") {

                    div(classes = "embed-responsive embed-responsive-9by16 container text-center") {

                        iframe(classes = "embed-responsive-item") {
                            attributes["allowfullscreen"] = ""
                            attributes["frameborder"] = "0"
                            attributes["height"] = "515"
                            attributes["src"] = ytVideo.videoUrl.replace("shorts", "embed")
                        }
                    }

                    h5(classes = "card-title mt-2") {
                        +ytVideo.title
                    }
                    p(classes = "card-text") {
                        +ytVideo.description
                    }
                    p(classes = "card-text") {
                        b {+ "ГЕО: " }
                        +proxyGeo
                    }
                    p(classes = "card-text") {
                        b {+ "Раздел в Trending: " }
                        +ytVideo.tabTitle
                    }
                    p(classes = "card-text") {
                        b {+ "Имя канала: " }
                        +ytVideo.channelName
                    }
                    p(classes = "card-text") {
                        b { +"Ссылка на канал: " }
                        a(href = ytVideo.channelUrl, target = "_blank") {
                            +ytVideo.channelUrl
                        }
                    }
                    if (ytVideo.isTrending) {
                        p(classes = "card-text") {
                            b { + "Опубликован: " }
                            +ytVideo.publishedTime
                        }
                    }
                    p(classes = "card-text") {
                        b { +"Просмотры на старте: " }
                        +ytVideo.viewCountAtStart
                    }
                    p(classes = "card-text") {
                        b { +"Просмотры при последней проверке: " }
                        +ytVideo.viewCountLast
                    }
                    p(classes = "card-text") {
                        b { +"Лайки на старте: " }
                        +formatNumber(ytVideo.likeCountAtStart, FormattedNumberType.LIKE)
                    }
                    p(classes = "card-text") {
                        b { +"Лайки при последней проверке: " }
                        +formatNumber(ytVideo.likeCountLast, FormattedNumberType.LIKE)
                    }
                    p(classes = "card-text") {
                        b { +"Первое появление: " }
                        span { +" $firstAppearedHumanReadable" }
                    }
                    p(classes = "card-text") {
                        b { +"Обновлено в последний раз: " }
                        span { +" $lastUpdatedHumanReadable" }
                    }
                    p(classes = "card-text") {
                        b { +"Длина: " }
                        +ytVideo.length
                    }
                    p(classes = "card-text") {
                        b { +trendingLocationResult }
                    }
                    p(classes = "card-text") {
                        b { +"Ссылка на видео: " }
                        a(href = ytVideo.videoUrl, target = "_blank") {
                            +ytVideo.videoUrl
                        }
                    }
                    p(classes = "card-text") {
                        b { +"Статистика по времени: " }
                    }
                    div(classes = "table-responsive") {

                        table(classes = "table table-striped table-bordered") {

                            thead {

                                tr {

                                    th { +"Тип данных" }

                                    for (historySlice in videoHistorySlices) {

                                        val humanReadableDate = SimpleDateFormat(
                                            "dd MMMM yyyy, HH:mm:ss",
                                            Locale.forLanguageTag("ru")
                                        ).format(historySlice.historyTimeStamp)

                                        th { +humanReadableDate }
                                    }
                                }
                            }

                            tbody {

                                tr {

                                    th { +"Просмотры" }

                                    for (historySlice in videoHistorySlices) {

                                        td { +formatNumber(historySlice.viewCount, FormattedNumberType.VIEW) }
                                    }
                                }

                                if (videoHistorySlices.first().likeCount > 0) {

                                    tr {

                                        th { +"Лайки" }

                                        for (historySlice in videoHistorySlices) {

                                            td { +formatNumber(historySlice.likeCount, FormattedNumberType.LIKE) }
                                        }
                                    }
                                }

                                tr {

                                    th { +"Комменты" }

                                    for (historySlice in videoHistorySlices) {

                                        td { +formatNumber(historySlice.commentCount, FormattedNumberType.COMMENT) }
                                    }
                                }

                                tr {

                                    th { +"Подписчики" }

                                    for (historySlice in videoHistorySlices) {

                                        td { +formatNumber(historySlice.subscriberCount, FormattedNumberType.SUBSCRIBER) }
                                    }
                                }

                                if (videoHistorySlices.first().likeCount > 0) {

                                    tr {

                                        th { +"Соотношение лайки/просмотры в %" }

                                        for (historySlice in videoHistorySlices) {

                                            val percentage = "${(historySlice.likeCount * 100 / historySlice.viewCount)} %"

                                            td { +percentage }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (
                        videoHistorySlices[videoHistorySlices.lastIndex].pinnedCommentLinks.isNotEmpty()
                    ) {

                        val pinnedCommentLinks = parseLinks(
                            input = videoHistorySlices[videoHistorySlices.lastIndex].pinnedCommentLinks
                        )

                        p(classes = "card-text") {
                            b { +"Ссылки в закрепленном комментарии: " }
                        }

                        div(classes = "table-responsive") {

                            table(classes = "table table-striped table-bordered") {

                                for (link in pinnedCommentLinks) {

                                    tr {
                                        td {
                                            +link
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (
                        videoHistorySlices[videoHistorySlices.lastIndex].descriptionLinks.isNotEmpty()
                    ) {

                        val descriptionLinks = parseLinks(
                            input = videoHistorySlices[videoHistorySlices.lastIndex].descriptionLinks
                        )

                        p(classes = "card-text") {
                            b { +"Ссылки в описании: " }
                        }

                        div(classes = "table-responsive") {

                            table(classes = "table table-striped table-bordered") {

                                for (link in descriptionLinks) {

                                    tr {
                                        td {
                                            +link
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}