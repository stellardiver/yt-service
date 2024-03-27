package com.example.models

import com.google.gson.annotations.SerializedName

data class GrstResponse(
    @SerializedName("responseContext")
    val responseContext: ResponseContext,

    @SerializedName("sessionToken")
    val sessionToken: String
)
