package com.example.projetoparcial.data.datasource

import com.example.projetoparcial.data.model.ItemDados

class ItemMemoryDataSource {
    private val cachedItens = mutableMapOf<String, CachedItems>()

    data class CachedItems(
        val itens: List<ItemDados>,
        val lastUpdate: Long
    )

    fun saveItens(idLista: String, itens: List<ItemDados>) {
        cachedItens[idLista] = CachedItems(
            itens = itens,
            lastUpdate = System.currentTimeMillis()
        )
    }

    fun getItens(idLista: String): List<ItemDados>? {
        return cachedItens[idLista]?.itens
    }

    fun getLastUpdate(idLista: String): Long? {
        return cachedItens[idLista]?.lastUpdate
    }

    fun addItem(idLista: String, item: ItemDados) {
        val cached = cachedItens[idLista]
        if (cached != null) {
            cachedItens[idLista] = CachedItems(
                itens = cached.itens + item,
                lastUpdate = System.currentTimeMillis()
            )
        }
    }

    fun updateItem(idLista: String, item: ItemDados) {
        val cached = cachedItens[idLista]
        if (cached != null) {
            cachedItens[idLista] = CachedItems(
                itens = cached.itens.map { if (it.id == item.id) item else it },
                lastUpdate = System.currentTimeMillis()
            )
        }
    }

    fun removeItem(idLista: String, itemId: String) {
        val cached = cachedItens[idLista]
        if (cached != null) {
            cachedItens[idLista] = CachedItems(
                itens = cached.itens.filter { it.id != itemId },
                lastUpdate = System.currentTimeMillis()
            )
        }
    }

    fun clear(idLista: String) {
        cachedItens.remove(idLista)
    }

    fun clearAll() {
        cachedItens.clear()
    }
}