package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.api.FichierApi
import com.example.data.encryption.AESCipher
import com.example.domain.model.Fichier
import com.example.domain.repository.FichierRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class FichierRepositoryImplem @Inject constructor(
    private val fichierApi: FichierApi,
    private val context: Context
) : FichierRepository {

    override suspend fun uploadFichier(uri: Uri, name: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val fileBytes = inputStream?.readBytes() ?: throw Exception("URI incorrecte")
                inputStream.close()

                val processedBytes = AESCipher.encrypt(fileBytes)
                val requestBody = processedBytes.toRequestBody(
                    "application/octet-stream".toMediaTypeOrNull(),
                    0,
                    processedBytes.size
                )
                val body = MultipartBody.Part.createFormData("file", name, requestBody)

                val response = fichierApi.uploadFile(body)
                if (response.isSuccessful) {
                    Result.success(response.body()?.message ?: "Envoi terminé avec succès")
                } else {
                    Result.failure(Exception("Erreur dans le téléchargement"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getAll(): Result<List<Fichier>> = withContext(Dispatchers.IO) {
        try {
            val response = fichierApi.getAll()
            if (response.isSuccessful) {
                val fichiersApp = response.body()?.map { Fichier(it) } ?: emptyList()
                Result.success(fichiersApp)
            } else {
                Result.failure(Exception("Erreur de liste"))

            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadFichier(name: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val response = fichierApi.downloadFile(name)
            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("body vide")
                val fichierTemp = File(context.cacheDir, name)

                body.byteStream().use { inputStream ->
                    val rawBytes = inputStream.readBytes()
                    val processedBytes = AESCipher.decrypt(rawBytes)

                    FileOutputStream(fichierTemp).use { outputStream ->
                        outputStream.write(processedBytes)
                    }
                }
                Result.success(fichierTemp)
            } else {
                Result.failure(Exception("Echec téléchargement"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFile(name: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = fichierApi.deleteFile(name)
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: " Fichier supprimé")
            } else {
                Result.failure(Exception("Echec suppression"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}