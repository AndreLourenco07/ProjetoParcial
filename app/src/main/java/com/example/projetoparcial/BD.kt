package com.example.projetoparcial

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class BD {
    private val db = Firebase.firestore
    private val storage = FirebaseStorage.getInstance().reference

    // -----------------------------
    // 1) SALVAR IMAGEM NO STORAGE
    // -----------------------------
    fun salvarImagem(
        imageUri: Uri,
        onSucesso: (String) -> Unit,
        onErro: (String) -> Unit
    ) {
        val nomeArquivo = "imagens/${System.currentTimeMillis()}.jpg"
        val imagemRef = storage.child(nomeArquivo)

        imagemRef.putFile(imageUri)
            .addOnSuccessListener {
                imagemRef.downloadUrl.addOnSuccessListener { url ->
                    onSucesso(url.toString())
                }
            }
            .addOnFailureListener { e ->
                onErro(e.message ?: "Erro ao salvar imagem")
            }
    }

    // -----------------------------
    // 2) SALVAR LISTA NO FIRESTORE
    // -----------------------------
    fun salvarLista(
        lista: ListaDados,
        onSucesso: () -> Unit,
        onErro: (String) -> Unit
    ) {
        val dados = hashMapOf(
            "nome" to lista.nome,
            "descricao" to lista.descricao,
            "imageUrl" to lista.imageUrl
        )

        db.collection("listas")
            .add(dados)
            .addOnSuccessListener {
                Log.d("BD", "Lista salva com ID: ${it.id}")
                onSucesso()
            }
            .addOnFailureListener { e ->
                Log.e("BD", "Erro ao salvar lista", e)
                onErro(e.message ?: "Erro desconhecido")
            }
    }

    // -----------------------------
    // 3) LER LISTAS DO FIRESTORE
    // -----------------------------
    fun lerListas(onRetorno: (List<ListaDados>) -> Unit) {
        db.collection("listas")
            .get()
            .addOnSuccessListener { resultado ->
                val listasConvertidas = resultado.documents.map { doc ->
                    ListaDados(
                        id = doc.id,
                        nome = doc.getString("nome") ?: "",
                        descricao = doc.getString("descricao") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )
                }
                onRetorno(listasConvertidas)
            }
            .addOnFailureListener { erro ->
                Log.e("BD", "Erro ao ler listas", erro)
                onRetorno(emptyList())
            }
    }

    // -----------------------------
    // 4) REMOVER LISTA DO FIRESTORE
    // -----------------------------
    fun removerLista(
        idLista: String,
        onSucesso: () -> Unit,
        onErro: (String) -> Unit
    ) {
        db.collection("listas")
            .document(idLista)
            .delete()
            .addOnSuccessListener {
                Log.d("BD", "Lista removida com ID: $idLista")
                onSucesso()
            }
            .addOnFailureListener { e ->
                Log.e("BD", "Erro ao remover lista", e)
                onErro(e.message ?: "Erro desconhecido")
            }
    }

    // -----------------------------
    // 5) ATUALIZAR LISTA NO FIRESTORE
    // -----------------------------
    fun atualizarLista(
        id : String,
        lista: ListaDados,
        onSucesso: () -> Unit,
        onErro: (String) -> Unit
    ) {
        val dados = hashMapOf(
            "nome" to lista.nome,
            "descricao" to lista.descricao,
            "imageUrl" to lista.imageUrl
        )

        db.collection("listas")
            .document(id)
            .update(dados as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("BD", "Lista atualizada com ID: ${id}")
                onSucesso()
            }
            .addOnFailureListener { e ->
                Log.e("BD", "Erro ao atualizar lista", e)
                onErro(e.message ?: "Erro desconhecido")
            }
    }
}