package com.example.presentation.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.presentation.FichierViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(viewModel: FichierViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val fileList by viewModel.listFichier.collectAsState()
    val message by viewModel.message.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val downloadedFile by viewModel.downloadFile.collectAsState()

    var showUploadDialog by remember { mutableStateOf(false) }
    var fileToUploadUri by remember { mutableStateOf<Uri?>(null) }
    var fileToUploadName by remember { mutableStateOf("") }
    var uploadEncrypt by remember { mutableStateOf(false) }

    var showDownloadDialog by remember { mutableStateOf(false) }
    var fileToDownloadName by remember { mutableStateOf("") }
    var downloadDecrypt by remember { mutableStateOf(false) }

    val pickFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                fileToUploadUri = it
                val cursor = context.contentResolver.query(it, null, null, null, null)
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val nameIndex =
                            c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            fileToUploadName = c.getString(nameIndex)
                        }
                    }
                }
                showUploadDialog = true
            }
        }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(downloadedFile) {
        downloadedFile?.let { file ->
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".fileprovider",
                file
            )

            val mimeType = context.contentResolver.getType(fileUri) ?: "*/*"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val canOpen = intent.resolveActivity(context.packageManager) != null
            if (canOpen) {
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Impossible d'ouvrir le fichier : ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    context,
                    "Aucune application ne peut ouvrir ce type de fichier.",
                    Toast.LENGTH_LONG
                ).show()
            }
            viewModel.clearDownloadedFile()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gestionnaire de Fichiers") })
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = { pickFileLauncher.launch("*/*") },
                    Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Filled.Upload, "Télécharger un fichier")

                }
                FloatingActionButton(onClick = { viewModel.fetch() }) {
                    Icon(Icons.Filled.Refresh, "Rafraîchir la liste")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (fileList.isEmpty()) {
                Text(
                    "Aucun fichier sur le serveur.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(fileList) { appFile -> // Utilise AppFile
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        fileToDownloadName =
                                            appFile.name
                                        showDownloadDialog = true
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(appFile.name, modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.deleteFichier(appFile.name) }) {
                                    Icon(Icons.Filled.Delete, "Supprimer le fichier")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showUploadDialog) {
        ValidationDialog(
            title = "Télécharger Fichier : $fileToUploadName",
            confirmButtonText = "Télécharger",
            initialEncryptDecrypt = uploadEncrypt,
            onDismissRequest = { showUploadDialog = false },
            onConfirm = { encrypt ->
                fileToUploadUri?.let { uri ->
                    viewModel.uploadFichier(uri, fileToUploadName)
                }
                showUploadDialog = false
                fileToUploadUri = null
                fileToUploadName = ""
            }
        )
    }

    if (showDownloadDialog) {
        ValidationDialog(
            title = "Télécharger Fichier : $fileToDownloadName",
            confirmButtonText = "Télécharger",
            initialEncryptDecrypt = downloadDecrypt,
            onDismissRequest = { showDownloadDialog = false },
            onConfirm = { decrypt ->
                viewModel.downloadFichier(fileToDownloadName)
                showDownloadDialog = false
                fileToDownloadName = ""
            }
        )
    }
}