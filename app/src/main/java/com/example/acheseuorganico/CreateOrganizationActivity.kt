package com.example.acheseuorganico

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.view.View

class CreateOrganizationActivity : AppCompatActivity() {

    private lateinit var fantasyNameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var cepEditText: EditText
    private lateinit var addressNameEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var stateEditText: EditText
    private lateinit var productsSpinner: Spinner
    private lateinit var selectedProducts: List<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_organization_activity)

        fantasyNameEditText = findViewById(R.id.fantasyNameEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        cepEditText = findViewById(R.id.cepEditText)
        addressNameEditText = findViewById(R.id.addressNameEditText)
        cityEditText = findViewById(R.id.cityEditText)
        stateEditText = findViewById(R.id.stateEditText)
        val saveButton: Button = findViewById(R.id.saveButton)

        productsSpinner = findViewById(R.id.productsSpinner)

        val productOptions = listOf(1, 2, 3, 4)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, productOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        productsSpinner.adapter = adapter

        productsSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedProducts = listOf(productOptions[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedProducts = emptyList()
            }
        })

        saveButton.setOnClickListener {
            createOrganization()
        }
    }

    private fun createOrganization() {
        val fantasyName = fantasyNameEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val cep = cepEditText.text.toString().trim()
        val addressName = addressNameEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val state = stateEditText.text.toString().trim()
        val latitude = "-23.57433"
        val longitude = "-46.86267"
        val number = "283"

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.3:8000/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val organizationRequest = OrganizationRequest(
            fantasy_name = fantasyName,
            description = description,
            address = Address(
                cep = cep,
                name = addressName,
                state = state,
                city = city,
                latitude = latitude,
                longitude = longitude,
                number = number
            ),
            products = selectedProducts
        )

        val call = apiService.createOrganization(organizationRequest)
        call.enqueue(object : Callback<OrganizationPostResponse> {
            override fun onResponse(
                call: Call<OrganizationPostResponse>,
                response: Response<OrganizationPostResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateOrganizationActivity, "Organization created successfully!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Log.e("CreateOrganization", "Creation failed: ${response.message()}")
                    Toast.makeText(this@CreateOrganizationActivity, "Error: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<OrganizationPostResponse>, t: Throwable) {
                Log.e("CreateOrganization", "onFailure: ${t.message}")
                Toast.makeText(this@CreateOrganizationActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    interface ApiService {
        @POST("organizations/")
        fun createOrganization(@Body organizationRequest: OrganizationRequest): Call<OrganizationPostResponse>
    }
}