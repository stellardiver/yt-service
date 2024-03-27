package com.example.models

import com.google.gson.annotations.SerializedName
import org.jetbrains.exposed.sql.Table

data class GmailAccountProduct(
    @SerializedName("product_id")
    var productId: Int = 0,
    var title: String = "",
    var link: String = "",
    var cost: String = "",
    var quantity: Int = 0,
    var datestamp: Long = 0L,
    var costHistory: String = "",
    var quantityHistory: String = "",
    var datestampHistory: String = ""
)

object GmailAccountProducts : Table(name = "market_gmail_account_products") {
    val id = integer("id").autoIncrement()
    val productId = integer("product_id").uniqueIndex()
    val title = varchar("title", 1028)
    val link = varchar("link", 1028)
    val cost = float("cost")
    val quantity = integer("quantity")
    val datestamp = long("datestamp")
    val costHistory = varchar("cost_history", 2048)
    val quantityHistory = varchar("quantity_history", 2048)
    val datestampHistory = varchar("datestamp_history", 5096)
    override val primaryKey = PrimaryKey(id)
}