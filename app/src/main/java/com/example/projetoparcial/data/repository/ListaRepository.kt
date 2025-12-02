package com.example.projetoparcial.data.repository

import android.net.Uri
import com.example.projetoparcial.data.datasource.ListaMemoryDataSource
import com.example.projetoparcial.data.datasource.ListaRemoteDataSource
import com.example.projetoparcial.data.model.ListaDados

private const val CACHE_EXPIRATION_TIME_MILLIS = 30000 // 30 segundos

class ListaRepository {

    private val remote = ListaRemoteDataSource()
    private val memory = ListaMemoryDataSource()

    suspend fun salvarImagem(imageUri: Uri): Result<String> {
        return remote.salvarImagem(imageUri)
    }

    suspend fun salvarLista(lista: ListaDados): Result<String> {
        return remote.salvarLista(lista).onSuccess { id ->
            memory.addLista(lista.copy(id = id))
        }
    }

    suspend fun lerListas(forceRefresh: Boolean = false): Result<List<ListaDados>> {
        // Verificar se há cache válido
        if (!forceRefresh && memory.getListas() != null) {
            val lastUpdate = memory.getLastUpdate() ?: 0
            val cacheValid = (lastUpdate + CACHE_EXPIRATION_TIME_MILLIS) > System.currentTimeMillis()

            if (cacheValid) {
                return Result.success(memory.getListas()!!.sortedBy { it.nome.lowercase() })
            }
        }

        // Buscar do servidor
        return remote.lerListas().onSuccess { listas ->
            memory.saveListas(listas)
        }.map { listas ->
            listas.sortedBy { it.nome.lowercase() }
        }.recoverCatching { exception ->
            // Se falhar e houver cache, retornar o cache
            memory.getListas()?.sortedBy { it.nome.lowercase() }
                ?: throw exception
        }
    }

    suspend fun atualizarLista(id: String, lista: ListaDados): Result<Unit> {
        return remote.atualizarLista(id, lista).onSuccess {
            memory.updateLista(lista.copy(id = id))
        }
    }

    suspend fun removerLista(idLista: String): Result<Unit> {
        return remote.removerLista(idLista).onSuccess {
            memory.removeLista(idLista)
        }
    }

    fun limparCache() {
        memory.clear()
    }
}