package com.example.projetoparcial.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projetoparcial.data.model.ItemDados
import com.example.projetoparcial.data.repository.ItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ItensUiState(
    val itens: List<ItemDados> = emptyList(),
    val itensAgrupados: Map<String, List<ItemDados>> = emptyMap(),
    val termoBusca: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ItensViewModel : ViewModel() {

    private val repository = ItemRepository()

    private val _uiState = MutableStateFlow(ItensUiState())
    val uiState: StateFlow<ItensUiState> = _uiState.asStateFlow()

    private var currentListId: String = ""

    fun setListId(listId: String) {
        currentListId = listId
        carregarItens()
    }

    fun carregarItens(forceRefresh: Boolean = false) {
        if (currentListId.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true) }
            }

            repository.lerItens(currentListId, forceRefresh)
                .onSuccess { itens ->
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                itens = itens,
                                itensAgrupados = agruparEOrdenarItens(itens, it.termoBusca),
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
                                errorMessage = "Erro ao carregar itens: ${exception.message}"
                            )
                        }
                    }
                }
        }
    }

    fun buscarItens(termo: String) {
        _uiState.update {
            it.copy(
                termoBusca = termo,
                itensAgrupados = agruparEOrdenarItens(it.itens, termo)
            )
        }
    }

    private fun agruparEOrdenarItens(
        itens: List<ItemDados>,
        termo: String
    ): Map<String, List<ItemDados>> {
        // Filtrar se houver termo de busca
        val itensFiltrados = if (termo.isEmpty()) {
            itens
        } else {
            val termoLower = termo.lowercase()
            itens.filter { item ->
                item.nome.lowercase().contains(termoLower) ||
                        item.categoria?.lowercase()?.contains(termoLower) == true ||
                        item.unidade.lowercase().contains(termoLower)
            }
        }

        val itensOrdenados = itensFiltrados.sortedWith(
            compareBy({ it.concluido }, { it.categoria ?: "" }, { it.nome })
        )

        return itensOrdenados.groupBy { it.categoria ?: "Sem categoria" }
    }

    fun atualizarStatusItem(item: ItemDados, concluido: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.atualizarStatus(currentListId, item.id, concluido)
                .onSuccess {
                    carregarItens(forceRefresh = true)
                }
                .onFailure { exception ->
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(errorMessage = "Erro ao atualizar item: ${exception.message}")
                        }
                    }
                }
        }
    }

    fun removerItem(item: ItemDados) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true) }
            }

            repository.removerItem(currentListId, item.id)
                .onSuccess {
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Item deletado!"
                            )
                        }
                    }
                    carregarItens(forceRefresh = true)
                }
                .onFailure { exception ->
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Erro ao deletar item: ${exception.message}"
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