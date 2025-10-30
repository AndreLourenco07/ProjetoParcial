package com.example.projetoparcial

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import androidx.core.content.edit
import com.example.projetoparcial.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // Criar binding
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflar o layout com ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCadastro.setOnClickListener {
            val intent = Intent(this, CadastroContaActivity::class.java)
            startActivity(intent)
        }

        binding.btnAcessar.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val senha = binding.senhaInput.text.toString().trim()

            val intent = Intent(this, ListasActivity::class.java)
            startActivity(intent)
//            when {
//                email.isEmpty() || senha.isEmpty() -> {
//                    Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
//                }
//                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
//                    Toast.makeText(this, "E-mail inválido!", Toast.LENGTH_SHORT).show()
//                }
//                UsuarioRepository.autenticar(email, senha) -> {
//                    val intent = Intent(this, ListasActivity::class.java)
//                    startActivity(intent)
//
//                    binding.emailInput.setText("")
//                    binding.senhaInput.setText("")
//
//                    // limpa os SharedPreferences após cadastro
//                    val prefs = getSharedPreferences("login_temp", MODE_PRIVATE)
//                    prefs.edit {
//                        clear()   // mesma coisa do clear()
//                    }
//                }
//                else -> {
//                    Toast.makeText(this, "Não existe nenhuma conta com essas credênciais", Toast.LENGTH_SHORT).show()
//                }
//            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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