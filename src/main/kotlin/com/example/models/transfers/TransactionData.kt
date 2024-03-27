package com.example.models.transfers

import com.google.gson.annotations.SerializedName

data class TransactionData(
    @SerializedName("transactionList")
    val transactionList: List<TokenTransfer>?
)
