package com.example.models.browse

data class BrowseRequest(
    var context: Context = Context(),
    var continuation: String? = null,
    var browseId: String? = null,
    var params: String? = null
)
