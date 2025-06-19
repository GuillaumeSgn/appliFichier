package com.example.domain.usecase

import com.example.domain.repository.FichierRepository
import javax.inject.Inject

class DeleteFileUseCase @Inject constructor(
    private val repository: FichierRepository
) {
    suspend operator fun invoke(name: String): Result<String> {
        return repository.deleteFile(name)
    }
}