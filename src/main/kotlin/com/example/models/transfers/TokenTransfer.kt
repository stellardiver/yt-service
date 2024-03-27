package com.example.models.transfers

import com.google.gson.annotations.SerializedName

data class TokenTransfer(

    @SerializedName("txId")
    val txHash: String,

    @SerializedName("from")
    val fromAddress: String,

    @SerializedName("to")
    val toAddress: String,

    @SerializedName("amount")
    val quant: String,

    @SerializedName("symbol")
    val symbol: String,

    @SerializedName("transactionTime")
    val transactionTime: String
)
