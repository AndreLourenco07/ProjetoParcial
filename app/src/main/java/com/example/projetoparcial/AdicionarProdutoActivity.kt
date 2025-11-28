package com.example.projetoparcial

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.projetoparcial.data.model.ItemDados
import com.example.projetoparcial.databinding.ActivityAdicionarProdutoBinding

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
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.unidades)
        )

        binding.spinnerCategoria.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.categorias)
        )

        if (itemTitle.isNotEmpty()) {
            binding.etNomeItem.setText(itemTitle)
        }

        if (itemUnidade.isNotEmpty()) {
            val unidades = resources.getStringArray(R.array.unidades)
            val posicao = unidades.indexOf(itemUnidade)
            if (posicao >= 0) {
                binding.spinnerUnidade.setSelection(posicao)
            }
        }

        if (itemCategoria.isNotEmpty()) {
            val categorias = resources.getStringArray(R.array.categorias)
            val posicao = categorias.indexOf(itemCategoria)
            if (posicao >= 0) {
                binding.spinnerCategoria.setSelection(posicao)
            }
        }

        if (itemQuantidade > 0) {
            binding.etQuantidade.setText(itemQuantidade.toString())
        }

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

        if (itemId.isEmpty()){
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
        }else{
            BD().atualizarItemLista(
                idLista = idLista,
                idItem = itemId,
                item = item,
                onSucesso = {
                    Toast.makeText(this, "Produto atualizado!", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onErro = {
                    Toast.makeText(this, "Erro ao atualizar produto: $it", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}