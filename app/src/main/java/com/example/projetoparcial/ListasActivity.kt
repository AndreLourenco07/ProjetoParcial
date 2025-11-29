package com.example.projetoparcial

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.projetoparcial.data.model.ListaDados
import com.example.projetoparcial.databinding.ActivityListasBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ListasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListasBinding
    private val viewModel: ViewLista by viewModels()
    private lateinit var adapterLista: AdapterLista
    private lateinit var firebaseAuth: FirebaseAuth

    // Lista completa de listas (sem filtro)
    private var todasAsListas: List<ListaDados> = emptyList()

    private val addOrEditListLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                carregarListasDoFirestore()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        setupRecyclerView()
        configurarBusca()

        viewModel.lists.observe(this) { lists ->
            adapterLista.updateLists(lists)
        }

        viewModel.toastMessage.observe(this) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.onToastShown()
            }
        }

        binding.btnAddLista.setOnClickListener {
            val intent = Intent(this, AdicionarListaActivity::class.java)
            addOrEditListLauncher.launch(intent)
        }

        binding.imgBtnVoltar.setOnClickListener {
            mostrarDialogoLogout()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        carregarListasDoFirestore()
    }

    override fun onResume() {
        super.onResume()
        carregarListasDoFirestore()
    }

    private fun carregarListasDoFirestore() {
        BD().lerListas { listas ->
            todasAsListas = listas
            viewModel.setListas(listas)
        }
    }

    private fun configurarBusca() {
        binding.edtBuscarItem.addTextChangedListener { termo ->
            val textoBusca = termo.toString().trim().lowercase()

            if (textoBusca.isEmpty()) {
                // Se o campo de busca estiver vazio, mostra todas as listas
                atualizarAdapter(todasAsListas)
            } else {
                // Filtra as listas localmente
                val listasFiltradas = todasAsListas.filter { lista ->
                    lista.nome.lowercase().contains(textoBusca)
                }
                atualizarAdapter(listasFiltradas)
            }
        }
    }

    private fun atualizarAdapter(listas: List<ListaDados>) {
        adapterLista.updateLists(listas)
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

    private fun showListOptionsDialog(list: ListaDados) {
        val opcoes = arrayOf("Editar Lista", "Remover Lista")

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
            .setTitle("Remover Lista")
            .setMessage("Tem certeza de que deseja remover a lista '${list.nome}'?")
            .setPositiveButton("Remover") { _, _ ->
                BD().removerLista(
                    idLista = list.id,
                    onSucesso = {
                        viewModel.removeList(list)
                        Toast.makeText(this, "Lista removida com sucesso", Toast.LENGTH_SHORT).show()
                    },
                    onErro = { mensagem ->
                        Toast.makeText(this, "Erro: $mensagem", Toast.LENGTH_SHORT).show()
                    }
                )
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
        firebaseAuth.signOut()

        Snackbar.make(binding.root, "Logout realizado com sucesso", Snackbar.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}