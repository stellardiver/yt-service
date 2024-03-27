package com.example.dao

import com.example.dao.DatabaseFactory.dbQuery
import com.example.models.ServiceUser
import com.example.models.ServiceUsers
import com.example.models.YTUser
import com.example.models.YTUsers
import org.jetbrains.exposed.sql.*

class UserDaoImpl : UserDao {

    private fun resultRowToYTUser(row: ResultRow) = YTUser(
        id = row[YTUsers.id],
        email = row[YTUsers.email],
        password = row[YTUsers.password],
        recoveryEmail = row[YTUsers.recoveryEmail],
        sidCookie = row[YTUsers.sidCookie],
        hsidCookie = row[YTUsers.hsidCookie],
        ssidCookie = row[YTUsers.ssidCookie],
        apisidCookie = row[YTUsers.apisidCookie],
        sapisidCookie = row[YTUsers.sapisidCookie],
        secure1psidCookie = row[YTUsers.secure1psidCookie],
        secure3psidCookie = row[YTUsers.secure3psidCookie],
        sessionToken = row[YTUsers.sessionToken]
    )

    private fun resultRowToServiceUser(row: ResultRow) = ServiceUser(
        id = row[ServiceUsers.id],
        login = row[ServiceUsers.login],
        password = row[ServiceUsers.password]
    )

    override suspend fun createYTUser(ytUser: YTUser): YTUser? = dbQuery {

        val insertStatement = YTUsers.insert {
            it[email] = ytUser.email
            it[password] = ytUser.password
            it[recoveryEmail] = ytUser.recoveryEmail
            it[sidCookie] = ytUser.sidCookie
            it[hsidCookie] = ytUser.hsidCookie
            it[ssidCookie] = ytUser.ssidCookie
            it[apisidCookie] = ytUser.apisidCookie
            it[sapisidCookie] = ytUser.sapisidCookie
            it[secure1psidCookie] = ytUser.secure1psidCookie
            it[secure3psidCookie] = ytUser.secure3psidCookie
            it[sessionToken] = ytUser.sessionToken
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToYTUser)

    }

    override suspend fun findYTUserByEmail(email: String): YTUser? = dbQuery {

        YTUsers
            .select { YTUsers.email eq email }
            .map(::resultRowToYTUser).singleOrNull()
    }

    override suspend fun getAllYTUsers(): List<YTUser> = dbQuery {
        YTUsers.selectAll().map(::resultRowToYTUser)
    }

    override suspend fun updateYTUser(ytUser: YTUser): YTUser? {
        return dbQuery {
            YTUsers.update({ YTUsers.id eq ytUser.id }) {
                it[email] = ytUser.email
                it[password] = ytUser.password
                it[recoveryEmail] = ytUser.recoveryEmail
                it[sidCookie] = ytUser.sidCookie
                it[hsidCookie] = ytUser.hsidCookie
                it[ssidCookie] = ytUser.ssidCookie
                it[apisidCookie] = ytUser.apisidCookie
                it[sapisidCookie] = ytUser.sapisidCookie
                it[secure1psidCookie] = ytUser.secure1psidCookie
                it[secure3psidCookie] = ytUser.secure3psidCookie
                it[sessionToken] = ytUser.sessionToken
            }
            findYTUserByEmail(ytUser.email)
        }
    }

    override suspend fun createServiceUser(serviceUser: ServiceUser): ServiceUser? = dbQuery {

        val insertStatement = ServiceUsers.insert {
            it[login] = serviceUser.login
            it[password] = serviceUser.password
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToServiceUser)

    }

    override suspend fun findServiceUserByLogin(login: String): ServiceUser? = dbQuery {

        ServiceUsers
            .select { ServiceUsers.login eq login }
            .map(::resultRowToServiceUser).singleOrNull()
    }
}

val userDAO = UserDaoImpl()