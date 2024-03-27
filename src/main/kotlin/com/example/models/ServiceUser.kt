package com.example.models

import org.jetbrains.exposed.sql.Table

data class ServiceUser(
    val id: Int? = 0,
    var login: String = "",
    var password: String = ""
)

object ServiceUsers : Table(name = "service_users") {
    val id = integer("id").autoIncrement()
    val login = varchar("login", 128).uniqueIndex()
    val password = varchar("password", 128)
    override val primaryKey = PrimaryKey(id)
}