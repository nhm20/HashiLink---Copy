package com.example.hashilink.data.model

data class User(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var mobile: String = "",
    var role: String = "buyer",
    var profileImageUrl: String? = null
)