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
import com.example.projetoparcial.databinding.ActivityListasBinding

class ListasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListasBinding
    private val viewModel: ViewLista by viewModels()
    private lateinit var adapterLista: AdapterLista

    private val addOrEditListLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Recarrega as listas do Firestore após adicionar/editar
                carregarListasDoFirestore()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2) RecyclerView
        setupRecyclerView()

        // 3) Observa as listas para atualizar a tela
        viewModel.lists.observe(this) { lists ->
            adapterLista.updateLists(lists)
        }

        // 4) Observa mensagens de erro
        viewModel.toastMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.onToastShown()
            }
        }

        // Navegação
        binding.btnAddLista.setOnClickListener {
            val intent = Intent(this, AdicionarListaActivity::class.java)
            addOrEditListLauncher.launch(intent)
        }

        binding.imgButtonVoltar.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        // Carrega as listas toda vez que a Activity volta ao primeiro plano
        carregarListasDoFirestore()
    }

    private fun carregarListasDoFirestore() {
        BD().lerListas { listas ->
            viewModel.setListas(listas)
        }
    }

    private fun setupRecyclerView() {
        adapterLista = AdapterLista(
            emptyList(),
            onItemClick = { list ->
                // Clique simples: vai para ItensActivity
                val intent = Intent(this, ItensActivity::class.java).apply {
                    putExtra("LIST_ID", list.id)
                    putExtra("LIST_TITLE", list.nome)
                    putExtra("IMAGE_URI", list.imageUrl)
                }
                startActivity(intent)
            },
            onLongItemClick = { list ->
                // Clique longo: abre o menu de opções
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
                // Remove do Firestore primeiro
                BD().removerLista(
                    idLista = list.id,
                    onSucesso = {
                        // Remove da ViewModel (interface)
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
}