package com.devfalah.entities

data class User(
    val birthdate: String,
    val coverUrl: Boolean,
    val email: String,
    val firstName: String,
    val fullName: String,
    val gender: String,
    val guid: Int,
    val icon: Icon,
    val language: String,
    val lastName: String,
    val username: String,
)

data class Icon(
    val large: String? = "",
    val larger: String?= "",
    val small: String?= "",
    val smaller: String? = "",
    val topBar: String? = "",
)