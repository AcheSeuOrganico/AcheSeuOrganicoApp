package com.example.acheseuorganico

data class Address(
    val cep: String,
    val name: String,
    val city: String,
    val state: String,
    val latitude: String? = null,
    val longitude: String? = null,
    val number: String? = null
)