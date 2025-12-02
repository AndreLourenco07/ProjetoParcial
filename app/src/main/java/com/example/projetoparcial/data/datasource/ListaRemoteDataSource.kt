package com.example.projetoparcial.data.datasource

import android.net.Uri
import android.util.Log
import com.example.projetoparcial.data.model.ListaDados
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ListaRemoteDataSource {

    private val db = Firebase.firestore
    private val storage = FirebaseStorage.getInstance().reference

    suspend fun salvarImagem(imageUri: Uri): Result<String> {
        return try {
            val nomeArquivo = "imagens/${System.currentTimeMillis()}.jpg"
            val imagemRef = storage.child(nomeArquivo)

            imagemRef.putFile(imageUri).await()
            val url = imagemRef.downloadUrl.await()

            Result.success(url.toString())
        } catch (e: Exception) {
            Log.e("ListaRemoteDataSource", "Erro ao salvar imagem", e)
            Result.failure(e)
        }
    }

    suspend fun salvarLista(lista: ListaDados): Result<String> {
        return try {
            val dados = hashMapOf(
                "nome" to lista.nome,
                "descricao" to lista.descricao,
                "imageUrl" to lista.imageUrl
            )

            val docRef = db.collection("listas")
                .add(dados)
                .await()

            Log.d("ListaRemoteDataSource", "Lista salva com ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("ListaRemoteDataSource", "Erro ao salvar lista", e)
            Result.failure(e)
        }
    }

    suspend fun lerListas(): Result<List<ListaDados>> {
        return try {
            val snapshot = db.collection("listas")
                .get()
                .await()

            val listas = snapshot.documents.map { doc ->
                ListaDados(
                    id = doc.id,
                    nome = doc.getString("nome") ?: "",
                    descricao = doc.getString("descricao") ?: "",
                    imageUrl = doc.getString("imageUrl")
                )
            }

            Result.success(listas)
        } catch (e: Exception) {
            Log.e("ListaRemoteDataSource", "Erro ao ler listas", e)
            Result.failure(e)
        }
    }

    suspend fun atualizarLista(id: String, lista: ListaDados): Result<Unit> {
        return try {
            val dados = hashMapOf(
                "nome" to lista.nome,
                "descricao" to lista.descricao,
                "imageUrl" to lista.imageUrl
            )

            db.collection("listas")
                .document(id)
                .update(dados as Map<String, Any>)
                .await()

            Log.d("ListaRemoteDataSource", "Lista atualizada com ID: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ListaRemoteDataSource", "Erro ao atualizar lista", e)
            Result.failure(e)
        }
    }

    // 🆕 EXCLUSÃO EM BATCH (Lista + Itens)
    suspend fun removerLista(idLista: String): Result<Unit> {
        return try {
            val listaRef = db.collection("listas").document(idLista)

            // Buscar todos os itens da lista
            val itensSnapshot = listaRef.collection("itens").get().await()

            // Criar batch para deletar tudo junto
            val batch = db.batch()

            // Adicionar todos os itens ao batch
            for (itemDoc in itensSnapshot.documents) {
                batch.delete(itemDoc.reference)
            }

            // Adicionar a lista ao batch
            batch.delete(listaRef)

            // Executar batch
            batch.commit().await()

            Log.d("ListaRemoteDataSource", "Lista e itens removidos: $idLista")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ListaRemoteDataSource", "Erro ao remover lista", e)
            Result.failure(e)
        }
    }
}