package com.example.domain.usecase

import android.net.Uri
import com.example.domain.repository.FichierRepository
import javax.inject.Inject

class UploadFileUseCase @Inject constructor(
    private val repository: FichierRepository
) {
    suspend operator fun invoke(uri: Uri, name: String): Result<String> {
        return repository.uploadFichier(uri, name)
    }
}