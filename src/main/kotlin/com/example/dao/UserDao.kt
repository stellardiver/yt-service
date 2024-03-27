package com.example.dao

import com.example.models.ServiceUser
import com.example.models.YTUser

interface UserDao {

    suspend fun createYTUser(ytUser: YTUser): YTUser?

    suspend fun findYTUserByEmail(email: String): YTUser?

    suspend fun getAllYTUsers(): List<YTUser>

    suspend fun updateYTUser(ytUser: YTUser): YTUser?

    suspend fun createServiceUser(serviceUser: ServiceUser): ServiceUser?

    suspend fun findServiceUserByLogin(login: String): ServiceUser?
}