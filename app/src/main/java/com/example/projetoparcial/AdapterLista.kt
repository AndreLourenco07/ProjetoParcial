package com.example.projetoparcial

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projetoparcial.data.model.ListaDados
import com.example.projetoparcial.databinding.ActivityItemListasBinding

class AdapterLista(
    private var lists: List<ListaDados>,
    private val onItemClick: (ListaDados) -> Unit,
    private val onLongItemClick: (ListaDados) -> Unit
) : RecyclerView.Adapter<AdapterLista.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ActivityItemListasBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(lists[position])
    }

    override fun getItemCount(): Int = lists.size

    fun updateLists(newLists: List<ListaDados>) {
        lists = newLists
        notifyDataSetChanged()
    }

    inner class ListViewHolder(
        private val binding: ActivityItemListasBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(list: ListaDados) {
            // título
            binding.textViewListTitle.text = list.nome

            // imagem
            if (!list.imageUrl.isNullOrBlank()) {
                binding.imageViewList.setImageURI(Uri.parse(list.imageUrl))
            } else {
                binding.imageViewList.setImageResource(R.drawable.ic_placeholder_image)
            }

            // clique curto
            binding.root.setOnClickListener {
                onItemClick(list)
            }

            // clique longo
            binding.root.setOnLongClickListener {
                onLongItemClick(list)
                true
            }
        }
    }
}
