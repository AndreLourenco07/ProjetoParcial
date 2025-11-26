package com.example.projetoparcial

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.projetoparcial.databinding.ActivityAdicionarProdutoBinding

class AdicionarProdutoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdicionarProdutoBinding
    private var idLista = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAdicionarProdutoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // PEGAR ID DA LISTA QUE RECEBE O PRODUTO
        idLista = intent.getStringExtra("LIST_ID") ?: ""

        binding.btnAdicionar.setOnClickListener {
            salvarProduto()
        }
    }

    private fun salvarProduto() {
        val nomeProduto = binding.etNomeItem.text.toString().trim()
        val qtdProduto = binding.etQuantidade.text.toString().toDoubleOrNull() ?: 1.0

        // Pegar unidade e categoria dos componentes do XML
        val unidade = binding.spinnerUnidade.selectedItem.toString()
        val categoria = binding.spinnerCategoria.selectedItem.toString()

        if (nomeProduto.isEmpty()) {
            Toast.makeText(this, "Digite um nome do produto", Toast.LENGTH_SHORT).show()
            return
        }

        if (idLista.isEmpty()) {
            Toast.makeText(this, "Erro: lista não identificada!", Toast.LENGTH_SHORT).show()
            return
        }

        val item = ItemDados(
            nome = nomeProduto,
            quantidade = qtdProduto,
            unidade = unidade,
            categoria = categoria
        )

        BD().salvarItemNaLista(
            idLista = idLista,
            item = item,
            onSucesso = {
                Toast.makeText(this, "Produto salvo!", Toast.LENGTH_SHORT).show()
                finish()
            },
            onErro = {
                Toast.makeText(this, "Erro ao salvar produto: $it", Toast.LENGTH_SHORT).show()
            }
        )
    }
}