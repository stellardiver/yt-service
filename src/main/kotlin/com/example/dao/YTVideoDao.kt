package com.example.dao

import com.example.models.YTVideo
import com.example.models.YTVideoHistorySlice

interface YTVideoDao {

    suspend fun createYTVideo(ytVideo: YTVideo): YTVideo?

    suspend fun findYTVideoByVideoId(videoId: String): YTVideo?

    suspend fun getAllYTVideos(): List<YTVideo>

    suspend fun getYTVideosByGeo(geo: String): List<YTVideo>

    suspend fun getYTVideosByDateAndGeo(dateFrom: Long, dateTill: Long, geo: String): List<YTVideo>

    suspend fun updateYTVideo(ytVideo: YTVideo): YTVideo?

    suspend fun createHistorySlice(ytVideoHistorySlice: YTVideoHistorySlice): YTVideoHistorySlice?

    suspend fun getHistorySlicesByVideoId(videoId: String): List<YTVideoHistorySlice>

    suspend fun getHistorySlicesByViewsAndSubscribers(
        subscribersMin: Int,
        subscribersMax: Int,
        viewsMin: Int,
        viewsMax: Int
    ): List<YTVideoHistorySlice>
}