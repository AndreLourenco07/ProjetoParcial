package com.example.projetoparcial.data.model

data class ItemDados(
    val id: String = "",
    val nome: String = "",
    val quantidade: Double = 1.0,
    val unidade: String = "UN",
    val categoria: String = "Outros",
    val concluido: Boolean = false
)