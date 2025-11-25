package com.example.projetoparcial

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projetoparcial.databinding.ActivityItensBinding

class ItensActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItensBinding

    private var listId = ""
    private var listTitle = ""
    private var imageUri = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //Pega as informações que são passadas ao chamar a Activity para depois usar
        //como verificacao se é inclusão ou alteração
        listId = intent.getStringExtra("LIST_ID") ?: ""
        listTitle = intent.getStringExtra("LIST_TITLE") ?: ""
        imageUri = intent.getStringExtra("IMAGE_URI") ?: ""

        binding = ActivityItensBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Se tiver informação preenche o campo do nome da lista
        if (!listId.isEmpty()) {
            binding.textTitulo.setText(listTitle)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnAddProduto.setOnClickListener {
            val intent = Intent(this, AdicionarProdutoActivity::class.java)
            startActivity(intent)
        }
    }
}