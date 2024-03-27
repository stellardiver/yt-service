package com.example.models.browse

data class Context(
    var client: Client = Client(),
    var request: Request = Request(),
    var user: User = User()
)
