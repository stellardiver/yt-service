package com.example.models.search

import com.example.models.browse.Context

data class SearchRequest(
    var context: Context = Context(),
    var query: String = "",
    var webSearchboxStatsUrl: String = "",
)
