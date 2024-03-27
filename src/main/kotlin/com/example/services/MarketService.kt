package com.example.services

import com.example.dao.MarketDao
import com.example.dao.marketDao
import com.example.models.GmailAccountProduct

class MarketService(private val marketDao: MarketDao) {

    suspend fun findGmailAccountProductByProductId(productId: Int): GmailAccountProduct? {
        return marketDao.findGmailAccountProductByProductId(productId)
    }

    suspend fun getAllGmailAccountProducts(): List<GmailAccountProduct> {
        return marketDao.getAllGmailAccountProducts()
    }

    suspend fun saveGmailAccountProduct(
        gmailAccountProduct: GmailAccountProduct
    ): GmailAccountProduct? {

        marketDao.findGmailAccountProductByProductId(gmailAccountProduct.productId)?.let { accountProduct ->

            gmailAccountProduct.costHistory = if (accountProduct.costHistory.isBlank()) {
                gmailAccountProduct.cost.replace(",",".")
            } else accountProduct.costHistory.plus(":${gmailAccountProduct.cost.replace(",",".")}")

            gmailAccountProduct.quantityHistory = if (accountProduct.quantityHistory.isBlank()) {
                gmailAccountProduct.quantity.toString()
            } else accountProduct.quantityHistory.plus(":${gmailAccountProduct.quantity}")

            gmailAccountProduct.datestampHistory = if (accountProduct.datestampHistory.isBlank()) {
                gmailAccountProduct.datestamp.toString()
            } else accountProduct.datestampHistory.plus(":${gmailAccountProduct.datestamp}")

            return marketDao.updateGmailAccountProduct(gmailAccountProduct)

        } ?: run {

            gmailAccountProduct.costHistory = gmailAccountProduct.cost.replace(",",".")
            gmailAccountProduct.quantityHistory = gmailAccountProduct.quantity.toString()
            gmailAccountProduct.datestampHistory = gmailAccountProduct.datestamp.toString()

            return marketDao.createGmailAccountProduct(gmailAccountProduct)
        }

    }
}

val marketService = MarketService(marketDao)