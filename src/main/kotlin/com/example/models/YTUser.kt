package com.example.models

import org.jetbrains.exposed.sql.Table

data class YTUser(
    val id: Int = 0,
    var email: String = "",
    var password: String = "",
    var recoveryEmail: String = "",
    var sidCookie: String = "",
    var hsidCookie: String = "",
    var ssidCookie: String = "",
    var apisidCookie: String = "",
    var sapisidCookie: String = "",
    var secure1psidCookie: String = "",
    var secure3psidCookie: String = "",
    var sessionToken: String = ""
)

object YTUsers : Table(name = "yt_users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 128).uniqueIndex()
    val password = varchar("password", 128)
    val recoveryEmail = varchar("recovery_email", 128)
    val sidCookie = varchar("sid_cookie", 128)
    val hsidCookie = varchar("hsid_cookie", 128)
    val ssidCookie = varchar("ssid_cookie", 128)
    val apisidCookie = varchar("apisid_cookie", 128)
    val sapisidCookie = varchar("sapisid_cookie", 128)
    val secure1psidCookie = varchar("secure1psid_cookie", 256)
    val secure3psidCookie = varchar("secure3psid_cookie", 256)
    val sessionToken = varchar("session_token", 256)
    override val primaryKey = PrimaryKey(id)
}