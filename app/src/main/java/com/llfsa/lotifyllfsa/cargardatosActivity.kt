package com.llfsa.lotifyllfsa

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class cargardatosActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var lotNumberInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var sizeInput: EditText
    private lateinit var imageView: ImageView
    private lateinit var cargarButton: Button
    private lateinit var cancelButton: Button
    private lateinit var db: FirebaseFirestore
    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cargardatos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        nameInput = findViewById(R.id.nameInput)
        lotNumberInput = findViewById(R.id.lotNumberInput)
        locationInput = findViewById(R.id.locationInput)
        sizeInput = findViewById(R.id.sizeInput)
        imageView = findViewById(R.id.imageView)
        cargarButton = findViewById(R.id.cargarButton)
        cancelButton = findViewById(R.id.cancelButton)

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance()

        // Configurar listener del botón para seleccionar imagen
        imageView.setOnClickListener {
            seleccionarImagen()
        }

        // Configurar el listener del botón de carga
        cargarButton.setOnClickListener {
            val nombre = nameInput.text.toString().trim()
            val numeroLote = lotNumberInput.text.toString().trim()
            val ubicacion = locationInput.text.toString().trim()
            val tamaño = sizeInput.text.toString().trim()

            if (nombre.isNotEmpty() && numeroLote.isNotEmpty() && ubicacion.isNotEmpty() && tamaño.isNotEmpty()) {
                guardarDatos(nombre, numeroLote, ubicacion, tamaño)
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar el listener del botón de cancelar
        cancelButton.setOnClickListener {
            // Implementar la lógica de cancelación si es necesario
            finish()
        }
    }

    private fun seleccionarImagen() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Seleccione una imagen"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            imageView.setImageURI(selectedImageUri)
        }
    }

    private fun guardarDatos(nombre: String, numeroLote: String, ubicacion: String, tamaño: String) {
        // Crear un mapa con los datos del lote
        val lote = hashMapOf(
            "nombre" to nombre,
            "numeroLote" to numeroLote,
            "ubicacion" to ubicacion,
            "tamaño" to tamaño
        )

        // Agregar la URL de la imagen si se seleccionó una
        selectedImageUri?.let { uri ->
            lote["imageUrl"] = uri.toString()
        }

        // Agregar los datos del lote a Cloud Firestore
        db.collection("lotes")
            .add(lote)
            .addOnSuccessListener { documentReference ->
                // Datos guardados exitosamente
                Toast.makeText(this, "Lote cargado exitosamente", Toast.LENGTH_SHORT).show()
                // Limpiar campos después de éxito si es necesario
                limpiarCampos()
            }
            .addOnFailureListener { e ->
                // Manejo de errores
                Toast.makeText(this, "Error al cargar el lote: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarCampos() {
        nameInput.text.clear()
        lotNumberInput.text.clear()
        locationInput.text.clear()
        sizeInput.text.clear()
        imageView.setImageResource(android.R.color.transparent) // Limpiar imagen seleccionada si es necesario
        selectedImageUri = null
    }
}

