package com.example.projetoparcial.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetoparcial.adapter.ItensAdapter
import com.example.projetoparcial.data.model.ItemDados
import com.example.projetoparcial.databinding.ActivityItensBinding
import com.example.projetoparcial.ui.viewmodel.ItensViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ItensActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItensBinding
    private val viewModel: ItensViewModel by viewModels()
    private lateinit var adapter: ItensAdapter

    private var listId = ""
    private var listTitle = ""
    private var imageUri = ""

    private val addOrEditItemLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.carregarItens(forceRefresh = true)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        listId = intent.getStringExtra("LIST_ID") ?: ""
        listTitle = intent.getStringExtra("LIST_TITLE") ?: ""
        imageUri = intent.getStringExtra("IMAGE_URI") ?: ""

        binding = ActivityItensBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        setupBusca()
        setupClickListeners()
        setupWindowInsets()
        observeUiState()

        // Configurar ViewModel
        viewModel.setListId(listId)
    }

    override fun onResume() {
        super.onResume()
        viewModel.carregarItens(forceRefresh = true)
    }

    private fun setupUI() {
        if (listId.isNotEmpty()) {
            binding.txtTitulo.text = listTitle
        }
    }

    private fun setupRecyclerView() {
        adapter = ItensAdapter(
            listaItensGrouped = emptyMap(),
            onCheckChanged = { item, isChecked ->
                viewModel.atualizarStatusItem(item, isChecked)
            },
            onItemClick = { item ->
                showListOptionsDialog(item)
            }
        )
        binding.recyclerViewItens.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewItens.adapter = adapter
    }

    // 🆕 CONFIGURAR BUSCA
    private fun setupBusca() {
        binding.edtBuscarItem.addTextChangedListener { texto ->
            val termo = texto.toString().trim()
            viewModel.buscarItens(termo)
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Atualizar lista (com agrupamento)
                    adapter.atualizarLista(state.itensAgrupados)

                    // Atualizar botão
                    binding.btnAddProduto.isEnabled = !state.isLoading

                    // Mensagens de erro
                    state.errorMessage?.let { message ->
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                        viewModel.limparMensagens()
                    }

                    // Mensagens de sucesso
                    state.successMessage?.let { message ->
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                        viewModel.limparMensagens()
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAddProduto.setOnClickListener {
            val intent = Intent(this, AdicionarProdutoActivity::class.java)
            intent.putExtra("LIST_ID", listId)
            addOrEditItemLauncher.launch(intent)
        }

        binding.imgBtnEditarLista.setOnClickListener {
            editarLista(listId, listTitle, imageUri)
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showListOptionsDialog(item: ItemDados) {
        val opcoes = arrayOf("Editar Item", "Remover Item")

        android.app.AlertDialog.Builder(this)
            .setTitle(item.nome)
            .setItems(opcoes) { _, which ->
                when (which) {
                    0 -> editarItem(item)
                    1 -> mostrarDialogDeletar(item)
                }
            }
            .show()
    }

    private fun editarLista(listId: String, listTitle: String, imageUri: String) {
        val intent = Intent(this, AdicionarListaActivity::class.java).apply {
            putExtra("LIST_ID", listId)
            putExtra("LIST_TITLE", listTitle)
            putExtra("IMAGE_URI", imageUri)
        }
        addOrEditItemLauncher.launch(intent)
    }

    private fun editarItem(item: ItemDados) {
        val intent = Intent(this, AdicionarProdutoActivity::class.java).apply {
            putExtra("LIST_ID", listId)
            putExtra("ITEM_ID", item.id)
            putExtra("ITEM_TITLE", item.nome)
            putExtra("ITEM_UNIDADE", item.unidade)
            putExtra("ITEM_CATEGORIA", item.categoria)
            putExtra("ITEM_QUANTIDADE", item.quantidade)
        }
        addOrEditItemLauncher.launch(intent)
    }

    private fun mostrarDialogDeletar(item: ItemDados) {
        AlertDialog.Builder(this)
            .setTitle("Deletar Item")
            .setMessage("Deseja realmente deletar ${item.nome}?")
            .setPositiveButton("Sim") { _, _ ->
                viewModel.removerItem(item)
            }
            .setNegativeButton("Não", null)
            .show()
    }
}