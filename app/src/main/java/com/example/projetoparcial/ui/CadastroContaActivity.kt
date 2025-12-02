package com.example.projetoparcial.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projetoparcial.databinding.ActivityCadastroContaBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class CadastroContaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroContaBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ViewBinding para o layout de cadastro
        binding = ActivityCadastroContaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnCriar.setOnClickListener {
            val nome  = binding.edtNome.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val senha = binding.edtSenha.text.toString().trim()
            val confirmaSenha = binding.edtConfirmaSenha.text.toString().trim()

            if (nome.isNotEmpty() && email.isNotEmpty() && senha.isNotEmpty() && confirmaSenha.isNotEmpty()) {
                if (senha == confirmaSenha) {
                    firebaseAuth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener {
                        if (it.isSuccessful){
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }else{
                            Snackbar.make(binding.root, it.exception.toString(), Snackbar.LENGTH_SHORT).show()
                        }
                    }

                    binding.edtNome.setText("")
                    binding.edtEmail.setText("")
                    binding.edtSenha.setText("")
                    binding.edtConfirmaSenha.setText("")

                    // limpa os SharedPreferences após cadastro
                    val prefs = getSharedPreferences("cadastro_temp", MODE_PRIVATE)
                    prefs.edit {
                        clear()
                    }

                    finish()
                } else {
                    Snackbar.make(binding.root, "As senhas não são iguais!", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(binding.root, "Preencha todos os campos!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Recupera os dados salvos
        val prefs = getSharedPreferences("cadastro_temp", MODE_PRIVATE)
        binding.edtNome.setText(prefs.getString("nome", ""))
        binding.edtEmail.setText(prefs.getString("email", ""))
        binding.edtSenha.setText(prefs.getString("senha", ""))
        binding.edtConfirmaSenha.setText(prefs.getString("confirmaSenha", ""))
    }

    override fun onStop() {
        super.onStop()
        // Salva os dados temporários
        val prefs = getSharedPreferences("cadastro_temp", MODE_PRIVATE)
        prefs.edit {
            putString("nome", binding.edtNome.text.toString())
            putString("email", binding.edtEmail.text.toString())
            putString("senha", binding.edtSenha.text.toString())
            putString("confirmaSenha", binding.edtConfirmaSenha.text.toString())
        }
    }
}