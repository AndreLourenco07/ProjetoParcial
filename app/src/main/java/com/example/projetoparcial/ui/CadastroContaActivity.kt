package com.example.projetoparcial.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.projetoparcial.databinding.ActivityCadastroContaBinding
import com.example.projetoparcial.ui.viewmodel.RegisterViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CadastroContaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCadastroContaBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCadastroContaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeUiState()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupUI() {
        binding.btnCriar.setOnClickListener {
            val nome = binding.edtNome.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val senha = binding.edtSenha.text.toString().trim()
            val confirmaSenha = binding.edtConfirmaSenha.text.toString().trim()

            viewModel.onNameChanged(nome)
            viewModel.onEmailChanged(email)
            viewModel.onPasswordChanged(senha)
            viewModel.onConfirmPasswordChanged(confirmaSenha)
            viewModel.onRegisterClicked()
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isLoading) {
                        mostrarLoading()
                    } else {
                        esconderLoading()
                    }

                    if (state.isSuccess) {
                        Snackbar.make(binding.root, "Cadastro realizado com sucesso!", Snackbar.LENGTH_SHORT).show()

                        binding.edtNome.setText("")
                        binding.edtEmail.setText("")
                        binding.edtSenha.setText("")
                        binding.edtConfirmaSenha.setText("")

                        val prefs = getSharedPreferences("cadastro_temp", MODE_PRIVATE)
                        prefs.edit { clear() }

                        val intent = Intent(this@CadastroContaActivity, ListasActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }

                    state.errorMessage?.let { message ->
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun mostrarLoading() {
        binding.btnCriar.isEnabled = false
        binding.btnCriar.text = ""
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun esconderLoading() {
        binding.btnCriar.isEnabled = true
        binding.btnCriar.text = "Criar Conta"
        binding.progressBar.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        val prefs = getSharedPreferences("cadastro_temp", MODE_PRIVATE)
        binding.edtNome.setText(prefs.getString("nome", ""))
        binding.edtEmail.setText(prefs.getString("email", ""))
        binding.edtSenha.setText(prefs.getString("senha", ""))
        binding.edtConfirmaSenha.setText(prefs.getString("confirmaSenha", ""))
    }

    override fun onStop() {
        super.onStop()
        val prefs = getSharedPreferences("cadastro_temp", MODE_PRIVATE)
        prefs.edit {
            putString("nome", binding.edtNome.text.toString())
            putString("email", binding.edtEmail.text.toString())
            putString("senha", binding.edtSenha.text.toString())
            putString("confirmaSenha", binding.edtConfirmaSenha.text.toString())
        }
    }
}