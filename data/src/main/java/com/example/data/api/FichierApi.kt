package com.example.data.api

import com.example.data.entity.MessageResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface FichierApi {

    @GET("/")
    suspend fun getAll(): Response<List<String>>

    @Multipart
    @POST("/")
    suspend fun uploadFile(@Part fichier: MultipartBody.Part): Response<MessageResponse>

    @GET("fichiers/{nomfichier}")
    suspend fun downloadFile(@Path("nomfichier") nomfichier: String): Response<ResponseBody>

    @DELETE("fichiers/{nomfichier}")
    suspend fun deleteFile(@Path("nomfichier") nomfichier: String): Response<MessageResponse>

}