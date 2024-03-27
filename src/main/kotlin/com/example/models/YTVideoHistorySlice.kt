package com.example.models

import com.google.gson.annotations.SerializedName
import org.jetbrains.exposed.sql.Table

data class YTVideoHistorySlice(
    val id: Int = 0,

    @SerializedName("video_id")
    var videoId: String = "",

    @SerializedName("view_count")
    var viewCount: Int = 0,

    @SerializedName("like_count")
    var likeCount: Int = 0,

    @SerializedName("comment_count")
    var commentCount: Int = 0,

    @SerializedName("subscriber_count")
    var subscriberCount: Int = 0,

    @SerializedName("history_time_stamp")
    var historyTimeStamp: Long = 0L,

    @SerializedName("description_links")
    var descriptionLinks: String = "",

    @SerializedName("pinned_comment_links")
    var pinnedCommentLinks: String = ""
)

object YTVideoHistorySlices : Table(name = "yt_trending_video_history_slices") {
    val id = integer("id").autoIncrement()
    val videoId = varchar("video_id", 128)
    val viewCount = integer("view_count")
    val likeCount = integer("like_count")
    val commentCount = integer("comment_count")
    val subscriberCount = integer("subscriber_count")
    val historyTimeStamp = long("history_time_stamp")
    val descriptionLinks = text("description_links")
    val pinnedCommentLinks = text("pinned_comment_links")
}