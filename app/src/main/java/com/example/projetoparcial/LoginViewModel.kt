package com.example.projetoparcial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginViewModel : ViewModel(){

    // Expose screen UI state
    private val _uiState = MutableStateFlow("-")
    val uiState: StateFlow<String> = _uiState.asStateFlow()

    fun onSorteioClicked() {
        viewModelScope.launch(Dispatchers.IO) {
//            val fact = repository.getFact()
//            withContext(Dispatchers.Main) {
//                _uiState.update {
//                    fact?.text ?: "-"
//                }
//            }
        }
    }
}