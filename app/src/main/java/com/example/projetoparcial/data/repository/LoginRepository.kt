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

    fun logout() {
        authRemoteDatasource.logout()
    }
}