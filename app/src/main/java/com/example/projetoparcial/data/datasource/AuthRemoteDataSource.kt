package com.example.projetoparcial.data.datasource

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRemoteDataSource {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): Result<String> {
        return try {
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

    suspend fun register(name: String, email: String, password: String): Result<String> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid
                ?: return Result.failure(Exception("Erro ao criar usuário"))

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            authResult.user?.updateProfile(profileUpdates)?.await()

            val dadosUsuario = hashMapOf(
                "nome" to name,
                "email" to email,
                "criadoEm" to Timestamp.now()
            )

            firestore.collection("usuarios")
                .document(uid)
                .set(dadosUsuario)
                .await()

            Result.success("Cadastro realizado com sucesso!")
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("A senha deve ter pelo menos 6 caracteres!"))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Este e-mail já está cadastrado!"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("E-mail inválido!"))
        } catch (e: Exception) {
            Result.failure(Exception("Erro ao cadastrar: ${e.message}"))
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