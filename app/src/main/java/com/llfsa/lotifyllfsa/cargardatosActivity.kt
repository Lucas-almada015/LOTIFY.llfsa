package com.llfsa.lotifyllfsa

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.view.View

class cargardatosActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var lotNumberInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var sizeInput: EditText
    private lateinit var imageView: ImageView
    private lateinit var cargarButton: Button
    private lateinit var cancelButton: Button
    private lateinit var checkboxUseCurrentLocation: CheckBox
    private lateinit var checkboxManualLocation: CheckBox
    private lateinit var manualLatitudeInput: EditText
    private lateinit var manualLongitudeInput: EditText
    private lateinit var db: FirebaseFirestore
    private var selectedImageUri: Uri? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imageView.setImageURI(selectedImageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cargardatos)

        // Inicializar vistas
        nameInput = findViewById(R.id.nameInput)
        lotNumberInput = findViewById(R.id.lotNumberInput)
        locationInput = findViewById(R.id.locationInput)
        sizeInput = findViewById(R.id.sizeInput)
        imageView = findViewById(R.id.imageView)
        cargarButton = findViewById(R.id.cargarButton)
        cancelButton = findViewById(R.id.cancelButton)
        checkboxUseCurrentLocation = findViewById(R.id.checkbox_use_current_location)
        checkboxManualLocation = findViewById(R.id.checkbox_manual_location)
        manualLatitudeInput = findViewById(R.id.manual_latitude_input)
        manualLongitudeInput = findViewById(R.id.manual_longitude_input)

        // Inicializar Firestore y LocationServices
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar listener para seleccionar imagen
        imageView.setOnClickListener {
            seleccionarImagen()
        }

        // Configurar lógica para mostrar u ocultar campos de ubicación
        checkboxUseCurrentLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkboxManualLocation.isChecked = false
                manualLatitudeInput.visibility = View.GONE
                manualLongitudeInput.visibility = View.GONE
                solicitarPermisosUbicacion()
            }
        }

        checkboxManualLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkboxUseCurrentLocation.isChecked = false
                manualLatitudeInput.visibility = View.VISIBLE
                manualLongitudeInput.visibility = View.VISIBLE
            } else {
                manualLatitudeInput.visibility = View.GONE
                manualLongitudeInput.visibility = View.GONE
            }
        }

        // Configurar el listener del botón de carga
        cargarButton.setOnClickListener {
            val nombre = nameInput.text.toString().trim()
            val numeroLote = lotNumberInput.text.toString().trim()
            val ubicacion = locationInput.text.toString().trim()
            val tamaño = sizeInput.text.toString().trim()

            if (nombre.isNotEmpty() && numeroLote.isNotEmpty() && ubicacion.isNotEmpty() && tamaño.isNotEmpty()) {
                if (checkboxUseCurrentLocation.isChecked) {
                    obtenerUbicacionActual { location ->
                        if (location != null) {
                            guardarDatos(nombre, numeroLote, ubicacion, tamaño, location.latitude.toString(), location.longitude.toString())
                        } else {
                            Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else if (checkboxManualLocation.isChecked) {
                    val latitud = manualLatitudeInput.text.toString().trim()
                    val longitud = manualLongitudeInput.text.toString().trim()

                    if (latitud.isNotEmpty() && longitud.isNotEmpty()) {
                        guardarDatos(nombre, numeroLote, ubicacion, tamaño, latitud, longitud)
                    } else {
                        Toast.makeText(this, "Por favor completa la latitud y longitud", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Por favor selecciona una opción de ubicación", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar el listener del botón de cancelar
        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun seleccionarImagen() {
        pickImageLauncher.launch("image/*")
    }

    private fun solicitarPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            obtenerUbicacionActual { location ->
                if (location != null) {
                    manualLatitudeInput.setText(location.latitude.toString())
                    manualLongitudeInput.setText(location.longitude.toString())
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual { location ->
                if (location != null) {
                    manualLatitudeInput.setText(location.latitude.toString())
                    manualLongitudeInput.setText(location.longitude.toString())
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Permisos de ubicación denegados", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerUbicacionActual(callback: (Location?) -> Unit) {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                callback(location)
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "No se pudo obtener la ubicación actual: ${e.message}", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    private fun guardarDatos(nombre: String, numeroLote: String, ubicacion: String, tamaño: String, latitud: String, longitud: String) {
        val lote = hashMapOf(
            "nombre" to nombre,
            "numeroLote" to numeroLote,
            "ubicacion" to ubicacion,
            "tamaño" to tamaño,
            "latitud" to latitud,
            "longitud" to longitud
        )

        selectedImageUri?.let { uri ->
            lote["imageUrl"] = uri.toString()
        }

        db.collection("lotes")
            .add(lote)
            .addOnSuccessListener {
                Toast.makeText(this, "Lote cargado exitosamente", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar el lote: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarCampos() {
        nameInput.text.clear()
        lotNumberInput.text.clear()
        locationInput.text.clear()
        sizeInput.text.clear()
        manualLatitudeInput.text.clear()
        manualLongitudeInput.text.clear()
        imageView.setImageResource(android.R.color.transparent)
        selectedImageUri = null
        checkboxUseCurrentLocation.isChecked = false
        checkboxManualLocation.isChecked = false
    }
}

