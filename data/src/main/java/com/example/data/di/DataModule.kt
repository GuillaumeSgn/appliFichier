package com.example.data.di

import android.content.Context
import com.example.data.api.FichierApi
import com.example.data.repository.FichierRepositoryImplem
import com.example.domain.repository.FichierRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Singleton
    @Provides
    fun provideFichierApi(
        retrofit: Retrofit
    ): FichierApi {
        return retrofit.create(FichierApi::class.java)
    }

    @Singleton
    @Provides
    fun provideFichierRepository(
        fichierApi: FichierApi,
        @ApplicationContext context: Context
    ): FichierRepository {
        return FichierRepositoryImplem(fichierApi, context)
    }
}