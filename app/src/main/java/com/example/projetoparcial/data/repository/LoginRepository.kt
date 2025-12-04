package com.example.projetoparcial.data.repository

import com.example.projetoparcial.data.datasource.AuthRemoteDataSource

class LoginRepository {
    private val authRemoteDatasource = AuthRemoteDataSource()

    suspend fun login(email: String, password: String): Result<String> {
        return authRemoteDatasource.login(email, password)
    }

    suspend fun sendPasswordResetEmail(email: String): Result<String> {
        return authRemoteDatasource.sendPasswordResetEmail(email)
    }

    fun isUserLoggedIn() = authRemoteDatasource.getCurrentUser() != null

    suspend fun register(name: String, email: String, password: String): Result<String> {
        return authRemoteDatasource.register(name, email, password)
    }

    fun logout() {
        authRemoteDatasource.logout()
    }
}