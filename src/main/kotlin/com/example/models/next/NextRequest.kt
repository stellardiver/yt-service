package com.example.models.next

import com.example.models.browse.Context

data class NextRequest(
    var context: Context = Context(),
    var videoId: String = "",
    var params: String = "",
    var racyCheckOk: Boolean = false,
    var contentCheckOk: Boolean = false,
    var autonavState: String = "STATE_NONE"
)