package com.example.dao

import com.example.models.YTUsers
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: ApplicationConfig) {

        val driverClassName = config.property("storage.driverClassName").getString()
        val jdbcURL = config.property("storage.jdbcURL").getString()
        val username = config.property("storage.user").getString()
        val password = config.property("storage.password").getString()

        println(config.propertyOrNull("storage.jdbcURL")?.getString())

        val connectionPool = createHikariDataSource(
            url = "$jdbcURL?user=$username&password=$password",
            driver = driverClassName
        )

        val flyway = Flyway
            .configure()
            //.baselineOnMigrate(true)
            .dataSource(connectionPool)
            .load()

        //flyway.repair()
        flyway.migrate()

        val database = Database.connect(connectionPool)
        transaction(database) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(YTUsers)
        }
    }

    private fun createHikariDataSource(
        url: String,
        driver: String
    ) = HikariDataSource(HikariConfig().apply {
        driverClassName = driver
        jdbcUrl = url
        //maximumPoolSize = maxPoolSize
        //isAutoCommit = autoCommit
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    })

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

