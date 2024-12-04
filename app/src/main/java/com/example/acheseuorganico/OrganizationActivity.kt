package com.example.acheseuorganico

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class OrganizationActivity : AppCompatActivity() {

    private lateinit var fantasyNameTextView: TextView
    private lateinit var stateTextView: TextView
    private lateinit var cityTextView: TextView
    private lateinit var streetTextView: TextView
    private lateinit var numberTextView: TextView
    private lateinit var organizationImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization_details)

        fantasyNameTextView = findViewById(R.id.fantasyNameTextView)
        cityTextView = findViewById(R.id.cityTextView)
        stateTextView = findViewById(R.id.stateTextView)
        streetTextView = findViewById(R.id.streetTextView)
        numberTextView = findViewById(R.id.numberTextView)
        organizationImageView = findViewById(R.id.organizationImageView)

        val goBackButton: Button = findViewById(R.id.goBackButton)
        goBackButton.setOnClickListener {
            finish()
        }

        val organizationId = intent.getIntExtra("ORGANIZATION_ID", -1)
        fetchOrganizationDetails(organizationId)
    }

    private fun fetchOrganizationDetails(organizationId: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.3:8000/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val call = apiService.getOrganizationDetails(organizationId)
        call.enqueue(object : Callback<Organization> {
            override fun onResponse(call: Call<Organization>, response: Response<Organization>) {
                if (response.isSuccessful && response.body() != null) {
                    val organization = response.body()!!

                    fantasyNameTextView.text = "Nome da organização: ${organization.fantasy_name}"
                    stateTextView.text = "Estado: ${organization.address.state}"
                    cityTextView.text = "Cidade: ${organization.address.city}"
                    streetTextView.text = "Logradouro: ${organization.address.name}"
                    numberTextView.text = "Numero: ${organization.address.number}"


                    val imageUrl = "http://192.168.0.3:8000${organization.img}"
                    Glide.with(this@OrganizationActivity)
                        .load(imageUrl)
                        .into(organizationImageView)
                } else {
                    Log.e("OrganizationActivity", "Request failed: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@OrganizationActivity, "Failed to load organization details", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Organization>, t: Throwable) {
                Log.e("OrganizationActivity", "Request failed: ${t.message}")
                Toast.makeText(this@OrganizationActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    interface ApiService {
        @GET("v2/organization/{id}")
        fun getOrganizationDetails(@Path("id") organizationId: Int): Call<Organization>
    }
}