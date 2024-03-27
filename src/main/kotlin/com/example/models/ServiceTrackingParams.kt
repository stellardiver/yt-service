package com.example.models

import com.google.gson.annotations.SerializedName

data class ServiceTrackingParams(
    @SerializedName("service")
    val service: String,

    @SerializedName("params")
    val params: ArrayList<Params>
)
