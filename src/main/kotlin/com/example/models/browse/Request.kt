package com.example.models.browse

data class Request(
    var useSsl: Boolean = true,
    var internalExperimentFlags: List<String> = listOf(),
    var consistencyTokenJars: List<String> = listOf()
)
