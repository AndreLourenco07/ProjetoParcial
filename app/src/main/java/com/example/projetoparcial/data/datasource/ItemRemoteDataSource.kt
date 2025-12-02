package com.example.projetoparcial.data.datasource

import android.util.Log
import com.example.projetoparcial.data.model.ItemDados
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ItemRemoteDataSource {

    private val db = Firebase.firestore

    suspend fun salvarItem(idLista: String, item: ItemDados): Result<String> {
        return try {
            val dados = hashMapOf(
                "nome" to item.nome,
                "quantidade" to item.quantidade,
                "unidade" to item.unidade,
                "categoria" to item.categoria,
                "concluido" to item.concluido
            )

            val docRef = db.collection("listas")
                .document(idLista)
                .collection("itens")
                .add(dados)
                .await()

            Log.d("ItemRemoteDataSource", "Item salvo com ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("ItemRemoteDataSource", "Erro ao salvar item", e)
            Result.failure(e)
        }
    }

    suspend fun lerItens(idLista: String): Result<List<ItemDados>> {
        return try {
            val snapshot = db.collection("listas")
                .document(idLista)
                .collection("itens")
                .get()
                .await()

            val itens = snapshot.documents.map { doc ->
                ItemDados(
                    id = doc.id,
                    nome = doc.getString("nome") ?: "",
                    quantidade = doc.getDouble("quantidade") ?: 1.0,
                    unidade = doc.getString("unidade") ?: "UN",
                    categoria = doc.getString("categoria") ?: "Outros",
                    concluido = doc.getBoolean("concluido") ?: false
                )
            }

            Result.success(itens)
        } catch (e: Exception) {
            Log.e("ItemRemoteDataSource", "Erro ao ler itens", e)
            Result.failure(e)
        }
    }

    suspend fun atualizarItem(idLista: String, idItem: String, item: ItemDados): Result<Unit> {
        return try {
            val dados = hashMapOf(
                "nome" to item.nome,
                "quantidade" to item.quantidade,
                "unidade" to item.unidade,
                "categoria" to item.categoria,
                "concluido" to item.concluido
            )

            db.collection("listas")
                .document(idLista)
                .collection("itens")
                .document(idItem)
                .update(dados as Map<String, Any>)
                .await()

            Log.d("ItemRemoteDataSource", "Item atualizado: $idItem")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ItemRemoteDataSource", "Erro ao atualizar item", e)
            Result.failure(e)
        }
    }

    suspend fun atualizarStatus(idLista: String, idItem: String, concluido: Boolean): Result<Unit> {
        return try {
            db.collection("listas")
                .document(idLista)
                .collection("itens")
                .document(idItem)
                .update("concluido", concluido)
                .await()

            Log.d("ItemRemoteDataSource", "Status atualizado: $concluido")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ItemRemoteDataSource", "Erro ao atualizar status", e)
            Result.failure(e)
        }
    }

    suspend fun removerItem(idLista: String, idItem: String): Result<Unit> {
        return try {
            db.collection("listas")
                .document(idLista)
                .collection("itens")
                .document(idItem)
                .delete()
                .await()

            Log.d("ItemRemoteDataSource", "Item removido: $idItem")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ItemRemoteDataSource", "Erro ao remover item", e)
            Result.failure(e)
        }
    }
}