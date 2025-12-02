package com.example.projetoparcial.ui.viewmodel

import android.net.Uri
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

data class AdicionarListaUiState(
    val nomeLista: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

class AdicionarListaViewModel : ViewModel() {

    private val repository = ListaRepository()

    private val _uiState = MutableStateFlow(AdicionarListaUiState())
    val uiState: StateFlow<AdicionarListaUiState> = _uiState.asStateFlow()

    fun setNomeLista(nome: String) {
        _uiState.update { it.copy(nomeLista = nome) }
    }

    fun setImageUri(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    fun salvarLista(listId: String, nome: String, imageUri: Uri?) {
        if (nome.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Digite um nome para a lista") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            // Salvar imagem se houver
            val imageUrl = if (imageUri != null) {
                repository.salvarImagem(imageUri).getOrElse { exception ->
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Erro ao enviar imagem: ${exception.message}"
                            )
                        }
                    }
                    return@launch
                }
            } else {
                null
            }

            // Criar/atualizar lista
            val novaLista = ListaDados(
                nome = nome,
                descricao = "",
                imageUrl = imageUrl
            )

            val result = if (listId.isEmpty()) {
                repository.salvarLista(novaLista)
            } else {
                repository.atualizarLista(listId, novaLista)
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
                                errorMessage = "Erro ao salvar lista: ${exception.message}"
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