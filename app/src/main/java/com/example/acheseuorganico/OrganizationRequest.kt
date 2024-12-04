package com.example.acheseuorganico

data class OrganizationRequest(
    val fantasy_name: String,
    val description: String,
    val address: Address,
    val products: List<Int>,
    val user_id: String?
)