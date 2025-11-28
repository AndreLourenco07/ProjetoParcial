package com.example.projetoparcial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projetoparcial.data.model.ListaDados

class ViewLista : ViewModel() {

    private val _lists = MutableLiveData<List<ListaDados>>(listOf())
    val lists: LiveData<List<ListaDados>>
        get() = _lists

    // LiveData para mensagens (erros, avisos)
    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?>
        get() = _toastMessage

    // 👉 usado quando ler do Firestore
    fun setListas(listas: List<ListaDados>) {
        _lists.value = listas.sortedBy { it.nome.lowercase() }
    }

    fun addList(list: ListaDados) {
        val currentLists = _lists.value ?: emptyList()

        if (currentLists.any { it.nome.equals(list.nome, ignoreCase = true) }) {
            _toastMessage.value = "Uma lista com o nome '${list.nome}' já existe."
            return
        }

        val updatedList = currentLists.toMutableList().apply {
            add(list)
            sortBy { it.nome.lowercase() }
        }
        _lists.value = updatedList
    }

    fun removeList(listToRemove: ListaDados) {
        val updatedList = (_lists.value ?: emptyList()).toMutableList().apply {
            removeAll { it.id == listToRemove.id }
        }
        _lists.value = updatedList
    }

    fun updateList(listToUpdate: ListaDados) {
        val currentLists = _lists.value ?: emptyList()

        if (currentLists.any {
                it.nome.equals(listToUpdate.nome, ignoreCase = true) &&
                        it.id != listToUpdate.id
            }) {
            _toastMessage.value =
                "Não foi possível salvar. O nome '${listToUpdate.nome}' já está em uso."
            return
        }

        val updatedList = currentLists.toMutableList().apply {
            val index = indexOfFirst { it.id == listToUpdate.id }
            if (index != -1) {
                set(index, listToUpdate)
                sortBy { it.nome.lowercase() }
            }
        }
        _lists.value = updatedList
    }

    fun onToastShown() {
        _toastMessage.value = null
    }
}
