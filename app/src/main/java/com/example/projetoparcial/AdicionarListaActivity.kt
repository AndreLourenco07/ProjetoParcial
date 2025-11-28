package com.example.projetoparcial

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.projetoparcial.data.model.ListaDados
import com.example.projetoparcial.databinding.ActivityAdicionarListaBinding
import java.io.File

class AdicionarListaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdicionarListaBinding
    private var selectedImageUri: Uri? = null

    private val viewModel: ViewLista by viewModels()

    private var listId = ""
    private var listTitle = ""
    private var imageUri = ""

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                binding.imageViewPreview.setImageURI(it)
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                binding.imageViewPreview.setImageURI(selectedImageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Pega as informações que são passadas ao chamar a Activity para depois usar
        //como verificacao se é inclusão ou alteração
        listId = intent.getStringExtra("LIST_ID") ?: ""
        listTitle = intent.getStringExtra("LIST_TITLE") ?: ""
        imageUri = intent.getStringExtra("IMAGE_URI") ?: ""

        binding = ActivityAdicionarListaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Se tiver informação preenche o campo do nome da lista
        if (!listId.isEmpty()) {
            binding.edtNomeLista.setText(listTitle)
        }

        // Se tiver ID, preenche os campos e MOSTRA o botão remover
        if (listId.isNotEmpty()) {
            binding.edtNomeLista.setText(listTitle)

            // Lógica de visibilidade do botão Remover
            binding.btnExcluir.visibility = View.VISIBLE
            binding.btnExcluir.setOnClickListener {
                showDeleteConfirmationDialog()
            }
        } else {
            // Se for lista nova, ESCONDE o botão remover
            binding.btnExcluir.visibility = View.GONE
        }

        // Clique para escolher da galeria
        binding.imageViewPreview.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        // Clique para tirar foto
        binding.fabCamera.setOnClickListener {
            pedirPermissaoECapturar()
        }

        // Botão de salvar
        binding.buttonAdicionar.setOnClickListener {
            salvarLista()
        }
    }
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Remover Lista")
            .setMessage("Tem certeza de que deseja remover a lista '$listTitle'?")
            .setPositiveButton("Remover") { _, _ ->

                binding.btnExcluir.isEnabled = false

                BD().removerLista(
                    idLista = listId,
                    onSucesso = {
                        Toast.makeText(this, "Lista removida com sucesso", Toast.LENGTH_SHORT).show()

                        finish()

                        val intent = Intent(this, ListasActivity::class.java)
                        startActivity(intent)
                    },
                    onErro = { mensagem ->
                        binding.btnExcluir.isEnabled = true
                        Toast.makeText(this, "Erro: $mensagem", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun pedirPermissaoECapturar() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                abrirCamera()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> {
                AlertDialog.Builder(this)
                    .setTitle("Permissão necessária")
                    .setMessage("O app precisa acessar sua câmera para tirar fotos.")
                    .setPositiveButton("OK") { _, _ ->
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) abrirCamera()
            else Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show()
        }

    private fun abrirCamera() {
        val diretorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fotoFile = File(diretorio, "foto_${System.currentTimeMillis()}.jpg")

        selectedImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            fotoFile
        )

        cameraLauncher.launch(selectedImageUri)
    }

    private fun salvarLista() {
        val nome = binding.edtNomeLista.text.toString().trim()

        if (nome.isEmpty()) {
            Toast.makeText(this, "Digite um nome", Toast.LENGTH_SHORT).show()
            return
        }

        binding.buttonAdicionar.isEnabled = false
        binding.buttonAdicionar.text = "Enviando..."

        // 👉 CASO 1: SALVAR SEM FOTO
        if (selectedImageUri == null) {

            val novaLista = ListaDados(
                nome = nome,
                descricao = "",
                imageUrl = null  // sem foto
            )

            if (listId.isEmpty()) {
                BD().salvarLista(
                    lista = novaLista,
                    onSucesso = {
                        Toast.makeText(this, "Lista salva sem foto!", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onErro = {
                        Toast.makeText(this, "Erro ao salvar lista: $it", Toast.LENGTH_SHORT).show()
                    }
                )
            }else {
                BD().atualizarLista(
                    listId,
                    lista = novaLista,
                    onSucesso = {
                        Toast.makeText(this, "Lista atualizada sem foto!", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onErro = {
                        Toast.makeText(this, "Erro ao atualizar lista: $it", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            return
        }

        // 👉 CASO 2: SALVAR COM FOTO
        BD().salvarImagem(
            imageUri = selectedImageUri!!,
            onSucesso = { url ->

                val novaLista = ListaDados(
                    nome = nome,
                    descricao = "",
                    imageUrl = url
                )

                if (listId.isEmpty()) {
                    BD().salvarLista(
                        lista = novaLista,
                        onSucesso = {
                            Toast.makeText(this, "Lista salva!", Toast.LENGTH_SHORT).show()
                            finish()
                        },
                        onErro = {
                            Toast.makeText(this, "Erro ao salvar lista: $it", Toast.LENGTH_SHORT)
                                .show()
                        }
                    )
                }else{
                    BD().atualizarLista(
                        listId,
                        lista = novaLista,
                        onSucesso = {
                            Toast.makeText(this, "Lista atualizada sem foto!", Toast.LENGTH_SHORT).show()
                            finish()
                        },
                        onErro = {
                            Toast.makeText(this, "Erro ao atualizar lista: $it", Toast.LENGTH_SHORT).show()
                        }
                    )
                }


            },
            onErro = {
                Toast.makeText(this, "Erro ao enviar imagem: $it", Toast.LENGTH_SHORT).show()
            }
        )
    }

}

