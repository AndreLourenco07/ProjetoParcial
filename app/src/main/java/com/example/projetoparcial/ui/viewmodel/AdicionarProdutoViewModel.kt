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

data class AdicionarProdutoUiState(
    val nomeItem: String = "",
    val quantidade: Double = 1.0,
    val unidade: String = "",
    val categoria: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

class AdicionarProdutoViewModel : ViewModel() {

    private val repository = ItemRepository()

    private val _uiState = MutableStateFlow(AdicionarProdutoUiState())
    val uiState: StateFlow<AdicionarProdutoUiState> = _uiState.asStateFlow()

    fun salvarProduto(
        idLista: String,
        itemId: String,
        nome: String,
        quantidade: Double,
        unidade: String,
        categoria: String
    ) {
        if (nome.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Digite um nome para o produto") }
            return
        }

        if (idLista.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Erro: lista não identificada!") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            val item = ItemDados(
                nome = nome,
                quantidade = quantidade,
                unidade = unidade,
                categoria = categoria
            )

            val result = if (itemId.isEmpty()) {
                repository.salvarItem(idLista, item)
            } else {
                repository.atualizarItem(idLista, itemId, item)
            }

            result
                .onSuccess {
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSaved = true
                            )
                        }
                    }
                }
                .onFailure { exception ->
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = if (itemId.isEmpty()) {
                                    "Erro ao salvar produto: ${exception.message}"
                                } else {
                                    "Erro ao atualizar produto: ${exception.message}"
                                }
                            )
                        }
                    }
                }
        }
    }

    fun limparMensagens() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetSavedState() {
        _uiState.update { it.copy(isSaved = false) }
    }
}