package com.example.models.transfers


data class TransfersResponse(
    val code: String,
    val msg: String,
    val data: List<TransactionData>
)