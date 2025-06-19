package com.example.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Fichier
import com.example.domain.usecase.DeleteFileUseCase
import com.example.domain.usecase.DownloadFileUseCase
import com.example.domain.usecase.GetAllUseCase
import com.example.domain.usecase.UploadFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FichierViewModel @Inject constructor(
    private val getAllUseCase: GetAllUseCase,
    private val uploadFileUseCase: UploadFileUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase
) : ViewModel() {

    private val _listFichiers = MutableStateFlow<List<Fichier>>(emptyList())
    val listFichier: StateFlow<List<Fichier>> = _listFichiers

    private val _downloadFichier = MutableStateFlow<File?>(null)
    val downloadFile: StateFlow<File?> = _downloadFichier

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        fetch()
    }

    fun fetch() {
        viewModelScope.launch {
            getAllUseCase()
                .onSuccess {
                    _listFichiers.value = it
                }
                .onFailure {
                    _errorMessage.value = "Erreur de chargement de la liste"
                }
        }
    }

    fun uploadFichier(uri: Uri, name: String) {
        viewModelScope.launch {
            _message.value = null
            _errorMessage.value = null
            uploadFileUseCase(uri, name)
                .onSuccess {
                    _message.value = it
                    fetch()
                }
                .onFailure {
                    Log.e("bite", "Erreur d'envoi: ${it.message}")

                    _errorMessage.value = "Erreur d'envoi: ${it.message}"
                }
        }
    }

    fun downloadFichier(name: String) {
        viewModelScope.launch {
            _message.value = null
            _errorMessage.value = null
            _downloadFichier.value = null
            downloadFileUseCase(name)
                .onSuccess {
                    _message.value =
                        " Fichier '$name' téléchargé avec succès. Chemin: ${it.absoluteFile}"
                    _downloadFichier.value = it
                }
                .onFailure {
                    _errorMessage.value = "Erreur téléchargement: ${it.message}"
                }
        }
    }

    fun deleteFichier(name: String) {
        viewModelScope.launch {
            _message.value = null
            _errorMessage.value = null
            deleteFileUseCase(name)
                .onSuccess {
                    _message.value = it
                    fetch()
                }
                .onFailure {
                    _errorMessage.value = "Erreur de suppression : ${it.message}"
                }
        }
    }

    fun clearMessages() {
        _message.value = null
        _errorMessage.value = null
    }

    fun clearDownloadedFile() {
        _downloadFichier.value = null
    }
}