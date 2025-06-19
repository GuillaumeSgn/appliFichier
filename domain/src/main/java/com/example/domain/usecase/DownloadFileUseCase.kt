package com.example.domain.usecase

import com.example.domain.repository.FichierRepository
import java.io.File
import javax.inject.Inject

class DownloadFileUseCase @Inject constructor(
    private val repository: FichierRepository
) {
    suspend operator fun invoke(name: String): Result<File> {
        return repository.downloadFichier(name)
    }
}