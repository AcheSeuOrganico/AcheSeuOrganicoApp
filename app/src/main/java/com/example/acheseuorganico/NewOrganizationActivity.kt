package com.example.acheseuorganico

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class NewOrganizationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_events_activity)

        fetchOrganizations()
    }

    private fun fetchOrganizations() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.8:8000/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val goBackButton: Button = findViewById(R.id.goBackButton)
        goBackButton.setOnClickListener {
            finish()
        }

        val myFarmButton = findViewById<Button>(R.id.addOrganizationButton)
        myFarmButton.setOnClickListener {
            val intent = Intent(this@NewOrganizationActivity, MyEventsActivity::class.java).apply {
            }
            startActivity(intent)
        }

        val apiService = retrofit.create(ApiService::class.java)
        val userId = intent.getStringExtra("USER_ID") ?: "-1"
        val call = apiService.getOrganizations(userId)

        call.enqueue(object : Callback<OrganizationsResponse> {
            override fun onResponse(
                call: Call<OrganizationsResponse>,
                response: Response<OrganizationsResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val organizations = response.body()!!.results

                    val organizationsListLayout = findViewById<LinearLayout>(R.id.organizationsListLayout)
                    organizationsListLayout.removeAllViews()

                    for (organization in organizations) {
                        val linearLayout = LinearLayout(this@NewOrganizationActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setPadding(0, 8, 0, 8)
                        }

                        val textView = TextView(this@NewOrganizationActivity).apply {
                            text = organization.fantasy_name
                            textSize = 16f
                            setTextColor(ContextCompat.getColor(this@NewOrganizationActivity, R.color.black))
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                        }

                        val button = Button(this@NewOrganizationActivity).apply {
                            background = ContextCompat.getDrawable(this@NewOrganizationActivity, R.drawable.icon_button)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            setOnClickListener {
                                val intent = Intent(this@NewOrganizationActivity, OrganizationActivity::class.java).apply {
                                    putExtra("ORGANIZATION_ID", organization.id)
                                }
                                startActivity(intent)
                            }
                        }

                        linearLayout.addView(textView)
                        linearLayout.addView(button)

                        organizationsListLayout.addView(linearLayout)
                    }
                } else {
                    Log.e("MenuActivity", "Request failed: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@NewOrganizationActivity, "Failed to load organizations", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<OrganizationsResponse>, t: Throwable) {
                Log.e("MenuActivity", "Request failed: ${t.message}")
                Toast.makeText(this@NewOrganizationActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    interface ApiService {
        @GET("v2/organizations")
        fun getOrganizations(@Query("search") userId: String): Call<OrganizationsResponse>
    }
}