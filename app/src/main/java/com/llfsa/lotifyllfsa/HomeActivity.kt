package com.llfsa.lotifyllfsa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.FileDescriptor
import android.widget.ImageButton
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import android.graphics.drawable.Drawable
import android.system.Os
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions


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

        // Make the status bar transparent
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // Make the navigation bar transparent
        window.navigationBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        // Request permissions if needed
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission granted, initialize app
            initializeApp()
        }
    }

    private fun initializeApp() {
        // Initialize views
        emailTextView = findViewById(R.id.emaillTextView)
        providerTextView = findViewById(R.id.providerrTextView)
        logoutButton = findViewById(R.id.imageButton2)
        perfilButton = findViewById(R.id.imageButton5)
        cargarButton = findViewById(R.id.button2)
        updateButton = findViewById(R.id.imageButton)
        recyclerView = findViewById(R.id.recyclerView)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Setup
        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        val provider: String? = bundle?.getString("provider")
        setup(email ?: "", provider ?: "")

        // Configure RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LoteAdapter(listOf())
        recyclerView.adapter = adapter

        // Load lote data from Firestore
        loadLotesData()

        // Update button listener
        updateButton.setOnClickListener {
            loadLotesData() // Call the function to load data
        }
    }

    private fun setup(email: String, provider: String) {
        title = "Home"
        emailTextView.text = email
        providerTextView.text = provider

        // Logout button click listener
        logoutButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí") { dialog, _ ->
                    FirebaseAuth.getInstance().signOut()
                    dialog.dismiss()
                    onBackPressed() // Go back to the previous screen
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            builder.create().show()
        }

        // Perfil button click listener
        perfilButton.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java).apply {
                putExtra("email", email)
                putExtra("provider", provider)
            }
            startActivity(intent)
        }

        // Cargar button click listener
        cargarButton.setOnClickListener {
            val intent = Intent(this, cargardatosActivity::class.java).apply {
                putExtra("email", email)
                putExtra("provider", provider)
            }
            startActivity(intent)
        }
    }

    private fun loadLotesData() {
        // Get current user's UID
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        db.collection("lotes")
            .whereEqualTo("userId", userId) // Filter by userId
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
                    var parcelFileDescriptor: ParcelFileDescriptor? = null

                    // Check if imageUrl is a content URI
                    if (imageUrl != null && imageUrl.startsWith("content://")) {
                        val contentResolver = contentResolver
                        parcelFileDescriptor = contentResolver.openFileDescriptor(
                            Uri.parse(imageUrl),
                            "r"
                        )
                    } else {
                        Log.e("HomeActivity", "imageUrl nula o inválida: $imageUrl")
                    }

                    val lote = Lote(
                        idLote,
                        imageUrl,
                        parcelFileDescriptor,
                        nombreLote,
                        numeroLote,
                        tamaño,
                        ubicacion
                    )
                    lotesList.add(lote)
                }
                adapter.updateLotes(lotesList)
            }
            .addOnFailureListener { exception ->
                // Handle errors
                exception.printStackTrace()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize app
                initializeApp()
            } else {
                // Permission denied, show message to user
                Toast.makeText(
                    this,
                    "El permiso de medios es necesario para mostrar imágenes.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    data class Lote(
        val id: String,
        val imageUrl: String?,
        val fileDescriptor: ParcelFileDescriptor?,
        val nombre: String?,
        val numeroLote: String?,
        val tamaño: String?,
        val ubicacion: String?
    )
        class LoteAdapter(private var lotes: List<Lote>) :
            RecyclerView.Adapter<LoteAdapter.LoteViewHolder>() {

            inner class LoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                val loteImageView: ImageView = view.findViewById(R.id.loteImageView)
                val textViewLoteName: TextView = view.findViewById(R.id.textViewLoteName)

                init {
                    // Hacer que el item sea clickable
                    view.setOnClickListener {
                        val position = bindingAdapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val selectedLote = lotes[position]
                            val intent = Intent(view.context, LoteInfoActivity::class.java).apply {
                                putExtra("IDlote", selectedLote.id)
                                putExtra("imageUrl", selectedLote.imageUrl)
                            }
                            view.context.startActivity(intent)
                        }
                    }
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoteViewHolder {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_lote, parent, false)
                return LoteViewHolder(view)
            }

            override fun onBindViewHolder(holder: LoteViewHolder, position: Int) {
                val lote = lotes[position]

                // Cargar imagen con Glide
                if (lote.fileDescriptor != null) {
                    try {
                        // Verificar si el ParcelFileDescriptor es válido
                        val parcelFileDescriptor = lote.fileDescriptor
                        if (parcelFileDescriptor != null) {
                            Glide.with(holder.itemView.context)
                                .load(parcelFileDescriptor.fileDescriptor) // Cargar imagen desde ParcelFileDescriptor
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        // Cerrar el ParcelFileDescriptor si la carga falla
                                        try {
                                            parcelFileDescriptor.close() // Cerramos el ParcelFileDescriptor
                                        } catch (e: Exception) {
                                            Log.e(
                                                "LoteAdapter",
                                                "Error al cerrar ParcelFileDescriptor",
                                                e
                                            )
                                        }
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        // Cerrar el ParcelFileDescriptor si la carga tiene éxito
                                        try {
                                            parcelFileDescriptor.close() // Cerramos el ParcelFileDescriptor
                                        } catch (e: Exception) {
                                            Log.e(
                                                "LoteAdapter",
                                                "Error al cerrar ParcelFileDescriptor",
                                                e
                                            )
                                        }
                                        return false
                                    }
                                })
                                .into(holder.loteImageView)
                        } else {
                            Log.e("LoteAdapter", "ParcelFileDescriptor inválido")
                        }
                    } catch (e: Exception) {
                        Log.e("LoteAdapter", "Error al cargar imagen desde ParcelFileDescriptor", e)
                    }
                } else {
                    // Si no hay ParcelFileDescriptor, se carga la imagen desde la URL
                    Glide.with(holder.itemView.context)
                        .load(lote.imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.loteImageView)
                }
            }

            override fun onViewRecycled(holder: LoteViewHolder) {
                super.onViewRecycled(holder)

                val position = holder.absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val lote = lotes[position]
                    try {
                        lote.fileDescriptor?.let {
                            // Asegúrate de cerrar el ParcelFileDescriptor cuando el ViewHolder se recicla
                            ParcelFileDescriptor.dup(it.fileDescriptor).close()
                        }
                    } catch (e: Exception) {
                        Log.e("LoteAdapter", "Error al cerrar ParcelFileDescriptor", e)
                    }
                }
            }

            override fun getItemCount(): Int = lotes.size

            fun updateLotes(newLotes: List<Lote>) {
                lotes = newLotes
                notifyDataSetChanged()
            }
        }
    }



