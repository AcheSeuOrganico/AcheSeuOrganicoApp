package com.example.acheseuorganico

data class RegisterRequest(
    val username: String,
    val password: String,
    val password2: String,
    val first_name: String,
    val last_name: String,
    val email: String,
    val fantasy_name: String,
    val address: Address,
    val user_type: String
)