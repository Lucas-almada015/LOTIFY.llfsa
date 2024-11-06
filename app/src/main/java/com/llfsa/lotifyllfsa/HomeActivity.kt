package com.llfsa.lotifyllfsa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import android.widget.ImageButton

enum class ProviderType { BASIC }

class HomeActivity : AppCompatActivity() {

    private lateinit var emailTextView: TextView
    private lateinit var providerTextView: TextView
    private lateinit var logoutButton: ImageButton
    private lateinit var perfilButton: ImageButton
    private lateinit var cargarButton: Button
    private lateinit var updateButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoteAdapter
    private lateinit var db: FirebaseFirestore

    companion object {
        const val PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Solicitar permisos si es necesario
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PERMISSION_REQUEST_CODE)
        } else {
            // Permiso concedido, puedes acceder a la ubicación
            initializeApp()
        }
    }

    private fun initializeApp() {
        // Inicializar vistas
        emailTextView = findViewById(R.id.emaillTextView)
        providerTextView = findViewById(R.id.providerrTextView)
        logoutButton = findViewById(R.id.imageButton2)
        perfilButton = findViewById(R.id.imageButton5)
        cargarButton = findViewById(R.id.button2)
        updateButton = findViewById(R.id.imageButton)
        recyclerView = findViewById(R.id.recyclerView)

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance()

        // Setup
        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        val provider: String? = bundle?.getString("provider")
        setup(email ?: "", provider ?: "")

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LoteAdapter(listOf())
        recyclerView.adapter = adapter

        // Cargar datos de lotes desde Firestore
        loadLotesData()

        updateButton.setOnClickListener {
            loadLotesData() // Llamar a la función de cargar datos
        }
    }

    private fun setup(email: String, provider: String) {
        title = "Home"
        emailTextView.text = email
        providerTextView.text = provider

        logoutButton.setOnClickListener {
            // Crear un AlertDialog para confirmar el cierre de sesión
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí") { dialog, _ ->
                    // Cerrar sesión si el usuario confirma
                    FirebaseAuth.getInstance().signOut()
                    dialog.dismiss() // Cerrar el diálogo
                    onBackPressed() // Volver a la pantalla anterior
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss() // Cerrar el diálogo si el usuario cancela
                }

            // Mostrar el AlertDialog
            builder.create().show()
        }

        perfilButton.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java).apply {
                putExtra("email", email)
                putExtra("provider", provider)
            }
            startActivity(intent)
        }

        cargarButton.setOnClickListener {
            val intent = Intent(this, cargardatosActivity::class.java).apply {
                putExtra("email", email)
                putExtra("provider", provider)
            }
            startActivity(intent)
        }
    }

    private fun loadLotesData() {
        // Obtener el UID del usuario actual
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("lotes")
            .whereEqualTo("userId", userId) // Filtrar por userId
            .get()
            .addOnSuccessListener { result ->
                val lotesList = mutableListOf<Lote>()
                for (document in result) {
                    val imageUrl = document.getString("imageUrl")
                    val nombreLote = document.getString("nombre")
                    val numeroLote = document.getString("numeroLote")
                    val tamaño = document.getString("tamaño")
                    val ubicacion = document.getString("ubicacion")
                    val idLote = document.id

                    val lote = Lote(idLote,imageUrl, nombreLote, numeroLote, tamaño, ubicacion)
                    lotesList.add(lote)
                }
                adapter.updateLotes(lotesList)
            }
            .addOnFailureListener { exception ->
                // Manejar errores
                exception.printStackTrace()
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, puedes acceder a los medios
                initializeApp()
            } else {
                // Permiso denegado, muestra un mensaje al usuario
                Toast.makeText(this, "El permiso de medios es necesario para mostrar imágenes.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

data class Lote(
    val id: String,
    val imageUrl: String?,
    val nombre: String?,
    val numeroLote: String?,
    val tamaño: String?,
    val ubicacion: String?
)

class LoteAdapter(private var lotes: List<Lote>) : RecyclerView.Adapter<LoteAdapter.LoteViewHolder>() {

    inner class LoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val loteImageView: ImageView = view.findViewById(R.id.loteImageView)
        val textViewLoteName: TextView = view.findViewById(R.id.textViewLoteName)

        init {
            // Hacer que el elemento sea clicable
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedLote = lotes[position]
                    val intent = Intent(view.context, LoteInfoActivity::class.java).apply {
                        putExtra("IDlote", selectedLote.id) // Pasa el ID del lote como extra
                    }
                    view.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lote, parent, false)
        return LoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoteViewHolder, position: Int) {
        val lote = lotes[position]

        // Cargar imagen con Glide
        Glide.with(holder.itemView.context)
            .load(lote.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.demo4) // Asegúrate de tener una imagen de error
            .diskCacheStrategy(DiskCacheStrategy.NONE) // Evita el uso de caché
            .skipMemoryCache(true) // No usa caché en memoria
            .into(holder.loteImageView)

        holder.textViewLoteName.text = lote.nombre
    }

    override fun getItemCount(): Int = lotes.size

    fun updateLotes(newLotes: List<Lote>) {
        lotes = newLotes
        notifyDataSetChanged()
    }
}
