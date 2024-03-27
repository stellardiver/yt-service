package com.example.services

import com.example.dao.UserDao
import com.example.dao.userDAO
import com.example.models.ServiceUser
import com.example.models.YTUser
import com.example.utils.Hashes

class UserService(private val userRepository: UserDao) {

    suspend fun createYTUser(ytUser: YTUser): YTUser? {

        val newYTUser = YTUser(
            email = ytUser.email,
            password = ytUser.password,
            recoveryEmail = ytUser.recoveryEmail,
            sidCookie = ytUser.sidCookie,
            hsidCookie = ytUser.hsidCookie,
            ssidCookie = ytUser.ssidCookie,
            apisidCookie = ytUser.apisidCookie,
            sapisidCookie = ytUser.sapisidCookie,
            secure1psidCookie = ytUser.secure1psidCookie,
            secure3psidCookie = ytUser.secure3psidCookie,
            sessionToken = ytUser.sessionToken
        )
        return userRepository.createYTUser(newYTUser)
    }

    suspend fun findYTUserByEmail(email: String): YTUser? {
        return userRepository.findYTUserByEmail(email)
    }

    suspend fun createServiceUser(serviceUser: ServiceUser): ServiceUser? {

        val hashedPassword = Hashes.hashPassword(serviceUser.password)

        val newServiceUser = ServiceUser(
            login = serviceUser.login,
            password = hashedPassword
        )
        return userRepository.createServiceUser(newServiceUser)
    }

    suspend fun findServiceUserByLogin(login: String): ServiceUser? {
        return userRepository.findServiceUserByLogin(login)
    }

    suspend fun loginUser(login: String, password: String): Boolean? {

        val user = userRepository.findServiceUserByLogin(login)

        return if (user != null) {
            Hashes.checkPassword(password, user)
        } else null
    }
}

val userService = UserService(userDAO)