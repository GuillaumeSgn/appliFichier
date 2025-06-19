package com.example.domain.usecase

import com.example.domain.model.Fichier
import com.example.domain.repository.FichierRepository
import javax.inject.Inject

class GetAllUseCase @Inject constructor(
    private val repository: FichierRepository
) {
    suspend operator fun invoke(): Result<List<Fichier>> {
        return repository.getAll()
    }
}