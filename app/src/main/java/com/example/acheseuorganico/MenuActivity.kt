package com.example.acheseuorganico

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MenuActivity : AppCompatActivity() {

    private lateinit var organizationsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_activity) // Update to your layout name

        organizationsTextView = findViewById(R.id.organizationsTextView)

        fetchOrganizations()
    }

    private fun fetchOrganizations() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.8:8000/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val call = apiService.getOrganizations()
        call.enqueue(object : Callback<List<Organization>> {
            override fun onResponse(
                call: Call<List<Organization>>,
                response: Response<List<Organization>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val organizations = response.body()!!
                    val orgNames = organizations.joinToString("\n") { it.fantasy_name }
                    organizationsTextView.text = orgNames
                    Toast.makeText(this@MenuActivity, orgNames, Toast.LENGTH_LONG).show()
                } else {
                    Log.e("MenuActivity", "Request failed: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MenuActivity, "Failed to load organizations", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Organization>>, t: Throwable) {
                Log.e("MenuActivity", "Request failed: ${t.message}")
                Toast.makeText(this@MenuActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    interface ApiService {
        @GET("organizations/")
        fun getOrganizations(): Call<List<Organization>>
    }
}