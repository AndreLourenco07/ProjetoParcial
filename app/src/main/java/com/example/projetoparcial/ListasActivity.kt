package com.example.projetoparcial

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projetoparcial.databinding.ActivityListasBinding

class ListasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ViewBinding para o layout de cadastro
        binding = ActivityListasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnAddLista.setOnClickListener {
            val intent = Intent(this, AdicionarListaActivity::class.java)
            startActivity(intent)
        }

        binding.imgButtonVoltar.setOnClickListener {
            val intent = Intent(this, ItensActivity::class.java)
            startActivity(intent)
        }
    }
}