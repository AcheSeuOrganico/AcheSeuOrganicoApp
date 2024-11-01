package com.example.acheseuorganico

data class OrganizationsResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Organization>
)