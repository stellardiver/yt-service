package com.example.dao

import com.example.dao.DatabaseFactory.dbQuery
import com.example.models.*
import org.jetbrains.exposed.sql.*

class YTVideoDaoImpl: YTVideoDao {

    private fun resultRowToYTVideo(row: ResultRow) = YTVideo(
        videoId = row[YTVideos.videoId],
        title = row[YTVideos.title],
        description = row[YTVideos.description],
        channelName = row[YTVideos.channelName],
        channelUrl = row[YTVideos.channelUrl],
        publishedTime = row[YTVideos.publishedTime],
        tabTitle = row[YTVideos.tabTitle],
        viewCount = row[YTVideos.viewCount],
        viewCountAtStart = row[YTVideos.viewCountAtStart],
        viewCountLast = row[YTVideos.viewCountLast],
        length = row[YTVideos.length],
        thumbnailUrl = row[YTVideos.thumbnailUrl],
        videoUrl = row[YTVideos.videoUrl],
        firstAppeared = row[YTVideos.firstAppeared],
        lastUpdated = row[YTVideos.lastUpdated],
        proxyGeo = row[YTVideos.proxyGeo]
    )

    private fun resultRowToYTVideoHistorySlice(row: ResultRow) = YTVideoHistorySlice(
        videoId = row[YTVideoHistorySlices.videoId],
        viewCount = row[YTVideoHistorySlices.viewCount],
        likeCount = row[YTVideoHistorySlices.likeCount],
        commentCount = row[YTVideoHistorySlices.commentCount],
        subscriberCount = row[YTVideoHistorySlices.subscriberCount],
        historyTimeStamp = row[YTVideoHistorySlices.historyTimeStamp],
        descriptionLinks = row[YTVideoHistorySlices.descriptionLinks],
        pinnedCommentLinks = row[YTVideoHistorySlices.pinnedCommentLinks]
    )

    override suspend fun getAllYTVideos(): List<YTVideo> = dbQuery {
        YTVideos.selectAll().map(::resultRowToYTVideo)
    }

    override suspend fun getYTVideosByGeo(geo: String): List<YTVideo> = dbQuery {

        if (geo == "ALLGEO") {
            YTVideos
                .selectAll()
                .orderBy(YTVideos.firstAppeared, SortOrder.DESC)
                .map(::resultRowToYTVideo)
        } else {
            YTVideos
                .select { YTVideos.proxyGeo.eq(geo) }
                .orderBy(YTVideos.firstAppeared, SortOrder.DESC)
                //.limit(100)
                .map(::resultRowToYTVideo)
        }
    }

    override suspend fun getYTVideosByDateAndGeo(
        dateFrom: Long,
        dateTill: Long,
        geo: String
    ): List<YTVideo> {
        return dbQuery {
            YTVideos
                .select {
                    YTVideos
                        .lastUpdated.greaterEq(dateFrom) and YTVideos.lastUpdated.lessEq(dateTill) and YTVideos.proxyGeo.eq(geo)
                }
                .map(::resultRowToYTVideo)
        }
    }

    override suspend fun createHistorySlice(ytVideoHistorySlice: YTVideoHistorySlice): YTVideoHistorySlice? = dbQuery {

        val insertStatement = YTVideoHistorySlices.insert {
            it[videoId] = ytVideoHistorySlice.videoId
            it[viewCount] = ytVideoHistorySlice.viewCount
            it[likeCount] = ytVideoHistorySlice.likeCount
            it[commentCount] = ytVideoHistorySlice.commentCount
            it[subscriberCount] = ytVideoHistorySlice.subscriberCount
            it[historyTimeStamp] = ytVideoHistorySlice.historyTimeStamp
            it[descriptionLinks] = ytVideoHistorySlice.descriptionLinks
            it[pinnedCommentLinks] = ytVideoHistorySlice.pinnedCommentLinks
        }

        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToYTVideoHistorySlice)
    }

    override suspend fun getHistorySlicesByVideoId(videoId: String): List<YTVideoHistorySlice> {
        return dbQuery {
            YTVideoHistorySlices
                .select { YTVideoHistorySlices.videoId.eq(videoId) }
                .map(::resultRowToYTVideoHistorySlice)
        }
    }

    override suspend fun getHistorySlicesByViewsAndSubscribers(
        subscribersMin: Int,
        subscribersMax: Int,
        viewsMin: Int,
        viewsMax: Int
    ): List<YTVideoHistorySlice> = dbQuery {
        YTVideoHistorySlices
            .select {
                YTVideoHistorySlices.subscriberCount.greaterEq(subscribersMin) and YTVideoHistorySlices.subscriberCount.lessEq(subscribersMax) and
                        YTVideoHistorySlices.viewCount.greaterEq(viewsMin) and YTVideoHistorySlices.viewCount.lessEq(viewsMax)
            }
            .map(::resultRowToYTVideoHistorySlice)
    }

    override suspend fun createYTVideo(ytVideo: YTVideo): YTVideo? = dbQuery {

            val insertStatement = YTVideos.insert {
                it[videoId] = ytVideo.videoId
                it[title] = ytVideo.title
                it[description] = ytVideo.description
                it[channelName] = ytVideo.channelName
                it[channelUrl] = ytVideo.channelUrl
                it[publishedTime] = ytVideo.publishedTime
                it[tabTitle] = ytVideo.tabTitle
                it[viewCount] = ytVideo.viewCount
                it[viewCountAtStart] = ytVideo.viewCount
                it[viewCountLast] = ytVideo.viewCount
                it[length] = ytVideo.length
                it[thumbnailUrl] = ytVideo.thumbnailUrl
                it[videoUrl] = ytVideo.videoUrl
                it[firstAppeared] = ytVideo.lastUpdated
                it[lastUpdated] = ytVideo.lastUpdated
                it[proxyGeo] = ytVideo.proxyGeo
            }

            insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToYTVideo)
    }

    override suspend fun findYTVideoByVideoId(videoId: String): YTVideo? = dbQuery {
        YTVideos
            .select { YTVideos.videoId eq videoId }
            .map(::resultRowToYTVideo).singleOrNull()
    }

    override suspend fun updateYTVideo(ytVideo: YTVideo): YTVideo? = dbQuery {
        YTVideos.update({ YTVideos.videoId eq ytVideo.videoId }) {
            it[title] = ytVideo.title
            it[description] = ytVideo.description
            it[channelName] = ytVideo.channelName
            it[channelUrl] = ytVideo.channelUrl
            it[publishedTime] = ytVideo.publishedTime
            it[tabTitle] = ytVideo.tabTitle
            it[viewCount] = ytVideo.viewCount
            it[viewCountLast] = ytVideo.viewCountLast
            it[length] = ytVideo.length
            it[thumbnailUrl] = ytVideo.thumbnailUrl
            it[videoUrl] = ytVideo.videoUrl
            it[lastUpdated] = ytVideo.lastUpdated
            it[proxyGeo] = ytVideo.proxyGeo
        }
        findYTVideoByVideoId(ytVideo.videoId)

    }
}

val ytVideoDao = YTVideoDaoImpl()