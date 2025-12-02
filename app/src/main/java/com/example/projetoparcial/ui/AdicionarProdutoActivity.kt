package com.example.projetoparcial.ui

import android.R
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.projetoparcial.BD
import com.example.projetoparcial.data.model.ItemDados
import com.example.projetoparcial.databinding.ActivityAdicionarProdutoBinding
import com.google.android.material.snackbar.Snackbar

class AdicionarProdutoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdicionarProdutoBinding
    private var idLista = ""

    private var itemId = ""
    private var itemTitle = ""
    private var itemCategoria = ""
    private var itemQuantidade = 0.0
    private var itemUnidade = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        idLista        = intent.getStringExtra("LIST_ID") ?: ""
        itemId         = intent.getStringExtra("ITEM_ID") ?: ""
        itemTitle      = intent.getStringExtra("ITEM_TITLE") ?: ""
        itemUnidade    = intent.getStringExtra("ITEM_UNIDADE") ?: ""
        itemCategoria  = intent.getStringExtra("ITEM_CATEGORIA") ?: ""
        itemQuantidade = intent.getDoubleExtra("ITEM_QUANTIDADE", 0.0)

        binding = ActivityAdicionarProdutoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.spinnerUnidade.adapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_item,
            resources.getStringArray(com.example.projetoparcial.R.array.unidades)
        )

        binding.spinnerCategoria.adapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_item,
            resources.getStringArray(com.example.projetoparcial.R.array.categorias)
        )

        if (itemTitle.isNotEmpty()) {
            binding.edtNomeItem.setText(itemTitle)
        }

        if (itemUnidade.isNotEmpty()) {
            val unidades = resources.getStringArray(com.example.projetoparcial.R.array.unidades)
            val posicao = unidades.indexOf(itemUnidade)
            if (posicao >= 0) {
                binding.spinnerUnidade.setSelection(posicao)
            }
        }

        if (itemCategoria.isNotEmpty()) {
            val categorias = resources.getStringArray(com.example.projetoparcial.R.array.categorias)
            val posicao = categorias.indexOf(itemCategoria)
            if (posicao >= 0) {
                binding.spinnerCategoria.setSelection(posicao)
            }
        }

        if (itemQuantidade > 0) {
            binding.edtQuantidade.setText(itemQuantidade.toString())
        }

        binding.btnAdicionar.setOnClickListener {
            val Quantidade = binding.edtQuantidade.text.toString().trim()
            val NomeItem   = binding.edtNomeItem.text.toString().trim()

            when {
                Quantidade.isEmpty() || NomeItem.isEmpty() -> {
                    Snackbar.make(binding.root, "Preencha todos os campos!", Snackbar.LENGTH_SHORT).show()
                }else -> {
                    salvarProduto()
                }
            }
        }
    }

    private fun salvarProduto() {
        val nomeProduto = binding.edtNomeItem.text.toString().trim()
        val qtdProduto = binding.edtQuantidade.text.toString().toDoubleOrNull() ?: 1.0

        // Pegar unidade e categoria dos componentes do XML
        val unidade = binding.spinnerUnidade.selectedItem.toString()
        val categoria = binding.spinnerCategoria.selectedItem.toString()

        if (nomeProduto.isEmpty()) {
            Snackbar.make(binding.root, "Digite um nome do produto", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (idLista.isEmpty()) {
            Snackbar.make(binding.root, "Erro: lista não identificada!", Snackbar.LENGTH_SHORT).show()
            return
        }

        val item = ItemDados(
            nome = nomeProduto,
            quantidade = qtdProduto,
            unidade = unidade,
            categoria = categoria
        )

        if (itemId.isEmpty()){
            BD().salvarItemNaLista(
                idLista = idLista,
                item = item,
                onSucesso = {
                    Snackbar.make(binding.root, "Produto salvo!", Snackbar.LENGTH_SHORT).show()
                    finish()
                },
                onErro = {
                    Snackbar.make(binding.root, "Erro ao salvar produto: $it", Snackbar.LENGTH_SHORT).show()
                }
            )
        }else{
            BD().atualizarItemLista(
                idLista = idLista,
                idItem = itemId,
                item = item,
                onSucesso = {
                    Snackbar.make(binding.root, "Produto atualizado!", Snackbar.LENGTH_SHORT).show()
                    finish()
                },
                onErro = {
                    Snackbar.make(binding.root, "Erro ao atualizar produto: $it", Snackbar.LENGTH_SHORT).show()
                }
            )
        }
    }
}