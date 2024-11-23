package com.example.acheseuorganico

data class RegisterResponse(
    val username: String,
    val password: String,
    val password2: String,
    val first_name: String,
    val last_name: String,
    val email: String,
    val address: Address
)