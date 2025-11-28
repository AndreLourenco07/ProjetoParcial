package com.example.projetoparcial

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projetoparcial.data.model.ItemDados
import com.example.projetoparcial.databinding.ActivityItemCategoryHeaderBinding
import com.example.projetoparcial.databinding.ActivityItemProdutosBinding

class ItensAdapter(
    private var listaItensGrouped: Map<String, List<ItemDados>> = emptyMap(),
    private val onCheckChanged: (ItemDados, Boolean) -> Unit,
    private val onItemClick: (ItemDados) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private val items = mutableListOf<Pair<String?, ItemDados?>>()

    inner class ItemViewHolder(val binding: ActivityItemProdutosBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class HeaderViewHolder(val binding: ActivityItemCategoryHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ActivityItemCategoryHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ActivityItemProdutosBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ItemViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val categoria = items[position].first
                holder.binding.textHeaderCategory.text = categoria ?: "Sem categoria"
            }
            is ItemViewHolder -> {
                val item = items[position].second
                if (item != null) {
                    holder.binding.apply {
                        // Nome do produto
                        tvNomeProduto.text = item.nome

                        // Subtítulo com quantidade, unidade e categoria
                        tvSubtitulo.text = "${item.quantidade} ${item.unidade} - ${item.categoria}"

                        // IMPORTANTE: Remover listener antes de setar o estado
                        cbConcluido.setOnCheckedChangeListener(null)

                        // Setar estado do checkbox
                        cbConcluido.isChecked = item.concluido

                        // Mudar fundo se concluído
                        if (item.concluido) {
                            root.setBackgroundColor(Color.parseColor("#E0E0E0"))
                        } else {
                            root.setBackgroundColor(Color.TRANSPARENT)
                        }

                        // Adicionar listener DEPOIS de setar o estado
                        cbConcluido.setOnCheckedChangeListener { _, isChecked ->
                            onCheckChanged(item, isChecked)
                        }

                        // Long click para deletar
                        root.setOnClickListener {
                            onItemClick(item)
                            true
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return if (items[position].first != null && items[position].second == null) {
            TYPE_HEADER
        } else {
            TYPE_ITEM
        }
    }

    fun atualizarLista(novaLista: Map<String, List<ItemDados>>) {
        listaItensGrouped = novaLista
        items.clear()

        // Ordenar categorias alfabeticamente
        val categoriasOrdenadas = novaLista.keys.sorted()

        for (categoria in categoriasOrdenadas) {
            // Adicionar header da categoria
            items.add(Pair(categoria, null))

            // Adicionar itens da categoria ordenados alfabeticamente
            val itensCategoria = novaLista[categoria] ?: emptyList()
            val itensOrdenados = itensCategoria.sortedBy { it.nome }

            for (item in itensOrdenados) {
                items.add(Pair(null, item))
            }
        }

        notifyDataSetChanged()
    }
}