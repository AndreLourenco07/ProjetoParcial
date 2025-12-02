package com.example.projetoparcial.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetoparcial.data.model.ListaDados
import com.example.projetoparcial.data.repository.ListaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ListasUiState(
    val listas: List<ListaDados> = emptyList(),
    val listasFiltradas: List<ListaDados> = emptyList(),
    val termoBusca: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ListasViewModel : ViewModel() {

    private val repository = ListaRepository()

    private val _uiState = MutableStateFlow(ListasUiState())
    val uiState: StateFlow<ListasUiState> = _uiState.asStateFlow()

    init {
        carregarListas()
    }

    fun carregarListas(forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true) }
            }

            repository.lerListas(forceRefresh)
                .onSuccess { listas ->
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                listas = listas,
                                listasFiltradas = filtrarListas(listas, it.termoBusca),
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                }
                .onFailure { exception ->
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Erro ao carregar listas: ${exception.message}"
                            )
                        }
                    }
                }
        }
    }

    // 🆕 FILTRO DE BUSCA
    fun buscarListas(termo: String) {
        _uiState.update {
            it.copy(
                termoBusca = termo,
                listasFiltradas = filtrarListas(it.listas, termo)
            )
        }
    }

    private fun filtrarListas(listas: List<ListaDados>, termo: String): List<ListaDados> {
        if (termo.isEmpty()) return listas

        val termoLower = termo.lowercase()
        return listas.filter { lista ->
            lista.nome.lowercase().contains(termoLower)
        }
    }

    fun removerLista(lista: ListaDados) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true) }
            }

            repository.removerLista(lista.id)
                .onSuccess {
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Lista removida com sucesso"
                            )
                        }
                    }
                    carregarListas(forceRefresh = true)
                }
                .onFailure { exception ->
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Erro ao remover lista: ${exception.message}"
                            )
                        }
                    }
                }
        }
    }

    fun limparMensagens() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }
}