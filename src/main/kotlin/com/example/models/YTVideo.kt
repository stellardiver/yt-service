package com.example.models

import com.google.gson.annotations.SerializedName
import org.jetbrains.exposed.sql.Table

data class YTVideo(
    val id: Int = 0,

    @SerializedName("video_id")
    var videoId: String = "",

    var title: String = "",

    var description: String = "",

    @SerializedName("channel_name")
    var channelName: String = "",

    @SerializedName("channel_url")
    var channelUrl: String = "",

    @SerializedName("published_time")
    var publishedTime: String = "",

    @SerializedName("tab_title")
    var tabTitle: String = "",

    @SerializedName("view_count")
    var viewCount: String = "",

    var viewCountAtStart : String = "",

    var viewCountLast: String = "",

    var likeCountAtStart : Int = 0,

    var likeCountLast: Int = 0,

    var length: String = "",

    @SerializedName("thumbnail_url")
    var thumbnailUrl: String = "",

    @SerializedName("video_url")
    var videoUrl: String = "",

    @SerializedName("first_appeared")
    var firstAppeared: Long = 0L,

    @SerializedName("last_updated")
    var lastUpdated: Long = 0L,

    @SerializedName("proxy_geo")
    var proxyGeo: String = ""
)

object YTVideos : Table(name = "yt_trending_videos") {
    val id = integer("id").autoIncrement()
    val videoId = varchar("video_id", 128).uniqueIndex()
    val title = varchar("title", 512)
    val description = varchar("description", 512)
    val channelName = varchar("channel_name", 128)
    val channelUrl = varchar("channel_url", 128)
    val publishedTime = varchar("published_time", 128)
    val tabTitle = varchar("tab_title", 128)
    val viewCount = varchar("view_count", 128)
    val viewCountAtStart = varchar("view_count_at_start", 128)
    val viewCountLast = varchar("view_count_last", 128)
    val length = varchar("video_length", 128)
    val thumbnailUrl = varchar("thumbnail_url", 512)
    val videoUrl = varchar("video_url", 128)
    val firstAppeared = long("first_appeared")
    val lastUpdated = long("last_updated")
    val proxyGeo = varchar("proxy_geo", 64)
    override val primaryKey = PrimaryKey(id)
}

val YTVideo.isTrending get() = (System.currentTimeMillis() - this.lastUpdated) < 3600000