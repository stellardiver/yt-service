package com.example.dao

import com.example.models.GmailAccountProduct

interface MarketDao {

    suspend fun createGmailAccountProduct(gmailAccountProduct: GmailAccountProduct): GmailAccountProduct?

    suspend fun findGmailAccountProductByProductId(productId: Int): GmailAccountProduct?

    suspend fun getAllGmailAccountProducts(): List<GmailAccountProduct>

    suspend fun updateGmailAccountProduct(gmailAccountProduct: GmailAccountProduct): GmailAccountProduct?
}