package com.example.acheseuorganico

data class Organization(
    val id: Int,
    val fantasy_name: String,
    val username: String,
    val email: String,
    val user_type: UserType,
    val address: Address,
    val products: List<Product>,
    val img: String
)