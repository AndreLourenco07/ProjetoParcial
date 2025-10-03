package com.example.projetoparcial

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projetoparcial.databinding.ActivityCadastroContaBinding

class CadastroContaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroContaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ViewBinding para o layout de cadastro
        binding = ActivityCadastroContaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnCriar.setOnClickListener {
            val nome  = binding.nomeInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val senha = binding.senhaInput.text.toString().trim()
            val confirmaSenha = binding.confirmaSenhaInput.text.toString().trim()

            if (nome.isNotEmpty() && email.isNotEmpty() && senha.isNotEmpty() && confirmaSenha.isNotEmpty()) {
                if (senha == confirmaSenha) {
                    val usuario = Usuario(nome, email, senha)
                    UsuarioRepository.adicionarUsuario(usuario)

                    Toast.makeText(this, "Cadastro realizado!", Toast.LENGTH_SHORT).show()

                    binding.nomeInput.setText("")
                    binding.emailInput.setText("")
                    binding.senhaInput.setText("")
                    binding.confirmaSenhaInput.setText("")

                    // limpa os SharedPreferences após cadastro
                    val prefs = getSharedPreferences("cadastro_temp", MODE_PRIVATE)
                    prefs.edit {
                        clear()   // mesma coisa do clear()
                    }

                    finish() // volta pra tela de login
                } else {
                    Toast.makeText(this, "As senhas não são iguais!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Recupera os dados salvos
        val prefs = getSharedPreferences("cadastro_temp", MODE_PRIVATE)
        binding.nomeInput.setText(prefs.getString("nome", ""))
        binding.emailInput.setText(prefs.getString("email", ""))
        binding.senhaInput.setText(prefs.getString("senha", ""))
        binding.confirmaSenhaInput.setText(prefs.getString("confirmaSenha", ""))
    }

    override fun onStop() {
        super.onStop()
        // Salva os dados temporários
        val prefs = getSharedPreferences("cadastro_temp", MODE_PRIVATE)
        prefs.edit {
            putString("nome", binding.nomeInput.text.toString())
            putString("email", binding.emailInput.text.toString())
            putString("senha", binding.senhaInput.text.toString())
            putString("confirmaSenha", binding.confirmaSenhaInput.text.toString())
        }
    }
}