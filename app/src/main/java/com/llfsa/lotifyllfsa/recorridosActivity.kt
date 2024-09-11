package com.llfsa.lotifyllfsa

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class recorridosActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recorridos)

        // Configurar la vista para manejar los insets de la ventana
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener las vistas por ID
        val cultivoSpinner: Spinner = findViewById(R.id.cultivoSpinner)
        val malezaSpinner: Spinner = findViewById(R.id.malezaSpinner)
        val coberturaSpinner: Spinner = findViewById(R.id.coberturaSpinner)
        val tamanoMalezaSpinner: Spinner = findViewById(R.id.tamanoMalezaSpinner)
        val plagasSpinner: Spinner = findViewById(R.id.plagasSpinner)
        val aplicacionPlagaSpinner: Spinner = findViewById(R.id.aplicacionPlagaSpinner)
        val aplicacionEnfermedadesSpinner: Spinner = findViewById(R.id.aplicacionEnfermedadesSpinner)
        val umbralPlagasEditText: EditText = findViewById(R.id.umbralPlagasEditText)
        val enfermedadesEditText: EditText = findViewById(R.id.enfermedadesEditText)
        val infoAnexoEditText: EditText = findViewById(R.id.infoAnexoEditText)
        val saveButton: Button = findViewById(R.id.saveButton)
        val backButton: Button = findViewById(R.id.backButton)
        val fechaTextView: TextView = findViewById(R.id.fechaTextView)

        // Configurar el DatePickerDialog
        fechaTextView.setOnClickListener {
            showDatePicker()
        }

        // Cargar datos en los Spinners
        loadSpinnerData(cultivoSpinner, "cultivos")
        loadSpinnerData(malezaSpinner, "maleza")
        loadSpinnerData(plagasSpinner, "plagas")
        loadSpinnerData(coberturaSpinner, "cobertura")
        loadSpinnerData(tamanoMalezaSpinner, "tamano_maleza")
        loadSpinnerData(aplicacionPlagaSpinner, "aplicacion_plaga")
        loadSpinnerData(aplicacionEnfermedadesSpinner, "aplicacion_enfermedades")

        // Configurar el botón de guardar
        saveButton.setOnClickListener {
            saveRecorrido()
        }

        // Manejar el clic en el botón "Volver"
        backButton.setOnClickListener {
            finish() // Cierra la actividad actual y vuelve a la actividad anterior
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate)
                findViewById<TextView>(R.id.fechaTextView).text = formattedDate
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun loadSpinnerData(spinner: Spinner, collection: String) {
        val dataList = mutableListOf<String>()
        db.collection(collection)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val item = document.getString("tipo")
                    item?.let { dataList.add(it) }
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dataList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
    }

    private fun saveRecorrido() {
        val fechaTextView: TextView = findViewById(R.id.fechaTextView)
        val cultivoSpinner: Spinner = findViewById(R.id.cultivoSpinner)
        val malezaSpinner: Spinner = findViewById(R.id.malezaSpinner)
        val coberturaSpinner: Spinner = findViewById(R.id.coberturaSpinner)
        val tamanoMalezaSpinner: Spinner = findViewById(R.id.tamanoMalezaSpinner)
        val plagasSpinner: Spinner = findViewById(R.id.plagasSpinner)
        val aplicacionPlagaSpinner: Spinner = findViewById(R.id.aplicacionPlagaSpinner)
        val aplicacionEnfermedadesSpinner: Spinner = findViewById(R.id.aplicacionEnfermedadesSpinner)
        val umbralPlagasEditText: EditText = findViewById(R.id.umbralPlagasEditText)
        val enfermedadesEditText: EditText = findViewById(R.id.enfermedadesEditText)
        val infoAnexoEditText: EditText = findViewById(R.id.infoAnexoEditText)

        val fecha = fechaTextView.text.toString()
        val cultivo = cultivoSpinner.selectedItem.toString()
        val maleza = malezaSpinner.selectedItem.toString()
        val cobertura = coberturaSpinner.selectedItem.toString()
        val tamanoMaleza = tamanoMalezaSpinner.selectedItem.toString()
        val plagas = plagasSpinner.selectedItem.toString()
        val aplicacionPlaga = aplicacionPlagaSpinner.selectedItem.toString()
        val aplicacionEnfermedades = aplicacionEnfermedadesSpinner.selectedItem.toString()
        val umbralPlagas = umbralPlagasEditText.text.toString()
        val enfermedades = enfermedadesEditText.text.toString()
        val infoAnexo = infoAnexoEditText.text.toString()

        val recorridoData = hashMapOf(
            "fecha" to fecha,
            "cultivo" to cultivo,
            "maleza" to maleza,
            "cobertura" to cobertura,
            "tamano_maleza" to tamanoMaleza,
            "plagas" to plagas,
            "aplicacion_plaga" to aplicacionPlaga,
            "aplicacion_enfermedades" to aplicacionEnfermedades,
            "umbral_plagas" to umbralPlagas,
            "enfermedades" to enfermedades,
            "info_anexo" to infoAnexo
        )

        db.collection("recorridos")
            .add(recorridoData)
            .addOnSuccessListener {
                // Mostrar mensaje de éxito o hacer algo después de guardar
            }
            .addOnFailureListener { e ->
                // Manejar errores
            }
    }
}

