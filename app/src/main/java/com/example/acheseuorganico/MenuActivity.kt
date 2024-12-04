package com.example.acheseuorganico

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.EditText
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
import android.text.Editable
import android.text.TextWatcher

class MenuActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private var organizations: List<Organization> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_activity)

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterOrganizations(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        fetchOrganizations()
    }

    private fun filterOrganizations(query: String) {
        val filteredOrganizations = organizations.filter { organization ->
            organization.fantasy_name.contains(query, ignoreCase = true)
        }
        updateOrganizationsList(filteredOrganizations)
    }

    private fun fetchOrganizations() {
        tokenManager = TokenManager(this)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.3:8000/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.getOrganizations()

        val myFarmButton = findViewById<Button>(R.id.myOrganizations)
        myFarmButton.setOnClickListener {
            val intent = Intent(this@MenuActivity, MyEventsActivity::class.java).apply {
                putExtra("USER_ID", tokenManager.getUserIdFromToken())
            }
            startActivity(intent)
        }

        call.enqueue(object : Callback<OrganizationsResponse> {
            override fun onResponse(
                call: Call<OrganizationsResponse>,
                response: Response<OrganizationsResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    organizations = response.body()!!.results
                    updateOrganizationsList(organizations)
                } else {
                    Log.e("MenuActivity", "Request failed: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MenuActivity, "Failed to load organizations", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<OrganizationsResponse>, t: Throwable) {
                Log.e("MenuActivity", "Request failed: ${t.message}")
                Toast.makeText(this@MenuActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateOrganizationsList(organizations: List<Organization>) {
        val organizationsListLayout = findViewById<LinearLayout>(R.id.organizationsListLayout)
        organizationsListLayout.removeAllViews()

        for (organization in organizations) {
            val linearLayout = LinearLayout(this@MenuActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 8, 0, 8)
            }

            val textView = TextView(this@MenuActivity).apply {
                text = organization.fantasy_name
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@MenuActivity, R.color.black))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val button = Button(this@MenuActivity).apply {
                background = ContextCompat.getDrawable(this@MenuActivity, R.drawable.angle_square_right)
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.circle_size),
                    resources.getDimensionPixelSize(R.dimen.circle_size)
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                text = ""
                setOnClickListener {
                    val intent = Intent(this@MenuActivity, OrganizationActivity::class.java).apply {
                        putExtra("ORGANIZATION_ID", organization.id)
                    }
                    startActivity(intent)
                }
            }

            linearLayout.addView(textView)
            linearLayout.addView(button)

            organizationsListLayout.addView(linearLayout)
        }
    }

    interface ApiService {
        @GET("v2/organizations/")
        fun getOrganizations(): Call<OrganizationsResponse>
    }
}