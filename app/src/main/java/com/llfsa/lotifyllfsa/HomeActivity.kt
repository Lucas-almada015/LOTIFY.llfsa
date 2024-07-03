package com.llfsa.lotifyllfsa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

enum class ProviderType {
    BASIC
}

class HomeActivity : AppCompatActivity() {
    private lateinit var emailTextView: TextView
    private lateinit var providerTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var perfilButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        logoutButton = findViewById(R.id.logoutButton)
        perfilButton = findViewById(R.id.button3)

        // Setup
        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        val provider: String? = bundle?.getString("provider")
        setup(email ?: "", provider ?: "")
    }

    private fun setup(email: String, provider: String) {
        title = "Home"
        emailTextView.text = email
        providerTextView.text = provider

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }

        perfilButton.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("provider", provider)
            startActivity(intent)
        }
    }
}


