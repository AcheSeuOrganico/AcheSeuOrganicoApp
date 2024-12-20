package com.example.acheseuorganico

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PATCH
import retrofit2.http.GET
import android.content.Intent

class UpdateOrganizationActivity : AppCompatActivity() {

    private val productsMap = linkedMapOf(
        1 to "Vegetais",
        2 to "Frutas",
        3 to "Produtos",
        4 to "Verduras"
    )

    private lateinit var tokenManager: TokenManager
    private lateinit var fantasyNameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var cepEditText: EditText
    private lateinit var addressNameEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var stateEditText: EditText
    private lateinit var numberEditText: EditText
    private lateinit var selectedProducts: MutableList<Int>

    private var latitude: String? = null
    private var longitude: String? = null
    private var organizationId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.update_organization_activity)

        fantasyNameEditText = findViewById(R.id.fantasyNameEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        cepEditText = findViewById(R.id.cepEditText)
        addressNameEditText = findViewById(R.id.addressNameEditText)
        cityEditText = findViewById(R.id.cityEditText)
        stateEditText = findViewById(R.id.stateEditText)
        numberEditText = findViewById(R.id.numberEditText)
        val saveButton: Button = findViewById(R.id.saveButton)

        selectedProducts = mutableListOf()
        val productsTextView: TextView = findViewById(R.id.productsTextView)
        productsTextView.setOnClickListener {
            showMultiSelectDialog(productsTextView)
        }

        organizationId = intent.getIntExtra("ORGANIZATION_ID", -1)
        if (organizationId != -1) {
            fetchOrganizationDetails(organizationId)
        }

        saveButton.text = "Atualizar"
        saveButton.setOnClickListener {
            updateOrganization(organizationId)
        }

        cepEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val cep = s.toString().trim()
                if (cep.length == 8) {
                    fetchAddress(cep)
                }
            }
        })
    }

    private fun fetchOrganizationDetails(id: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.3:8000/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.getOrganization(id)

        call.enqueue(object : Callback<Organization> {
            override fun onResponse(call: Call<Organization>, response: Response<Organization>) {
                if (response.isSuccessful) {
                    val organization = response.body()
                    if (organization != null) {
                        prefillFields(organization)
                    }
                } else {
                    Toast.makeText(this@UpdateOrganizationActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Organization>, t: Throwable) {
                Toast.makeText(this@UpdateOrganizationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun prefillFields(organization: Organization) {
        fantasyNameEditText.setText(organization.fantasy_name)
        descriptionEditText.setText(organization.description)
        cepEditText.setText(organization.address.cep)
        addressNameEditText.setText(organization.address.name)
        cityEditText.setText(organization.address.city)
        stateEditText.setText(organization.address.state)
        numberEditText.setText(organization.address.number)

        latitude = organization.address.latitude
        longitude = organization.address.longitude

        selectedProducts.clear()
//        selectedProducts.addAll(organization)
    }

    private fun fetchAddress(cep: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://cep.awesomeapi.com.br/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val cepService = retrofit.create(CepService::class.java)
        val call = cepService.getAddress(cep)

        call.enqueue(object : Callback<AddressResponse> {
            override fun onResponse(call: Call<AddressResponse>, response: Response<AddressResponse>) {
                if (response.isSuccessful) {
                    val address = response.body()
                    if (address != null && address.address != null && address.city != null && address.state != null) {
                        stateEditText.setText(address.state)
                        cityEditText.setText(address.city)
                        addressNameEditText.setText(address.address)

                        latitude = address.lat
                        longitude = address.lng
                    } else {
                        Toast.makeText(this@UpdateOrganizationActivity, "CEP inválido ou incompleto", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("CreateOrganization", "Error: ${response.message()}")
                    Toast.makeText(this@UpdateOrganizationActivity, "Erro ao buscar o CEP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AddressResponse>, t: Throwable) {
                Log.e("CreateOrganization", "Failure: ${t.message}")
                Toast.makeText(this@UpdateOrganizationActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateOrganization(organizationId: Int) {
        val fantasyName = fantasyNameEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val cep = cepEditText.text.toString().trim()
        val addressName = addressNameEditText.text.toString().trim()
        val city = cityEditText.text.toString().trim()
        val state = stateEditText.text.toString().trim()
        val latitudeValue = latitude ?: "0.0"
        val longitudeValue = longitude ?: "0.0"
        val number = numberEditText.text.toString().trim()

        tokenManager = TokenManager(this)
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
                city = city,
                state = state,
                latitude = latitudeValue,
                longitude = longitudeValue,
                number = number
            ),
            products = selectedProducts,
            user_id = tokenManager.getUserIdFromToken()
        )

        val call = apiService.updateOrganization(organizationId, organizationRequest)
        call.enqueue(object : Callback<OrganizationPostResponse> {
            override fun onResponse(call: Call<OrganizationPostResponse>, response: Response<OrganizationPostResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@UpdateOrganizationActivity, "Organization updated successfully!", Toast.LENGTH_LONG).show()
                    val intent = Intent()
                    intent.putExtra("organization_updated", true)
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    Log.e("UpdateOrganization", "Update failed: ${response.message()}")
                    Toast.makeText(this@UpdateOrganizationActivity, "Error: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<OrganizationPostResponse>, t: Throwable) {
                Log.e("UpdateOrganization", "onFailure: ${t.message}")
                Toast.makeText(this@UpdateOrganizationActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showMultiSelectDialog(productsTextView: TextView) {
        val selectedItems = BooleanArray(productsMap.size)
        val itemsArray = productsMap.values.toTypedArray()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecionar produtos")
        builder.setMultiChoiceItems(itemsArray, selectedItems) { _, which, isChecked ->
            val key = productsMap.keys.elementAt(which)
            if (isChecked) {
                if (!selectedProducts.contains(key)) selectedProducts.add(key)
            } else {
                selectedProducts.remove(key)
            }
        }
        builder.setPositiveButton("OK") { _, _ ->
            productsTextView.text = selectedProducts.joinToString { productsMap[it] ?: "" }
        }
        builder.setNegativeButton("Cancel", null)

        builder.create().show()
    }

    interface ApiService {
        @POST("organizations/")
        fun createOrganization(@Body organizationRequest: OrganizationRequest): Call<OrganizationPostResponse>

        @GET("organization/{id}")
        fun getOrganization(@Path("id") id: Int): Call<Organization>

        @PATCH("organizations/{id}/update")
        fun updateOrganization(@Path("id") id: Int, @Body organizationRequest: OrganizationRequest): Call<OrganizationPostResponse>
    }

    interface CepService {
        @GET("json/{cep}")
        fun getAddress(@Path("cep") cep: String): Call<AddressResponse>
    }
}