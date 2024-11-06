package com.llfsa.lotifyllfsa

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class recorridosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var IDlote: String? = null // Asumimos que el ID del lote es pasado como extra

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorridos)

        // Inicializar Firestore
        db = Firebase.firestore

        // Obtener el ID del lote de los extras del Intent
        val idLote = intent.getStringExtra("IDlote")
        IDlote = idLote

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
        val backButton: Button = findViewById(R.id.cancelButton)
        val fechaTextView: TextView = findViewById(R.id.fechaTextView)

        // Configurar el DatePickerDialog
        fechaTextView.setOnClickListener {
            showDatePicker(fechaTextView)
        }

        // Cargar datos en los Spinners
        loadSpinnerData(cultivoSpinner, "cultivos")
        loadSpinnerData(malezaSpinner, "malezas")
        loadSpinnerData(plagasSpinner, "plagas")
        loadSpinnerFromArray(coberturaSpinner, R.array.cobertura_array)
        loadSpinnerFromArray(tamanoMalezaSpinner, R.array.tamano_maleza_array)
        loadSpinnerFromArray(aplicacionPlagaSpinner, R.array.aplicacion_plaga_array)
        loadSpinnerFromArray(aplicacionEnfermedadesSpinner, R.array.aplicacion_enfermedades_array)

        // Configurar el botón de guardar
        saveButton.setOnClickListener {
            saveRecorrido()
        }

        // Manejar el clic en el botón "Volver"
        backButton.setOnClickListener {
            finish() // Cierra la actividad actual y vuelve a la actividad anterior
        }

        // Obtener lote por ID (si está disponible)
        IDlote?.let { obtenerLotePorId(it) }
    }

    private fun showDatePicker(fechaTextView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                fechaTextView.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
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
                if (dataList.isNotEmpty()) {
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dataList)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                } else {
                    Toast.makeText(this, "No se encontraron elementos en $collection", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar $collection: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveRecorrido() {
        // Verificar que el ID del lote no sea nulo
        if (IDlote == null) {
            Toast.makeText(this, "El ID del lote no puede ser nulo", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener las vistas necesarias
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

        // Validar campos
        if (fechaTextView.text.isEmpty() || cultivoSpinner.selectedItem == null || malezaSpinner.selectedItem == null) {
            Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Construir el mapa de datos para guardar en Firestore
        val recorridoData = hashMapOf(
            "IDlote" to IDlote,
            "fecha" to Timestamp.now(),
            "cultivo" to cultivoSpinner.selectedItem.toString(),
            "maleza" to malezaSpinner.selectedItem.toString(),
            "cobertura" to coberturaSpinner.selectedItem.toString(),
            "tamano-de-maleza" to tamanoMalezaSpinner.selectedItem.toString(),
            "plagas" to plagasSpinner.selectedItem.toString(),
            "aplicacion-de-plaga" to aplicacionPlagaSpinner.selectedItem.toString(),
            "aplicacion-de-enfermedades" to aplicacionEnfermedadesSpinner.selectedItem.toString(),
            "umbral-de-plagas" to umbralPlagasEditText.text.toString(),
            "enfermedades" to enfermedadesEditText.text.toString(),
            "info-anexo" to infoAnexoEditText.text.toString() // Cambiado a "info-anexo" para seguir tu estructura
        )

        db.collection("recorridos")
            .add(recorridoData)
            .addOnSuccessListener {
                Toast.makeText(this, "Recorrido guardado exitosamente", Toast.LENGTH_SHORT).show()
                finish() // Cierra la actividad después de guardar
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar el recorrido: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadSpinnerFromArray(spinner: Spinner, arrayId: Int) {
        val adapter = ArrayAdapter.createFromResource(
            this,
            arrayId,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun obtenerLotePorId(idLote: String) {
        db.collection("lotes").document(idLote)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // Aquí puedes acceder a los campos del lote
                    val nombre = document.getString("nombre")
                    val productor = document.getString("productor")
                    // Puedes utilizar la información obtenida según sea necesario
                } else {
                    Log.d("RecorridosActivity", "No existe el documento con ID: $idLote")
                }
            }
            .addOnFailureListener { e ->
                Log.w("RecorridosActivity", "Error al obtener el lote: ", e)
            }
    }
}
