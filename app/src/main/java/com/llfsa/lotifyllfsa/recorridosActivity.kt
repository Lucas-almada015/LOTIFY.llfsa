package com.llfsa.lotifyllfsa

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class recorridosActivity : AppCompatActivity() {


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

        // Obtener los arrays de recursos
        val cultivoArray = resources.getStringArray(R.array.cultivo_array)
        val malezaArray = resources.getStringArray(R.array.maleza_array)
        val coberturaArray = resources.getStringArray(R.array.cobertura_array)
        val tamanoMalezaArray = resources.getStringArray(R.array.tamano_maleza_array)
        val plagasArray = resources.getStringArray(R.array.plagas_array)
        val aplicacionPlagaArray = resources.getStringArray(R.array.aplicacion_plaga_array)
        val aplicacionEnfermedadesArray = resources.getStringArray(R.array.aplicacion_enfermedades_array)

        // Configurar adaptadores para cada Spinner
        val cultivoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cultivoArray)
        cultivoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cultivoSpinner.adapter = cultivoAdapter

        val malezaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, malezaArray)
        malezaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        malezaSpinner.adapter = malezaAdapter

        val coberturaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, coberturaArray)
        coberturaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        coberturaSpinner.adapter = coberturaAdapter

        val tamanoMalezaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tamanoMalezaArray)
        tamanoMalezaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tamanoMalezaSpinner.adapter = tamanoMalezaAdapter

        val plagasAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, plagasArray)
        plagasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        plagasSpinner.adapter = plagasAdapter

        val aplicacionPlagaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, aplicacionPlagaArray)
        aplicacionPlagaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        aplicacionPlagaSpinner.adapter = aplicacionPlagaAdapter

        val aplicacionEnfermedadesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, aplicacionEnfermedadesArray)
        aplicacionEnfermedadesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        aplicacionEnfermedadesSpinner.adapter = aplicacionEnfermedadesAdapter

        // Configurar el botón de guardar
        saveButton.setOnClickListener {
            // Aquí puedes agregar la lógica para guardar los datos

            // Manejar el clic en el botón "Volver"
            backButton.setOnClickListener {
                finish() // Cierra la actividad actual y vuelve a la actividad anterior
            }
        }
    }
}
