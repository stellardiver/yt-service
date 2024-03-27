package com.example.models

import com.example.models.transfers.TokenTransfer
import com.example.utils.NetworkType

data class Wallet(
    val id: Int = 0,

    var walletAddress: String = "",

    var walletNetworkType: String = NetworkType.TRON,

    var owner: String = "",

    var viewCounts: Array<Int> = arrayOf(),

    var paymentAmount: Int = 0,

    var paymentCurrency: String = "$",

    var paymentStatus: Boolean = false,

    var checkedPaymentStatus: Boolean = false,

    var tokenTransfers: ArrayList<TokenTransfer> = arrayListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Wallet

        if (id != other.id) return false
        if (walletAddress != other.walletAddress) return false
        if (owner != other.owner) return false
        if (!viewCounts.contentEquals(other.viewCounts)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + walletAddress.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + viewCounts.contentHashCode()
        return result
    }
}