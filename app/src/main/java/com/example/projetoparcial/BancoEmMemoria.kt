package com.example.projetoparcial

data class Usuario(val nome: String, val email: String, val senha: String)

object UsuarioRepository {
    val usuarios = mutableListOf<Usuario>()

    fun adicionarUsuario(usuario: Usuario) {
        usuarios.add(usuario)
    }

    fun autenticar(email: String, senha: String): Boolean {
        return usuarios.any { it.email == email && it.senha == senha }
    }
}