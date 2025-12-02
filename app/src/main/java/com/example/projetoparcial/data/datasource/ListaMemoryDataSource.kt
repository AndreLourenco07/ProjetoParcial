package com.example.projetoparcial.data.datasource

import com.example.projetoparcial.data.model.ListaDados

class ListaMemoryDataSource {
    private var cachedListas: List<ListaDados>? = null
    private var lastUpdate: Long? = null

    fun saveListas(listas: List<ListaDados>) {
        cachedListas = listas
        lastUpdate = System.currentTimeMillis()
    }

    fun getListas(): List<ListaDados>? = cachedListas

    fun getLastUpdate(): Long? = lastUpdate

    fun addLista(lista: ListaDados) {
        cachedListas = cachedListas?.plus(lista) ?: listOf(lista)
        lastUpdate = System.currentTimeMillis()
    }

    fun updateLista(lista: ListaDados) {
        cachedListas = cachedListas?.map {
            if (it.id == lista.id) lista else it
        }
        lastUpdate = System.currentTimeMillis()
    }

    fun removeLista(listaId: String) {
        cachedListas = cachedListas?.filter { it.id != listaId }
        lastUpdate = System.currentTimeMillis()
    }

    fun clear() {
        cachedListas = null
        lastUpdate = null
    }
}