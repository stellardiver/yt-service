package com.example.dao

import com.example.dao.DatabaseFactory.dbQuery
import com.example.models.GmailAccountProduct
import com.example.models.GmailAccountProducts
import org.jetbrains.exposed.sql.*
import java.lang.Float.parseFloat
import java.text.NumberFormat
import java.util.*

class MarketDaoImpl: MarketDao {

    private fun resultRowToGmailAccountProduct(row: ResultRow) = GmailAccountProduct(
        productId = row[GmailAccountProducts.productId],
        title = row[GmailAccountProducts.title],
        link = row[GmailAccountProducts.link],
        cost = row[GmailAccountProducts.cost].toString(),
        quantity = row[GmailAccountProducts.quantity],
        datestamp = row[GmailAccountProducts.datestamp],
        costHistory = row[GmailAccountProducts.costHistory],
        quantityHistory = row[GmailAccountProducts.quantityHistory],
        datestampHistory = row[GmailAccountProducts.datestampHistory]
    )

    override suspend fun createGmailAccountProduct(
        gmailAccountProduct: GmailAccountProduct
    ): GmailAccountProduct? = dbQuery {

        val insertStatement = GmailAccountProducts.insert {
            it[productId] = gmailAccountProduct.productId
            it[title] = gmailAccountProduct.title
            it[link] = gmailAccountProduct.link
            it[cost] = parseFloat(gmailAccountProduct.cost.replace(",","."))
            it[quantity] = gmailAccountProduct.quantity
            it[datestamp] = gmailAccountProduct.datestamp
            it[costHistory] = gmailAccountProduct.costHistory
            it[quantityHistory] = gmailAccountProduct.quantityHistory
            it[datestampHistory] = gmailAccountProduct.datestampHistory
        }

        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToGmailAccountProduct)
    }

    override suspend fun getAllGmailAccountProducts(): List<GmailAccountProduct> = dbQuery {
        GmailAccountProducts.selectAll().map(::resultRowToGmailAccountProduct)
    }

    override suspend fun findGmailAccountProductByProductId(productId: Int): GmailAccountProduct? = dbQuery {
        GmailAccountProducts
            .select { GmailAccountProducts.productId eq productId }
            .map(::resultRowToGmailAccountProduct).singleOrNull()
    }

    override suspend fun updateGmailAccountProduct(
        gmailAccountProduct: GmailAccountProduct
    ): GmailAccountProduct? = dbQuery {
        GmailAccountProducts.update({ GmailAccountProducts.productId eq gmailAccountProduct.productId }) {
            it[title] = gmailAccountProduct.title
            it[link] = gmailAccountProduct.link
            it[cost] = parseFloat(gmailAccountProduct.cost.replace(",","."))
            it[quantity] = gmailAccountProduct.quantity
            it[datestamp] = gmailAccountProduct.datestamp
            it[costHistory] = gmailAccountProduct.costHistory
            it[quantityHistory] = gmailAccountProduct.quantityHistory
            it[datestampHistory] = gmailAccountProduct.datestampHistory
        }

        findGmailAccountProductByProductId(gmailAccountProduct.productId)
    }
}

val marketDao = MarketDaoImpl()