package com.example.projetoparcial.ui

import android.R
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.projetoparcial.databinding.ActivityAdicionarProdutoBinding
import com.example.projetoparcial.ui.viewmodel.AdicionarProdutoViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AdicionarProdutoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdicionarProdutoBinding
    private val viewModel: AdicionarProdutoViewModel by viewModels()

    private var idLista = ""
    private var itemId = ""
    private var itemTitle = ""
    private var itemCategoria = ""
    private var itemQuantidade = 0.0
    private var itemUnidade = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Pega os dados da intent
        idLista        = intent.getStringExtra("LIST_ID") ?: ""
        itemId         = intent.getStringExtra("ITEM_ID") ?: ""
        itemTitle      = intent.getStringExtra("ITEM_TITLE") ?: ""
        itemUnidade    = intent.getStringExtra("ITEM_UNIDADE") ?: ""
        itemCategoria  = intent.getStringExtra("ITEM_CATEGORIA") ?: ""
        itemQuantidade = intent.getDoubleExtra("ITEM_QUANTIDADE", 0.0)

        binding = ActivityAdicionarProdutoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        preencherCampos()
        setupClickListeners()
        observeUiState()
    }

    private fun setupSpinners() {
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
    }

    private fun preencherCampos() {
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
    }

    private fun setupClickListeners() {
        binding.btnAdicionar.setOnClickListener {
            val nome = binding.edtNomeItem.text.toString().trim()
            val quantidadeStr = binding.edtQuantidade.text.toString().trim()

            when {
                nome.isEmpty() || quantidadeStr.isEmpty() -> {
                    Snackbar.make(binding.root, "Preencha todos os campos!", Snackbar.LENGTH_SHORT).show()
                }
                else -> {
                    val quantidade = quantidadeStr.toDoubleOrNull() ?: 1.0
                    val unidade = binding.spinnerUnidade.selectedItem.toString()
                    val categoria = binding.spinnerCategoria.selectedItem.toString()

                    viewModel.salvarProduto(
                        idLista = idLista,
                        itemId = itemId,
                        nome = nome,
                        quantidade = quantidade,
                        unidade = unidade,
                        categoria = categoria
                    )
                }
            }
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Controla o loading
                    if (state.isLoading) {
                        mostrarLoading()
                    } else {
                        esconderLoading()
                    }

                    // Sucesso - volta para tela anterior
                    if (state.isSaved) {
                        val mensagem = if (itemId.isEmpty()) "Produto salvo!" else "Produto atualizado!"
                        Snackbar.make(binding.root, mensagem, Snackbar.LENGTH_SHORT).show()
                        viewModel.resetSavedState()
                        setResult(RESULT_OK)
                        finish()
                    }

                    // Mostra erro se houver
                    state.errorMessage?.let { message ->
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                        viewModel.limparMensagens()
                    }
                }
            }
        }
    }

    private fun mostrarLoading() {
        binding.btnAdicionar.isEnabled = false
        binding.btnAdicionar.text = ""
        binding.progressBarProduto.visibility = View.VISIBLE
    }

    private fun esconderLoading() {
        binding.btnAdicionar.isEnabled = true
        binding.btnAdicionar.text = if (itemId.isEmpty()) "Adicionar" else "Salvar"
        binding.progressBarProduto.visibility = View.GONE
    }
}