package com.example.projetoparcial.data.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.tasks.await

class AuthRemoteDataSource {
    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun login(email: String, password: String): Result<String> {
        return try {
            // await() suspende até o Firebase completar
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success("Login realizado com sucesso")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("E-mail ou senha incorretos!"))
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("Usuário não encontrado!"))
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao fazer login. Tente novamente!"))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<String> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success("Link de redefinição enviado para $email")
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("E-mail não encontrado!"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("E-mail inválido!"))
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao enviar e-mail. Tente novamente!"))
        }
    }

    fun getCurrentUser() = firebaseAuth.currentUser

    fun logout() {
        firebaseAuth.signOut()
    }
}