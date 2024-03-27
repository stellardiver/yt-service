package com.example.models

import com.google.gson.annotations.SerializedName

data class ResponseContext(
    @SerializedName("serviceTrackingParams")
    val serviceTrackingParams: ArrayList<ServiceTrackingParams>,

    @SerializedName("webResponseContextExtensionData")
    val webResponseContextExtensionData: WebResponseContextExtensionData
)
