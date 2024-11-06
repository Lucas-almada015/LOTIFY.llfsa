package com.llfsa.lotifyllfsa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LoteInfoActivity : AppCompatActivity(), OnDayClickListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherTempTextView: TextView
    private lateinit var weatherConditionTextView: TextView
    private lateinit var weatherForecastTextView: TextView
    private lateinit var weatherHumidityTextView: TextView
    private lateinit var weatherWindTextView: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var calendarView: CalendarView
    private var IDlote: String = ""

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lote_info)

        // Inicializar Firebase Firestore
        db = FirebaseFirestore.getInstance()

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
        val IDlote = intent.getStringExtra("IDlote") ?: ""

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
        weatherHumidityTextView = findViewById(R.id.weatherHumidityTextView)
        weatherWindTextView = findViewById(R.id.weatherWindTextView)
        calendarView = findViewById(R.id.calendarView)

        // Setear los datos en las vistas
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.demo4)
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
            val intent = Intent(this, recorridosActivity::class.java).apply {
                putExtra("IDlote", IDlote) // Pasa el ID del lote a RecorridosActivity
            }
            startActivity(intent)
        }

        db.collection("recorridos")
            .whereEqualTo("IDlote", IDlote)
            .get()
            .addOnSuccessListener { documents ->
                val fechasRecorridos = mutableListOf<Long>()
                for (document in documents) {
                    Log.d("LoteInfoActivity", "Documento: ${document.id}")
                    val fechaTimestamp = document.getTimestamp("fecha")
                    if (fechaTimestamp != null) {
                        fechasRecorridos.add(fechaTimestamp.toDate().time)
                    } else {
                        val fechaString = document.getString("fecha")
                        if (fechaString != null) {
                            try {
                                val simpleDateFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy, h:mm:ss a", Locale("es", "AR"))
                                val date = simpleDateFormat.parse(fechaString)
                                if (date != null) {
                                    fechasRecorridos.add(date.time)
                                }
                            } catch (e: Exception) {
                                Log.e("LoteInfoActivity","Error al parsear la fecha: ${e.message}")
                            }
                        }
                    }
                }
                Log.d("LoteInfoActivity", "Fechas recorridos: $fechasRecorridos")
                actualizarCalendario(fechasRecorridos)
            }
            .addOnFailureListener { exception ->
                Log.w("LoteInfoActivity", "Error al obtener recorridos: ", exception)
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

        calendarView.setOnDayClickListener(this)

    }

    private fun actualizarCalendario(fechasRecorridos: List<Long>) {
        val calendarView: CalendarView = findViewById(R.id.calendarView) // Asegúrate de que el ID sea correcto

        val events = mutableListOf<EventDay>()
        for (fechaRecorrido in fechasRecorridos) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = fechaRecorrido
            events.add(EventDay(calendar, R.drawable.event_drawable)) // Reemplaza your_event_drawable con tu drawable
        }
        calendarView.setEvents(events)
    }
    override fun onDayClick(eventDay: EventDay) {
        // Aquí se ejecutará el código cuando se presione un día en el calendario
        val clickedDayCalendar = eventDay.calendar
        // Obtén la fecha del día presionado
        val year = clickedDayCalendar.get(Calendar.YEAR)
        val month = clickedDayCalendar.get(Calendar.MONTH)
        val day = clickedDayCalendar.get(Calendar.DAY_OF_MONTH)

        // Busca la información del recorrido para la fecha seleccionada
        buscarInformacionRecorrido(year, month, day)
    }

    private fun buscarInformacionRecorrido(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, 0, 0, 0)
        val dateFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy, h:mm:ss a", Locale("es", "AR"))
        val formattedDate = dateFormat.format(calendar.time) + " UTC-3" // Agrega la zona horaria
        val startOfDay = Timestamp(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, 1) // Agrega un día para obtener el final del día
        val endOfDay = Timestamp(calendar.time)
        // Crea una consulta para buscar el recorrido con la fecha especificada
        val query = db.collection("recorridos")
            .whereEqualTo("IDlote", IDlote) // Asegúrate de que IDlote esté definido correctamente
            .whereEqualTo("fecha", formattedDate) // Consulta por la fecha
            .whereLessThan("fecha", endOfDay)

        // Ejecuta la consulta
        query.get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Se encontró un recorrido para la fecha especificada
                    val recorrido = documents.first().data // Obtén los datos del recorrido
                    // Muestra la información del recorrido en un Toast o en una nueva actividad
                    val informacionRecorrido = """
                     Cultivo: ${recorrido["cultivo"]}
                     Plagas: ${recorrido["plagas"]}
                     Enfermedades: ${recorrido["enfermedades"]}
                     Maleza: ${recorrido["maleza"]}
                     Aplicación de plaga: ${recorrido["aplicacion-de-plaga"]}
                     Aplicación de enfermedades: ${recorrido["aplicacion-de-enfermedades"]}
                     Cobertura: ${recorrido["cobertura"]}
                     Tamaño de maleza: ${recorrido["tamano-de-maleza"]}
                     Umbral de plagas: ${recorrido["umbral-de-plagas"]}
                     Info anexo: ${recorrido["info-anexo"]}
                 """.trimIndent()
                    Toast.makeText(this, informacionRecorrido, Toast.LENGTH_LONG).show()
                } else {
                    // No se encontró un recorrido para la fecha especificada
                    Toast.makeText(this, "No hay recorrido para esta fecha", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("LoteInfoActivity", "Error al obtener el recorrido: ", exception)
            }
    }

    private fun getTimestampForDate(year: Int, month: Int, day: Int): Timestamp {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, 0, 0, 0) // Establece la fecha y hora a las 00:00:00
        val date = calendar.time
        return Timestamp(date)
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

        // Clima actual
        RetrofitClient.weatherApiService.getCurrentWeather(apiKey, location, lang = "es")
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if (response.isSuccessful) {
                        val weatherResponse = response.body()
                        if (weatherResponse != null) {
                            weatherTempTextView.text = "Temperatura: ${weatherResponse.current.temp_c} °C"
                            weatherConditionTextView.text = "Condición: ${translateCondition(weatherResponse.current.condition.text)}"
                            weatherHumidityTextView.text = "Humedad: ${weatherResponse.current.humidity} %"
                            weatherWindTextView.text = "Viento: ${weatherResponse.current.wind_kph} km/h"
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    // Manejar el error
                }
            })

        // Pronóstico del clima para 7 días
        RetrofitClient.weatherApiService.getWeatherForecast(apiKey, location, days = 7, lang = "es")
            .enqueue(object : Callback<ForecastResponse> {
                override fun onResponse(call: Call<ForecastResponse>, response: Response<ForecastResponse>) {
                    if (response.isSuccessful) {
                        val forecastResponse = response.body()
                        if (forecastResponse != null) {
                            val forecast = forecastResponse.forecast.forecastday.joinToString("\n") { day ->
                                "Fecha: ${day.date}\n" +
                                        "Temp Max: ${day.day.maxtemp_c} °C\n" +
                                        "Temp Min: ${day.day.mintemp_c} °C\n" +
                                        "Condición: ${translateCondition(day.day.condition.text)}\n" +
                                        "Humedad: ${day.day.humidity} %\n" +
                                        "Viento: ${day.day.wind_kph} km/h\n"
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

