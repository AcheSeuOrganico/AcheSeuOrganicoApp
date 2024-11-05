package com.example.acheseuorganico

data class OrganizationPostResponse(
    val id: Int,
    val fantasy_name: String,
    val description: String,
    val address: Address
)