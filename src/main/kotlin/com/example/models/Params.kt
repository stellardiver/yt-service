package com.example.models

import com.google.gson.annotations.SerializedName

data class Params(
    @SerializedName("key")
    val key: String,

    @SerializedName("value")
    val value: String
)
