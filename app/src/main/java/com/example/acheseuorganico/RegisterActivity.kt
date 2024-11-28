package com.example.acheseuorganico

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path

class RegisterActivity : AppCompatActivity() {

    private lateinit var nomeEditText: EditText
    private lateinit var sobrenomeEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var nomeFantasiaEditText: EditText
    private lateinit var cepEditText: EditText
    private lateinit var estadoEditText: EditText
    private lateinit var cidadeEditText: EditText
    private lateinit var enderecoEditText: EditText
    private lateinit var numeroEditText: EditText
    private lateinit var registerButton: Button

    private var latitude: String? = null
    private var longitude: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        cepEditText = findViewById(R.id.cepEditText)
        estadoEditText = findViewById(R.id.estadoEditText)
        cidadeEditText = findViewById(R.id.cidadeEditText)
        enderecoEditText = findViewById(R.id.enderecoEditText)
        registerButton = findViewById(R.id.registerButton)

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

        registerButton.setOnClickListener {
            registerUser()
        }
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
                        estadoEditText.setText(address.state)
                        cidadeEditText.setText(address.city)
                        enderecoEditText.setText(address.address)


                        latitude = address.lat
                        longitude = address.lng
                    } else {
                        Toast.makeText(this@RegisterActivity, "CEP inválido ou incompleto", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("RegisterActivity", "Error: ${response.message()}")
                    Toast.makeText(this@RegisterActivity, "Erro ao buscar o CEP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AddressResponse>, t: Throwable) {
                Log.e("RegisterActivity", "Failure: ${t.message}")
                Toast.makeText(this@RegisterActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun registerUser() {
        nomeEditText = findViewById(R.id.nomeEditText)
        sobrenomeEditText = findViewById(R.id.sobrenomeEditText)
        usernameEditText = findViewById(R.id.usuarioEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.senhaEditText)
        confirmPasswordEditText = findViewById(R.id.repetirSenhaEditText)
        nomeFantasiaEditText = findViewById(R.id.nomeFantasiaEditText)
        cepEditText = findViewById(R.id.cepEditText)
        estadoEditText = findViewById(R.id.estadoEditText)
        cidadeEditText = findViewById(R.id.cidadeEditText)
        enderecoEditText = findViewById(R.id.enderecoEditText)
        numeroEditText = findViewById(R.id.numeroEditText)
        val latitudeValue = latitude ?: "0.0"
        val longitudeValue = longitude ?: "0.0"

        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val password2 = confirmPasswordEditText.text.toString().trim()
        val firstName = nomeEditText.text.toString().trim()
        val lastName = sobrenomeEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val fantasyName = nomeFantasiaEditText.text.toString().trim()
        val cep = cepEditText.text.toString().trim()
        val state = estadoEditText.text.toString().trim()
        val city = cidadeEditText.text.toString().trim()
        val address = enderecoEditText.text.toString().trim()
        val number = numeroEditText.text.toString().trim()

        if (password != password2) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
            return
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.3:8002/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val registerService = retrofit.create(RegisterService::class.java)
        val registerRequest = RegisterRequest(
            username = username,
            password = password,
            password2 = password2,
            first_name = firstName,
            last_name = lastName,
            email = email,
            user_type = "2",
            address = Address(
                cep = cep,
                name = address,
                state = state,
                city = city,
                latitude = latitudeValue,
                longitude = longitudeValue,
                number = number
            ),
            fantasy_name = fantasyName
        )

        val call = registerService.registerUser(registerRequest)
        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse != null) {
                        Toast.makeText(this@RegisterActivity, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Log.e("RegisterActivity", "Registration failed: ${response.errorBody()?.string()}")
                    Toast.makeText(this@RegisterActivity, "Erro ao realizar cadastro: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.e("RegisterActivity", "onFailure: ${t.message}")
                Toast.makeText(this@RegisterActivity, "Erro: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}

interface CepService {
    @GET("json/{cep}")
    fun getAddress(@Path("cep") cep: String): Call<AddressResponse>
}

interface RegisterService {
    @POST("accounts/register/")
    fun registerUser(@Body registerRequest: RegisterRequest): Call<RegisterResponse>
}