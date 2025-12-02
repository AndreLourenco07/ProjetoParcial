package com.example.projetoparcial.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.projetoparcial.databinding.ActivityMainBinding
import com.example.projetoparcial.ui.viewmodel.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
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
        binding.btnCadastro.setOnClickListener {
            val intent = Intent(this, CadastroContaActivity::class.java)
            startActivity(intent)
        }

        binding.btnAcessar.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val senha = binding.edtSenha.text.toString().trim()

            when {
                email.isEmpty() || senha.isEmpty() -> {
                    Snackbar.make(binding.root, "Preencha todos os campos!", Snackbar.LENGTH_SHORT).show()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Snackbar.make(binding.root, "E-mail inválido!", Snackbar.LENGTH_SHORT).show()
                }
                else -> {
                    viewModel.onEmailChanged(email)
                    viewModel.onPasswordChanged(senha)
                    viewModel.onLoginClicked()
                }
            }
        }

        binding.txtEsqueceuSenha.setOnClickListener {
            mostrarDialogoRedefinirSenha()
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isSuccess) {
                        binding.edtEmail.setText("")
                        binding.edtSenha.setText("")
                        val intent = Intent(this@MainActivity, ListasActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    state.errorMessage?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun mostrarDialogoRedefinirSenha() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Redefinir Senha")
        builder.setMessage("Digite seu e-mail para receber o link de redefinição:")

        val input = EditText(this)
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        input.hint = "exemplo@email.com"
        builder.setView(input)

        builder.setPositiveButton("Enviar") { _, _ ->
            val email = input.text.toString().trim()

            when {
                email.isEmpty() -> {
                    Snackbar.make(binding.root, "Digite um e-mail!", Snackbar.LENGTH_SHORT).show()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Snackbar.make(binding.root, "E-mail inválido!", Snackbar.LENGTH_SHORT).show()
                }
                else -> {
                    viewModel.onPasswordResetClicked(email)
                }
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.checkIfUserIsLoggedIn()) {
            val intent = Intent(this, ListasActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val prefs = getSharedPreferences("login_temp", MODE_PRIVATE)
            binding.edtEmail.setText(prefs.getString("email", ""))
            binding.edtSenha.setText(prefs.getString("senha", ""))
        }
    }

    override fun onStop() {
        super.onStop()
        val prefs = getSharedPreferences("login_temp", MODE_PRIVATE)
        prefs.edit {
            putString("email", binding.edtEmail.text.toString())
            putString("senha", binding.edtSenha.text.toString())
        }
    }
}