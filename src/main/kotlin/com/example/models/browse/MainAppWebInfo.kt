package com.example.models.browse

data class MainAppWebInfo(
    var graftUrl: String = "",
    var pwaInstallabilityStatus: String = "PWA_INSTALLABILITY_STATUS_UNKNOWN",
    var webDisplayMode: String = "WEB_DISPLAY_MODE_BROWSER",
    var isWebNativeShareAvailable: Boolean = false
)
