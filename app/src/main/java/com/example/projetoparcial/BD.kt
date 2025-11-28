package com.example.projetoparcial

import android.net.Uri
import android.util.Log
import com.example.projetoparcial.data.model.ItemDados
import com.example.projetoparcial.data.model.ListaDados
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
        val listaRef = db.collection("listas").document(idLista)

        listaRef.collection("itens")
            .get()
            .addOnSuccessListener { snapshot ->

                val batch = db.batch()

                for (documentoItem in snapshot.documents) {
                    batch.delete(documentoItem.reference)
                }

                batch.delete(listaRef)

                batch.commit()
                    .addOnSuccessListener {
                        Log.d("BD", "Lista e itens removidos com sucesso. ID: $idLista")
                        onSucesso()
                    }
                    .addOnFailureListener { e ->
                        Log.e("BD", "Erro ao executar o batch de exclusão", e)
                        onErro(e.message ?: "Erro ao deletar itens e lista")
                    }

            }
            .addOnFailureListener { e ->
                Log.e("BD", "Erro ao buscar itens para deletar", e)
                onErro("Não foi possível acessar os itens da lista para exclusão.")
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

    // -----------------------------
    // 6) SALVAR ITEM DENTRO DA LISTA (ATUALIZADO)
    // -----------------------------
    fun salvarItemNaLista(
        idLista: String,
        item: ItemDados,
        onSucesso: () -> Unit,
        onErro: (String) -> Unit
    ) {
        val dados = hashMapOf(
            "nome" to item.nome,
            "quantidade" to item.quantidade,
            "unidade" to item.unidade,
            "categoria" to item.categoria,
            "concluido" to item.concluido  // ← NOVO
        )

        db.collection("listas")
            .document(idLista)
            .collection("itens")
            .add(dados)
            .addOnSuccessListener {
                Log.d("BD", "Item salvo com ID: ${it.id}")
                onSucesso()
            }
            .addOnFailureListener { e ->
                Log.e("BD", "Erro ao salvar item", e)
                onErro(e.message ?: "Erro desconhecido")
            }
    }

    // -----------------------------
    // 7) LER ITENS DE UMA LISTA (ATUALIZADO)
    // -----------------------------
    fun lerItensLista(
        idLista: String,
        onRetorno: (List<ItemDados>) -> Unit
    ) {
        db.collection("listas")
            .document(idLista)
            .collection("itens")
            .get()
            .addOnSuccessListener { resultado ->
                val itensConvertidos = resultado.documents.map { doc ->
                    ItemDados(
                        id = doc.id,
                        nome = doc.getString("nome") ?: "",
                        quantidade = doc.getDouble("quantidade") ?: 1.0,
                        unidade = doc.getString("unidade") ?: "UN",
                        categoria = doc.getString("categoria") ?: "Outros",
                        concluido = doc.getBoolean("concluido") ?: false  // ← NOVO
                    )
                }
                onRetorno(itensConvertidos)
            }
            .addOnFailureListener { erro ->
                Log.e("BD", "Erro ao ler itens", erro)
                onRetorno(emptyList())
            }
    }

    // -----------------------------
    // 8) ATUALIZAR STATUS CONCLUÍDO DO ITEM
    // -----------------------------
    fun atualizarStatusItem(
        idLista: String,
        idItem: String,
        concluido: Boolean,
        onSucesso: () -> Unit,
        onErro: (String) -> Unit
    ) {
        db.collection("listas")
            .document(idLista)
            .collection("itens")
            .document(idItem)
            .update("concluido", concluido)
            .addOnSuccessListener {
                Log.d("BD", "Status atualizado: $concluido")
                onSucesso()
            }
            .addOnFailureListener { e ->
                Log.e("BD", "Erro ao atualizar status", e)
                onErro(e.message ?: "Erro desconhecido")
            }
    }

    // -----------------------------
    // 9) REMOVER ITEM DA LISTA
    // -----------------------------
    fun removerItemDaLista(
        idLista: String,
        idItem: String,
        onSucesso: () -> Unit,
        onErro: (String) -> Unit
    ) {
        db.collection("listas")
            .document(idLista)
            .collection("itens")
            .document(idItem)
            .delete()
            .addOnSuccessListener {
                Log.d("BD", "Item removido com ID: $idItem")
                onSucesso()
            }
            .addOnFailureListener { e ->
                Log.e("BD", "Erro ao remover item", e)
                onErro(e.message ?: "Erro desconhecido")
            }
    }

    // -----------------------------
    // 5) ATUALIZAR LISTA NO FIRESTORE
    // -----------------------------
    fun atualizarItemLista(
        idLista : String,
        idItem : String,
        item: ItemDados,
        onSucesso: () -> Unit,
        onErro: (String) -> Unit
    ) {
        val dados = hashMapOf(
            "nome" to item.nome,
            "quantidade" to item.quantidade,
            "unidade" to item.unidade,
            "categoria" to item.categoria,
            "concluido" to item.concluido  // ← NOVO
        )

        db.collection("listas")
            .document(idLista)
            .collection("itens")
            .document(idItem)
            .update(dados as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("BD", "Item atualizada com ID: ${idItem} da lista ${idLista}")
                onSucesso()
            }
            .addOnFailureListener { e ->
                Log.e("BD", "Erro ao atualizar lista", e)
                onErro(e.message ?: "Erro desconhecido")
            }
    }
}