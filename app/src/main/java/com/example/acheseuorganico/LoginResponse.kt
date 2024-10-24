package com.example.acheseuorganico

data class LoginResponse(
    val userId: Int,
    val token: String,
    val userName: String
)