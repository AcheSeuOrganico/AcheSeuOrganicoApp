package com.example.acheseuorganico

data class AddressResponse(
    val cep: String?,
    val address_type: String?,
    val address_name: String?,
    val address: String?,
    val state: String?,
    val district: String?,
    val lat: String?,
    val lng: String?,
    val city: String?,
    val city_ibge: String?,
    val ddd: String?
)