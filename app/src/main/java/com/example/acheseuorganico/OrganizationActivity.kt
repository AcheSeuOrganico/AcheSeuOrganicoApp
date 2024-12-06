package com.example.acheseuorganico

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
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
    private lateinit var descriptionTextView: TextView
    private lateinit var organizationImageView: ImageView
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button
    private lateinit var tokenManager: TokenManager

    val productIcons = mapOf(
        "1" to R.drawable.vegetais,
        "2" to R.drawable.frutas,
        "3" to R.drawable.produtos,
        "4" to R.drawable.verduras
    )

    override fun onResume() {
        super.onResume()
        val organizationId = intent.getIntExtra("ORGANIZATION_ID", -1)
        fetchOrganizationDetails(organizationId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization_details)

        fantasyNameTextView = findViewById(R.id.fantasyNameTextView)
        cityTextView = findViewById(R.id.cityTextView)
        stateTextView = findViewById(R.id.stateTextView)
        streetTextView = findViewById(R.id.streetTextView)
        numberTextView = findViewById(R.id.numberTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        organizationImageView = findViewById(R.id.organizationImageView)
        editButton = findViewById(R.id.editButton)
        deleteButton = findViewById(R.id.deleteButton)

        tokenManager = TokenManager(this)

        val organizationId = intent.getIntExtra("ORGANIZATION_ID", -1)

        val goBackButton: Button = findViewById(R.id.goBackButton)
        goBackButton.setOnClickListener { finish() }

        editButton.setOnClickListener {
            val intent = Intent(this, UpdateOrganizationActivity::class.java)
            intent.putExtra("ORGANIZATION_ID", organizationId)
            startActivity(intent)
        }

        deleteButton.setOnClickListener { confirmAndDeleteOrganization(organizationId) }

        editButton.visibility = Button.GONE
        deleteButton.visibility = Button.GONE

        fetchOrganizationDetails(organizationId)
    }

    private fun confirmAndDeleteOrganization(organizationId: Int) {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Excluir Organização")
            .setMessage("Você tem certeza que deseja excluir esta organização?")
            .setPositiveButton("Sim") { _, _ -> deleteOrganization(organizationId) }
            .setNegativeButton("Não", null)
            .create()
        alertDialog.show()
    }

    private fun deleteOrganization(organizationId: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.3:8000/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.deleteOrganization(organizationId)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@OrganizationActivity, "Organização excluída com sucesso", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@OrganizationActivity, "Erro ao excluir organização", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@OrganizationActivity, "Erro: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun renderProducts(products: List<Product>) {
        val productsContainer: LinearLayout = findViewById(R.id.productsContainer)
        productsContainer.removeAllViews()

        if (products.isEmpty()) return

        val svgMap = mapOf(
            "1" to R.drawable.vegetais,
            "2" to R.drawable.frutas,
            "3" to R.drawable.produtos,
            "4" to R.drawable.verduras
        )

        products.forEach { product ->
            val drawableResId = svgMap[product.product_id] ?: return@forEach
            val imageView = ImageView(this).apply {
                setImageResource(drawableResId)
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.icon_size),
                    resources.getDimensionPixelSize(R.dimen.icon_size)
                ).apply {
                    marginEnd = 16
                }
            }
            productsContainer.addView(imageView)
        }
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

                    fantasyNameTextView.text = organization.fantasy_name
                    descriptionTextView.text = organization.description
                    stateTextView.text = organization.address.state
                    cityTextView.text = organization.address.city
                    streetTextView.text = organization.address.name
                    numberTextView.text = organization.address.number

                    val imageUrl = "http://192.168.0.3:8000${organization.img}"
                    Glide.with(this@OrganizationActivity)
                        .load(imageUrl)
                        .into(organizationImageView)

                    renderProducts(organization.products)

                    val apiUserId = organization.user_id
                    val localUserId = tokenManager.getUserIdFromToken()
                    if (apiUserId == localUserId) {
                        editButton.visibility = Button.VISIBLE
                        deleteButton.visibility = Button.VISIBLE
                    }
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

        @retrofit2.http.DELETE("v2/organizations/{id}/delete")
        fun deleteOrganization(@Path("id") organizationId: Int): Call<Void>
    }
}