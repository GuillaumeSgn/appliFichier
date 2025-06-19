package com.example.domain.repository

import android.net.Uri
import com.example.domain.model.Fichier
import java.io.File

interface FichierRepository {
    suspend fun uploadFichier(uri: Uri, name: String): Result<String>
    suspend fun getAll(): Result<List<Fichier>>
    suspend fun downloadFichier(name: String): Result<File>
    suspend fun deleteFile(name: String): Result<String>
}