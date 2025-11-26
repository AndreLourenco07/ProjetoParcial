package com.example.projetoparcial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetoparcial.databinding.ActivityItensBinding

class ItensActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItensBinding
    private lateinit var adapter: ItensAdapter
    private val bd = BD()

    private var listId = ""
    private var listTitle = ""
    private var imageUri = ""

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

        // Configurar RecyclerView
        configurarRecyclerView()

        // Carregar itens
        carregarItens()

        binding.btnAddProduto.setOnClickListener {
            val intent = Intent(this, AdicionarProdutoActivity::class.java)
            intent.putExtra("LIST_ID", listId)
            startActivity(intent)
        }
    }

    private fun configurarRecyclerView() {
        adapter = ItensAdapter(
            listaItens = emptyList(),
            onCheckChanged = { item, isChecked ->
                atualizarStatusItem(item, isChecked)
            },
            onDeleteClick = { item ->
                mostrarDialogDeletar(item)
            }
        )
        binding.recyclerViewItens.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewItens.adapter = adapter
    }

    private fun carregarItens() {
        if (listId.isEmpty()) return

        bd.lerItensLista(listId) { itens ->
            // Ordenar: não concluídos primeiro, concluídos no final
            val itensOrdenados = itens.sortedBy { it.concluido }
            adapter.atualizarLista(itensOrdenados)
        }
    }

    private fun atualizarStatusItem(item: ItemDados, concluido: Boolean) {
        bd.atualizarStatusItem(
            idLista = listId,
            idItem = item.id,
            concluido = concluido,
            onSucesso = {
                carregarItens() // Recarrega e reordena automaticamente
            },
            onErro = { erro ->
                Toast.makeText(this, "Erro ao atualizar: $erro", Toast.LENGTH_SHORT).show()
            }
        )
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