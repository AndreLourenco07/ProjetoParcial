package com.example.projetoparcial

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetoparcial.data.model.ItemDados
import com.example.projetoparcial.databinding.ActivityItensBinding

class ItensActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItensBinding
    private lateinit var adapter: ItensAdapter
    private val bd = BD()

    private var listId = ""
    private var listTitle = ""
    private var imageUri = ""

    // Lista completa de itens (sem filtro)
    private var todosOsItens: List<ItemDados> = emptyList()

    private val addOrEditItemLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                carregarItens()
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

        if (listId.isNotEmpty()) {
            binding.textTitulo.text = listTitle
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        configurarRecyclerView()
        configurarBusca()
        carregarItens()

        binding.btnAddProduto.setOnClickListener {
            val intent = Intent(this, AdicionarProdutoActivity::class.java)
            intent.putExtra("LIST_ID", listId)
            startActivity(intent)
        }

        binding.imgButtonEditarLista.setOnClickListener {
            editarLista(listId, listTitle, imageUri)
        }
    }

    private fun configurarRecyclerView() {
        adapter = ItensAdapter(
            listaItensGrouped = emptyMap(),
            onCheckChanged = { item, isChecked ->
                atualizarStatusItem(item, isChecked)
            },
            onItemClick = { item ->
                showListOptionsDialog(item)
            }
        )
        binding.recyclerViewItens.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewItens.adapter = adapter
    }

    private fun configurarBusca() {
        binding.etBuscarItem.addTextChangedListener { termo ->
            val textoBusca = termo.toString().trim().lowercase()

            if (textoBusca.isEmpty()) {
                // Se o campo de busca estiver vazio, mostra todos os itens
                atualizarAdapter(todosOsItens)
            } else {
                // Filtra os itens localmente
                val itensFiltrados = todosOsItens.filter { item ->
                    item.nome.lowercase().contains(textoBusca) ||
                            item.categoria?.lowercase()?.contains(textoBusca) == true ||
                            item.unidade.lowercase().contains(textoBusca)
                }
                atualizarAdapter(itensFiltrados)
            }
        }
    }

    private fun carregarItens() {
        if (listId.isEmpty()) return

        bd.lerItensLista(listId) { itens ->
            todosOsItens = itens
            atualizarAdapter(itens)
        }
    }

    private fun atualizarAdapter(itens: List<ItemDados>) {
        // Ordenar: não concluídos primeiro, depois por categoria alfabética e nome
        val itensOrdenados = itens
            .sortedWith(compareBy({ it.concluido }, { it.categoria ?: "" }, { it.nome }))

        // Agrupar por categoria
        val itensAgrupados = itensOrdenados.groupBy { it.categoria ?: "Sem categoria" }

        adapter.atualizarLista(itensAgrupados)
    }

    private fun atualizarStatusItem(item: ItemDados, concluido: Boolean) {
        bd.atualizarStatusItem(
            idLista = listId,
            idItem = item.id,
            concluido = concluido,
            onSucesso = {
                carregarItens()
            },
            onErro = { erro ->
                Toast.makeText(this, "Erro ao atualizar: $erro", Toast.LENGTH_SHORT).show()
            }
        )
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

        startActivity(intent)
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
                deletarItem(item)
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun deletarItem(item: ItemDados) {
        bd.removerItemDaLista(
            idLista = listId,
            idItem = item.id,
            onSucesso = {
                Toast.makeText(this, "Item deletado!", Toast.LENGTH_SHORT).show()
                carregarItens()
            },
            onErro = { erro ->
                Toast.makeText(this, "Erro ao deletar: $erro", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onResume() {
        super.onResume()
        carregarItens()
    }
}