package com.example.projetoparcial.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.projetoparcial.data.repository.ListaRepository
import com.example.projetoparcial.databinding.ActivityAdicionarListaBinding
import com.example.projetoparcial.ui.viewmodel.AdicionarListaViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AdicionarListaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdicionarListaBinding
    private val viewModel: AdicionarListaViewModel by viewModels()
    private val repository = ListaRepository()

    private var listId = ""
    private var listTitle = ""
    private var imageUri = ""

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { viewModel.setImageUri(it) }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                // Imagem já está no viewModel
            }
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) abrirCamera()
            else Snackbar.make(binding.root, "Permissão negada", Snackbar.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listId = intent.getStringExtra("LIST_ID") ?: ""
        listTitle = intent.getStringExtra("LIST_TITLE") ?: ""
        imageUri = intent.getStringExtra("IMAGE_URI") ?: ""

        binding = ActivityAdicionarListaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInitialData()
        setupClickListeners()
        observeUiState()
    }

    private fun setupInitialData() {
        if (listId.isNotEmpty()) {
            binding.txtTitulo.setText("Editar Lista")
            binding.edtNomeLista.setText(listTitle)
            viewModel.setNomeLista(listTitle)

            binding.btnExcluir.visibility = View.VISIBLE
        } else {
            binding.txtTitulo.setText("Adicionar Lista")
            binding.btnExcluir.visibility = View.GONE
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Atualizar imagem
                    state.imageUri?.let { uri ->
                        binding.imgViewPreview.setImageURI(uri)
                    }

                    // Atualizar botões
                    binding.btnAdicionar.isEnabled = !state.isLoading
                    binding.btnExcluir.isEnabled = !state.isLoading
                    binding.btnAdicionar.text = if (state.isLoading) "Enviando..." else "Salvar"

                    // Mensagens de erro
                    state.errorMessage?.let { message ->
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                        viewModel.limparMensagens()
                    }

                    // Verificar se foi salvo
                    if (state.isSaved) {
                        Snackbar.make(
                            binding.root,
                            if (listId.isEmpty()) "Lista salva!" else "Lista atualizada!",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        viewModel.resetSavedState()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.imgViewPreview.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.fabCamera.setOnClickListener {
            pedirPermissaoECapturar()
        }

        binding.btnAdicionar.setOnClickListener {
            val nome = binding.edtNomeLista.text.toString().trim()
            viewModel.salvarLista(listId, nome, viewModel.uiState.value.imageUri)
        }

        binding.btnExcluir.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Excluir Lista")
            .setMessage("Tem certeza de que deseja excluir a lista '$listTitle'?")
            .setPositiveButton("Excluir") { _, _ ->
                excluirLista()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun excluirLista() {
        binding.btnExcluir.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            repository.excluirLista(listId)
                .onSuccess {
                    withContext(Dispatchers.Main) {
                        Snackbar.make(
                            binding.root,
                            "Lista excluida com sucesso",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@AdicionarListaActivity, ListasActivity::class.java)

                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

                        startActivity(intent)
                    }
                }
                .onFailure { exception ->
                    withContext(Dispatchers.Main) {
                        binding.btnExcluir.isEnabled = true
                        Snackbar.make(
                            binding.root,
                            "Erro: ${exception.message}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
        }
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

    private fun abrirCamera() {
        val diretorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fotoFile = File(diretorio, "foto_${System.currentTimeMillis()}.jpg")

        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            fotoFile
        )

        viewModel.setImageUri(uri)
        cameraLauncher.launch(uri)
    }
}