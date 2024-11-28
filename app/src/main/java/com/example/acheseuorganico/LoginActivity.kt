package com.example.acheseuorganico

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        val registerTextView: TextView = findViewById(R.id.registerTextView)

        loginButton.setOnClickListener {
            blockLogin()
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun blockLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.3:8000/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        val loginRequest = LoginRequest(username = email, password = password)

        val call = apiService.login(loginRequest)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putString("refresh_token", loginResponse.refresh)
                        putString("access_token", loginResponse.access)
                        apply()
                    }
                    val intent = Intent(this@LoginActivity, MenuActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("LoginActivity", "Login failed: HTTP error code: ${response.code()} msg: ${response.message()}")
                    Toast.makeText(this@LoginActivity, "Login inválido: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "onFailure: ${t.message}")
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    interface ApiService {
        @POST("auth/login/")
        fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>
    }
}
