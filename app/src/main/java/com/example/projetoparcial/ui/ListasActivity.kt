package com.example.projetoparcial.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.projetoparcial.adapter.AdapterLista
import com.example.projetoparcial.data.model.ListaDados
import com.example.projetoparcial.data.repository.LoginRepository
import com.example.projetoparcial.databinding.ActivityListasBinding
import com.example.projetoparcial.ui.viewmodel.ListasViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ListasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListasBinding
    private val viewModel: ListasViewModel by viewModels()
    private lateinit var adapterLista: AdapterLista
    private val loginRepository = LoginRepository()

    private val addOrEditListLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.carregarListas(forceRefresh = true)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupBusca()
        setupClickListeners()
        setupWindowInsets()
        observeUiState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.carregarListas(forceRefresh = true)
    }

    private fun setupRecyclerView() {
        adapterLista = AdapterLista(
            emptyList(),
            onItemClick = { list ->
                val intent = Intent(this, ItensActivity::class.java).apply {
                    putExtra("LIST_ID", list.id)
                    putExtra("LIST_TITLE", list.nome)
                    putExtra("IMAGE_URI", list.imageUrl)
                }
                startActivity(intent)
            },
            onLongItemClick = { list ->
                showListOptionsDialog(list)
            }
        )
        binding.recyclerViewListas.adapter = adapterLista
    }

    private fun setupBusca() {
        binding.edtBuscarItem.addTextChangedListener { texto ->
            val termo = texto.toString().trim()
            viewModel.buscarListas(termo)
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapterLista.updateLists(state.listasFiltradas)

                    binding.btnAddLista.isEnabled = !state.isLoading

                    state.errorMessage?.let { message ->
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                        viewModel.limparMensagens()
                    }

                    state.successMessage?.let { message ->
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                        viewModel.limparMensagens()
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAddLista.setOnClickListener {
            val intent = Intent(this, AdicionarListaActivity::class.java)
            addOrEditListLauncher.launch(intent)
        }

        binding.imgBtnVoltar.setOnClickListener {
            mostrarDialogoLogout()
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showListOptionsDialog(list: ListaDados) {
        val opcoes = arrayOf("Editar Lista", "Excluir Lista")

        AlertDialog.Builder(this)
            .setTitle(list.nome)
            .setItems(opcoes) { _, which ->
                when (which) {
                    0 -> editarLista(list)
                    1 -> showDeleteConfirmationDialog(list)
                }
            }
            .show()
    }

    private fun editarLista(list: ListaDados) {
        val intent = Intent(this, AdicionarListaActivity::class.java).apply {
            putExtra("LIST_ID", list.id)
            putExtra("LIST_TITLE", list.nome)
            putExtra("IMAGE_URI", list.imageUrl)
        }
        addOrEditListLauncher.launch(intent)
    }

    private fun showDeleteConfirmationDialog(list: ListaDados) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Lista")
            .setMessage("Tem certeza de que deseja excluir a lista '${list.nome}'?")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.removerLista(list)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Deseja realmente sair da sua conta?")
            .setPositiveButton("Sair") { _, _ ->
                realizarLogout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun realizarLogout() {
        loginRepository.logout()

        Snackbar.make(binding.root, "Logout realizado com sucesso", Snackbar.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}