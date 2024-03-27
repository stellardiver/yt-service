package com.example.routes

import com.example.models.YTVideo
import com.example.models.YTVideoHistorySlice
import com.example.services.YTVideoService
import com.example.ui.appendYTVideos
import com.example.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.dom.document
import kotlinx.html.dom.serialize
import org.w3c.dom.Element
import java.io.File
import kotlin.collections.ArrayList

fun Route.uiRoutes(ytVideoService: YTVideoService) {

    get("/web/css/normalize.css") {

        call.respondFile(
            File("src/main/resources/web/css/normalize.css")
        )
    }

    get("/web/js/script.js") {

        call.respondFile(
            File("src/main/resources/web/js/script.js")
        )
    }

    get("/.well-known/pki-validation/3A86DA37D974E79329E45B83386D76E6.txt") {

        call.respondFile(
            File("src/main/resources/.well-known/pki-validation/3A86DA37D974E79329E45B83386D76E6.txt")
        )
    }

    get("/web/js/form-trending-videos.js") {

        call.respondFile(
            File("src/main/resources/web/js/form-trending-videos.js")
        )
    }

    get("/favicon.ico") {

        call.respondFile(
            File("src/main/resources/web/favicon.ico")
        )
    }

    get("/login") {

        val document = document {}

        document.append {
            html {
                head {
                    meta(charset = "utf-8")
                    meta(
                        name = "viewport",
                        content = "width=device-width, initial-scale=1"
                    )
                    link(
                        rel = "stylesheet",
                        href = "/web/css/normalize.css",
                        type = "text/css"
                    )
                    link(
                        rel = "stylesheet",
                        href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css",
                        type = "text/css"
                    )
                    link(
                        rel = "shortcut icon",
                        href = "/favicon.ico",
                        type = "image/x-icon"
                    )
                    title("YT-Service Login")
                }
                body(classes = "h-100") {
                    style {
                        unsafe {
                            raw(
                                """
                            html,
                            body {
                                height: 100%;
                            }
                            """
                            )
                        }
                    }
                    div(classes = "d-flex flex-column justify-content-center h-100 max-width-sm") {

                        div(classes = "container mt-3") {
                            h1(classes = "text-center") { +"YT-Service" }
                        }
                        div(classes = "container mt-5") {
                            div(classes = "row") {
                                div(classes = "col") {
                                    form(classes = "form") {
                                        div(classes = "mb-3") {
                                            label(classes = "form-label") {
                                                +"Логин"
                                            }
                                            input(classes = "form-control input-login", type = InputType.text, name = "login")
                                        }
                                        div(classes = "mb-3") {
                                            label(classes = "form-label") {
                                                +"Пароль"
                                            }
                                            input(classes = "form-control input-password", type = InputType.password, name = "password")
                                        }
                                        button(classes = "btn btn-primary", type = ButtonType.submit) {
                                            +"Войти"
                                        }
                                    }
                                }
                            }
                        }
                    }
                    script {
                        src = "/web/js/script.js"
                        type = "text/javascript"
                    }
                }
            }
        }

        call.request.cookies["TOKEN"]?.let {
            call.respondRedirect("/yt_trending_videos")
            return@get
        }

        call.respondText(
            document.serialize(),
            ContentType.Text.Html.withCharset(Charsets.UTF_8)
        )
    }

    get("/yt_trending_videos") {

        val document = document {}
        val token = call.request.cookies["TOKEN"]

        if (token == null) {
            call.respondRedirect("/login")
            return@get
        }

        val page = call.request.queryParameters["page"]
        val geo = call.request.queryParameters["geo"]
        val onlyTrending = call.request.queryParameters["only_trending"]
        val onlyWithLinks = call.request.queryParameters["only_with_links"]
        val subscribersMin = call.request.queryParameters["subscribers_min"]
        val subscribersMax = call.request.queryParameters["subscribers_max"]
        val viewsMin = call.request.queryParameters["views_min"]
        val viewsMax = call.request.queryParameters["views_max"]
        val allYTVideos = arrayListOf<YTVideo>()
        var chunkedYTVideos = listOf<List<YTVideo>>()
        val historySlices = linkedMapOf<String, ArrayList<YTVideoHistorySlice>>()

        document.append {
            html {
                head {
                    meta(charset = "utf-8")
                    meta(
                        name = "viewport",
                        content = "width=device-width, initial-scale=1"
                    )
                    link(
                        rel = "stylesheet",
                        href = "/web/css/normalize.css",
                        type = "text/css"
                    )
                    link(
                        rel = "stylesheet",
                        href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css",
                        type = "text/css"
                    )
                    link(
                        rel = "stylesheet",
                        href = "https://code.jquery.com/ui/1.13.2/themes/base/jquery-ui.css",
                        type = "text/css"
                    )
                    link(
                        rel = "stylesheet",
                        href = "https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datepicker/1.10.0/css/bootstrap-datepicker3.min.css",
                        type = "text/css"
                    )
                    link(
                        rel = "shortcut icon",
                        href = "/favicon.ico",
                        type = "image/x-icon"
                    )
                    title("YT-Service Stats")
                }
                body(classes = "h-100") {
                    style {
                        unsafe {
                            raw(
                                """
                            html,
                            body {
                                height: 100%;
                            }
                            """
                            )
                        }
                    }

                    div(classes = "container mt-5") {
                        div(classes = "row") {
                            div(classes = "col") {
                                form(classes = "form") {
                                    div(classes = "mb-3") {
                                        label(classes = "form-label") {
                                            +"Выберите ГЕО"
                                        }
                                        select(classes = "form-select") {

                                            //attributes["multiple"] = ""
                                            attributes["name"] = "geo"
                                            attributes["id"] = "geo"

                                            for (geoItem in GEOS_LIST) {
                                                option {
                                                    attributes["value"] = geoItem.key
                                                    +geoItem.value
                                                }
                                            }
                                        }
                                    }

                                    div(classes = "row") {

                                        div(classes = "col") {

                                            p {
                                                attributes["id"] = "subscribers"
                                            }
                                            div {
                                                attributes["id"] = "subscribers-slider-range"
                                            }
                                        }
                                    }

                                    div(classes = "row mt-3") {

                                        div(classes = "col") {

                                            p {
                                                attributes["id"] = "views"
                                            }
                                            div {
                                                attributes["id"] = "views-slider-range"
                                            }
                                        }
                                    }

                                    div(classes = "row mt-3") {

                                        div(classes = "col") {

                                            input(classes = "form-check-input", type = InputType.checkBox) {
                                                attributes["id"] = "only-trending-checkbox"
                                                attributes["checked"] = ""
                                                + "   Показать только те видео, которые сейчас в трендах"
                                            }
                                        }
                                    }

                                    div(classes = "row mt-3") {

                                        div(classes = "col") {

                                            input(classes = "form-check-input", type = InputType.checkBox) {
                                                attributes["id"] = "only-with-links-checkbox"
                                                attributes["checked"] = ""
                                                + "   Показать только видео со ссылками в описании/комментарии"
                                            }
                                        }
                                    }

                                    button(classes = "btn btn-primary mt-3", type = ButtonType.submit) {
                                        attributes["id"] = "submit-button"
                                        +"Показать"
                                    }
                                }
                            }

                            div(classes = "row") {

                                div(classes = "col-md-7 mx-auto") {

                                    attributes["id"] = "yt-videos-container"

                                }
                            }
                        }
                    }
                    script {
                        src = "https://code.jquery.com/jquery-3.7.1.min.js"
                        type = "text/javascript"
                    }
                    script {
                        src = "https://code.jquery.com/ui/1.13.2/jquery-ui.js"
                        type = "text/javascript"
                    }
                    script {
                        src = "https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datepicker/1.10.0/js/bootstrap-datepicker.min.js"
                        type = "text/javascript"
                    }
                    script {
                        src = "/web/js/form-trending-videos.js"
                        type = "text/javascript"
                    }
                }
            }
        }

        if (
            geo != null
            && onlyTrending != null
            && onlyWithLinks != null
            && subscribersMin != null
            && subscribersMax != null
            && viewsMin != null
            && viewsMax != null
        ) {

            println("geo: $geo, subscribersMin: $subscribersMin, subscribersMax: $subscribersMax, viewsMin: $viewsMin, viewsMax: $viewsMax, onlyTrending: $onlyTrending")

            val data = ytVideoService.getYTVideosByFilters(
                geo = geo,
                onlyTrending = onlyTrending.toBoolean(),
                onlyWithLinks = onlyWithLinks.toBoolean(),
                subscribersMin = subscribersMin.toInt(),
                subscribersMax = subscribersMax.toInt(),
                viewsMin = viewsMin.toInt(),
                viewsMax = viewsMax.toInt()
            )

            data.first.forEach { ytVideo ->

                println("Found ytVideo: $ytVideo")

                allYTVideos.add(ytVideo)
            }

            data.second.forEach {
                historySlices[it.key] = ArrayList(it.value).apply { sortBy { slice -> slice.historyTimeStamp } }
            }

            chunkedYTVideos = allYTVideos.chunked(size = 20)

            if (page != null) {

                val index = page.toInt() - 1
                val paginationVideos = if (index <= chunkedYTVideos.lastIndex) chunkedYTVideos[index] else emptyList()

                val paginationDocument = document {}
                val videoContainer = paginationDocument
                    .createElement("div")
                    .apply {
                        setAttribute("class", "col-md-7 mx-auto")
                        setAttribute("id", "yt-videos-container")
                    }

                videoContainer.appendYTVideos(
                    videos = paginationVideos,
                    historySlices = historySlices
                )

                call.respondText(
                    videoContainer.serialize(),
                    ContentType.Text.Html.withCharset(Charsets.UTF_8)
                )

                return@get
            }
        }

        val ytVideosContainer = document.getElementById("yt-videos-container")

        ytVideosContainer.append {

            if (chunkedYTVideos.isNotEmpty()) {

                val ytVideos = chunkedYTVideos[0]

                ytVideosContainer.appendYTVideos(
                    videos = ytVideos,
                    historySlices = historySlices
                )

                if (
                    ytVideos.isEmpty()
                    && call.request.queryParameters.entries().isNotEmpty()
                ) {
                    div(classes = "card mt-3") {
                        div(classes = "card-body mb-2") {
                            h5(classes = "card-title mt-2") {
                                +"Ничего не нашлось"
                            }
                        }
                    }
                }

                if (ytVideos.isNotEmpty()) {

                    div(classes = "text-center") {
                        attributes["id"] = "show-more-button-container"
                        button(classes = "btn btn-primary mt-3 mb-3") {
                            attributes["id"] = "show-more-button"
                            +"Показать еще"
                        }
                    }

                }

            } else {

                div(classes = "card mt-3") {
                    div(classes = "card-body mb-2") {
                        h5(classes = "card-title mt-2") {
                            +"Ничего не нашлось"
                        }
                    }
                }
            }

        }

        call.respondText(
            document.serialize(),
            ContentType.Text.Html.withCharset(Charsets.UTF_8)
        )
    }
}