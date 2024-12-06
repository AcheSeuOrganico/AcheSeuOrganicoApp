package com.example.acheseuorganico

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import android.text.Editable
import android.text.TextWatcher

class MyEventsActivity : AppCompatActivity() {

    private lateinit var footerTextView: TextView

    override fun onResume() {
        super.onResume()
        fetchOrganizations()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_events_activity)

        footerTextView = findViewById(R.id.footerTextView)
        footerTextView.setOnClickListener {
            val intent = Intent(this@MyEventsActivity, AboutUsActivity::class.java)
            startActivity(intent)
        }

        val goBackButton: Button = findViewById(R.id.goBackButton)
        goBackButton.setOnClickListener {
            finish()
        }

        val createOrganizationButton: Button = findViewById(R.id.addOrganizationButton)
        createOrganizationButton.setOnClickListener {
            val intent = Intent(this@MyEventsActivity, CreateOrganizationActivity::class.java)
            startActivity(intent)
        }

        fetchOrganizations()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val newOrganizationCreated = data?.getBooleanExtra("new_organization_created", false) ?: false
            if (newOrganizationCreated) {
                fetchOrganizations()
            }
        }
    }

    private fun fetchOrganizations() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.3:8000/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

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
                    displayOrganizations(organizations)
                } else {
                    Log.e("MyEventsActivity", "Request failed: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MyEventsActivity, "Failed to load organizations", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<OrganizationsResponse>, t: Throwable) {
                Log.e("MyEventsActivity", "Request failed: ${t.message}")
                Toast.makeText(this@MyEventsActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun displayOrganizations(organizations: List<Organization>) {
        val organizationsListLayout = findViewById<LinearLayout>(R.id.organizationsListLayout)
        organizationsListLayout.removeAllViews()

        for (organization in organizations) {
            val linearLayout = LinearLayout(this@MyEventsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 8, 0, 8)
            }

            val textView = TextView(this@MyEventsActivity).apply {
                text = organization.fantasy_name
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@MyEventsActivity, R.color.black))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val button = Button(this@MyEventsActivity).apply {
                background = ContextCompat.getDrawable(this@MyEventsActivity, R.drawable.angle_square_right)
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.circle_size),
                    resources.getDimensionPixelSize(R.dimen.circle_size)
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                text = ""
                setOnClickListener {
                    val intent = Intent(this@MyEventsActivity, OrganizationActivity::class.java).apply {
                        putExtra("ORGANIZATION_ID", organization.id)
                    }
                    startActivity(intent)
                }
            }

            linearLayout.addView(textView)
            linearLayout.addView(button)

            organizationsListLayout.addView(linearLayout)
        }

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredOrganizations = organizations.filter {
                    it.fantasy_name.contains(s.toString(), ignoreCase = true)
                }
                displayOrganizations(filteredOrganizations)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    interface ApiService {
        @GET("v2/organizations")
        fun getOrganizations(@Query("search") userId: String): Call<OrganizationsResponse>
    }
}