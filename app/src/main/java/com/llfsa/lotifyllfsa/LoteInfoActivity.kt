package com.llfsa.lotifyllfsa

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoteInfoActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherTempTextView: TextView
    private lateinit var weatherConditionTextView: TextView

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lote_info)

        // Configuración de insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recuperar los datos del intent
        val imageUrl = intent.getStringExtra("imageUrl")
        val nombre = intent.getStringExtra("nombre")
        val numeroLote = intent.getStringExtra("numeroLote")
        val tamaño = intent.getStringExtra("tamaño")
        val ubicacion = intent.getStringExtra("ubicacion")

        // Referencias a las vistas
        val imageView: ImageView = findViewById(R.id.loteInfoImageView)
        val nombreTextView: TextView = findViewById(R.id.loteInfoNombreTextView)
        val numeroLoteTextView: TextView = findViewById(R.id.loteInfoNumeroLoteTextView)
        val tamañoTextView: TextView = findViewById(R.id.loteInfoTamañoTextView)
        val ubicacionTextView: TextView = findViewById(R.id.loteInfoUbicacionTextView)
        val backButton: Button = findViewById(R.id.backButton)

        weatherTempTextView = findViewById(R.id.weatherTempTextView)
        weatherConditionTextView = findViewById(R.id.weatherConditionTextView)

        // Inicializar FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setear los datos en las vistas
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(imageView)

        nombreTextView.text = nombre
        numeroLoteTextView.text = numeroLote
        tamañoTextView.text = tamaño
        ubicacionTextView.text = ubicacion

        // Configurar el botón de volver
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Verificar permisos y obtener la ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener(this, OnSuccessListener { location: Location? ->
                    if (location != null) {
                        val lat = location.latitude
                        val lon = location.longitude
                        fetchWeather(lat, lon)
                    } else {
                        Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error de permiso de ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        val apiKey = "da6c9b620e5c4ab28fd14800242108"
        val location = "$lat,$lon"

        RetrofitClient.weatherApiService.getCurrentWeather(apiKey, location)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if (response.isSuccessful) {
                        val weatherResponse = response.body()
                        weatherTempTextView.text = "Temperatura: ${weatherResponse?.current?.temp_c} °C"
                        weatherConditionTextView.text = "Condición: ${weatherResponse?.current?.condition?.text}"
                    } else {
                        Toast.makeText(this@LoteInfoActivity, "Error al obtener el clima", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Toast.makeText(this@LoteInfoActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
