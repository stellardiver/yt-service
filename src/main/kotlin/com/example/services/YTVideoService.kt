package com.example.services

import com.example.dao.YTVideoDao
import com.example.dao.ytVideoDao
import com.example.models.YTVideo
import com.example.models.YTVideoHistorySlice
import com.example.models.isTrending
import com.example.workers.ytVideoHistoryCollectWorker
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class YTVideoService(private val ytVideoDao: YTVideoDao) {

    suspend fun getAllYTVideos(): List<YTVideo> {
        return ytVideoDao.getAllYTVideos()
    }

    suspend fun getYTVideosByDateAndGeo(dateFrom: Long, dateTill: Long, geo: String): List<YTVideo> {
        return ytVideoDao.getYTVideosByDateAndGeo(dateFrom, dateTill, geo)
    }

    suspend fun saveYTVideo(ytVideo: YTVideo): YTVideo? {

        ytVideoDao.findYTVideoByVideoId(ytVideo.videoId)?.let { video ->

            video.title = ytVideo.title
            video.publishedTime = ytVideo.publishedTime
            video.description = ytVideo.description
            video.viewCount = ytVideo.viewCount
            video.viewCountLast = ytVideo.viewCount
            video.lastUpdated = ytVideo.lastUpdated
            video.proxyGeo = ytVideo.proxyGeo

            return ytVideoDao.updateYTVideo(video)

        } ?: run {

            delay(1.seconds)

            ytVideoHistoryCollectWorker.scheduleHistoryCollection(ytVideo)
            
            return ytVideoDao.createYTVideo(ytVideo)
        }
    }

    suspend fun getYTVideosByFilters(
        geo: String,
        onlyTrending: Boolean,
        onlyWithLinks: Boolean,
        subscribersMin: Int,
        subscribersMax: Int,
        viewsMin: Int,
        viewsMax: Int
    ): Pair<List<YTVideo>, Map<String, List<YTVideoHistorySlice>>> {

        val ytVideoHistorySlices = ytVideoDao.getHistorySlicesByViewsAndSubscribers(
            subscribersMin,
            subscribersMax,
            viewsMin,
            viewsMax
        ).groupBy { it.videoId }

        val ytVideosByGeoAndTrendingFlag = ytVideoDao.getYTVideosByGeo(geo)
            .filter { ytVideoFromDb ->
                if (onlyTrending) ytVideoFromDb.isTrending else true
            }
            .filter { ytVideoFromDb ->
                if (onlyWithLinks) {

                    ytVideoHistorySlices[ytVideoFromDb.videoId]?.let { historySlices ->
                        historySlices.last().pinnedCommentLinks.isNotEmpty() || historySlices.last().descriptionLinks.isNotEmpty()
                    } ?: true

                } else true
            }
            .filter { it.videoId in ytVideoHistorySlices.keys }

        ytVideosByGeoAndTrendingFlag.forEach { video ->

            ytVideoHistorySlices[video.videoId]?.let { historySlices ->
                video.likeCountAtStart = historySlices.first().likeCount
                video.likeCountLast = historySlices.last().likeCount
            }
        }

        return Pair(ytVideosByGeoAndTrendingFlag, ytVideoHistorySlices)
    }

    suspend fun findYTVideoByVideoId(videoId: String): YTVideo? {
        return ytVideoDao.findYTVideoByVideoId(videoId)
    }

    suspend fun saveHistorySlice(ytVideoHistorySlice: YTVideoHistorySlice): YTVideoHistorySlice? {
        return ytVideoDao.createHistorySlice(ytVideoHistorySlice)
    }

    suspend fun getHistorySlicesByVideoId(videoId: String): List<YTVideoHistorySlice> {
        return ytVideoDao.getHistorySlicesByVideoId(videoId)
    }
}

val ytVideoService = YTVideoService(ytVideoDao)