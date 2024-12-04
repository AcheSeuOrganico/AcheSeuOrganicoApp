package com.example.acheseuorganico

data class Organization(
    val id: Int,
    val fantasy_name: String,
    val username: String,
    val description: String,
    val email: String,
    val address: Address,
    val products: List<Product>,
    val img: String,
    val user_id: String?
)