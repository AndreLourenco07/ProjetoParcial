package com.example.projetoparcial.data.repository

import com.example.projetoparcial.data.datasource.ItemMemoryDataSource
import com.example.projetoparcial.data.datasource.ItemRemoteDataSource
import com.example.projetoparcial.data.model.ItemDados

private const val CACHE_EXPIRATION_TIME_MILLIS = 30000 // 30 segundos

class ItemRepository {

    private val remote = ItemRemoteDataSource()
    private val memory = ItemMemoryDataSource()

    suspend fun salvarItem(idLista: String, item: ItemDados): Result<String> {
        return remote.salvarItem(idLista, item).onSuccess { id ->
            memory.addItem(idLista, item.copy(id = id))
        }
    }

    suspend fun lerItens(idLista: String, forceRefresh: Boolean = false): Result<List<ItemDados>> {
        // Verificar se há cache válido
        if (!forceRefresh && memory.getItens(idLista) != null) {
            val lastUpdate = memory.getLastUpdate(idLista) ?: 0
            val cacheValid = (lastUpdate + CACHE_EXPIRATION_TIME_MILLIS) > System.currentTimeMillis()

            if (cacheValid) {
                return Result.success(memory.getItens(idLista)!!)
            }
        }

        // Buscar do servidor
        return remote.lerItens(idLista).onSuccess { itens ->
            memory.saveItens(idLista, itens)
        }.recoverCatching { exception ->
            // Se falhar e houver cache, retornar o cache
            memory.getItens(idLista) ?: throw exception
        }
    }

    suspend fun atualizarItem(idLista: String, idItem: String, item: ItemDados): Result<Unit> {
        return remote.atualizarItem(idLista, idItem, item).onSuccess {
            memory.updateItem(idLista, item.copy(id = idItem))
        }
    }

    suspend fun atualizarStatus(idLista: String, idItem: String, concluido: Boolean): Result<Unit> {
        return remote.atualizarStatus(idLista, idItem, concluido).onSuccess {
            memory.getItens(idLista)?.find { it.id == idItem }?.let { item ->
                memory.updateItem(idLista, item.copy(concluido = concluido))
            }
        }
    }

    suspend fun removerItem(idLista: String, idItem: String): Result<Unit> {
        return remote.removerItem(idLista, idItem).onSuccess {
            memory.removeItem(idLista, idItem)
        }
    }

    fun limparCache(idLista: String) {
        memory.clear(idLista)
    }

    fun limparTodoCache() {
        memory.clearAll()
    }
}