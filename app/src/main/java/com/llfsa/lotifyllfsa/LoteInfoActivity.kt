package com.llfsa.lotifyllfsa

import android.Manifest
import android.content.Intent
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
    private lateinit var weatherForecastTextView: TextView

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lote_info)

        // Inicializar FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Recuperar los datos del intent
        val latitud = intent.getDoubleExtra("latitud", 0.0)
        val longitud = intent.getDoubleExtra("longitud", 0.0)
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
        val addRecorridoButton: Button = findViewById(R.id.addRecorridoButton)

        weatherTempTextView = findViewById(R.id.weatherTempTextView)
        weatherConditionTextView = findViewById(R.id.weatherConditionTextView)
        weatherForecastTextView = findViewById(R.id.weatherForecastTextView)

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

        // Configurar el botón de agregar recorrido
        addRecorridoButton.setOnClickListener {
            val intent = Intent(this, recorridosActivity::class.java)
            startActivity(intent)
        }

        // Verificar permisos y obtener la ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }

        // Mostrar la ubicación en el mapa
        if (latitud != 0.0 && longitud != 0.0) {
            fetchWeather(latitud, longitud)
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

        // Llamada para obtener el clima actual
        RetrofitClient.weatherApiService.getCurrentWeather(apiKey, location, lang= "es")
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if (response.isSuccessful) {
                        val weatherResponse = response.body()
                        if (weatherResponse != null) {
                            // Actualizar la vista con el clima actual
                            weatherTempTextView.text = "Temperatura: ${weatherResponse.current.temp_c} °C"
                            weatherConditionTextView.text = "Condición: ${translateCondition(weatherResponse.current.condition.text)}"
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    // Manejar el error
                }
            })

        // Llamada para obtener el pronóstico
        RetrofitClient.weatherApiService.getWeatherForecast(apiKey, location, days = 3, lang = "es") // days = número de días de pronóstico
            .enqueue(object : Callback<ForecastResponse> {
                override fun onResponse(call: Call<ForecastResponse>, response: Response<ForecastResponse>) {
                    if (response.isSuccessful) {
                        val forecastResponse = response.body()
                        if (forecastResponse != null) {
                            // Actualizar la vista con el pronóstico
                            val forecast = forecastResponse.forecast.forecastday.joinToString("\n") { day ->
                                "Fecha: ${day.date}\n" +
                                        "Temp Max: ${day.day.maxtemp_c} °C\n" +
                                        "Temp Min: ${day.day.mintemp_c} °C\n" +
                                        "Condición: ${translateCondition(day.day.condition.text)}\n"
                            }
                            weatherForecastTextView.text = forecast
                        }
                    }
                }

                override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                    // Manejar el error
                }
            })
    }

    private fun translateCondition(condition: String): String {
        return when (condition) {
            "Clear" -> "Despejado"
            "Partly cloudy" -> "Parcialmente nublado"
            "Cloudy" -> "Nublado"
            "Overcast" -> "Cubierto"
            "Mist" -> "Niebla"
            "Fog" -> "Niebla"
            "Rain" -> "Lluvia"
            "Drizzle" -> "Llovizna"
            "Snow" -> "Nieve"
            "Sleet" -> "Aguanieve"
            "Hail" -> "Granizo"
            "Thunderstorm" -> "Tormenta"
            else -> condition
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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

