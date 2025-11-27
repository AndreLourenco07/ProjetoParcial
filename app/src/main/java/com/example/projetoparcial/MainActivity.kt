package com.example.projetoparcial

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projetoparcial.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    // Criar binding
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflar o layout com ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        //Botao para acessar a tela de cadastro
        binding.btnCadastro.setOnClickListener {
            val intent = Intent(this, CadastroContaActivity::class.java)
            startActivity(intent)
        }

        //Botao para acessar com firebase
        binding.btnAcessar.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val senha = binding.senhaInput.text.toString().trim()

            when {
                email.isEmpty() || senha.isEmpty() -> {
                    Snackbar.make(binding.root, "Preencha todos os campos!", Snackbar.LENGTH_SHORT).show()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Snackbar.make(binding.root, "E-mail inválido!", Snackbar.LENGTH_SHORT).show()
                }
                else -> {
                    firebaseAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener {
                        if (it.isSuccessful){
                            binding.emailInput.setText("")
                            binding.senhaInput.setText("")

                            val intent = Intent(this, ListasActivity::class.java)
                            startActivity(intent)
                        }else{
                            val mensagemErro = when (it.exception) {
                                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                                    "E-mail ou senha incorretos!"
                                is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
                                    "Usuário não encontrado!"
                                else ->
                                    "Erro ao fazer login. Tente novamente!"
                            }
                            Snackbar.make(binding.root, mensagemErro, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        binding.textEsqueceuSenha.setOnClickListener {
            mostrarDialogoRedefinirSenha()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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
                    firebaseAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Snackbar.make(
                                    binding.root,
                                    "Link de redefinição enviado para $email",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            } else {
                                val mensagemErro = when (task.exception) {
                                    is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
                                        "E-mail não encontrado!"
                                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                                        "E-mail inválido!"
                                    else ->
                                        "Erro ao enviar e-mail. Tente novamente!"
                                }
                                Snackbar.make(binding.root, mensagemErro, Snackbar.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    override fun onStart() {
        super.onStart()
        // Recupera os dados salvos
        val prefs = getSharedPreferences("login_temp", MODE_PRIVATE)
        binding.emailInput.setText(prefs.getString("email", ""))
        binding.senhaInput.setText(prefs.getString("senha", ""))
    }

    override fun onStop() {
        super.onStop()
        // Salva os dados temporários
        val prefs = getSharedPreferences("login_temp", MODE_PRIVATE)
        prefs.edit {
            putString("email", binding.emailInput.text.toString())
            putString("senha", binding.senhaInput.text.toString())
        }
    }
}