package com.example.data.encryption

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESCipher {
    private const val algorithm = "AES"
    private const val transformation = "AES/CBC/PKCS5Padding"
    private val key = "lacletressecrete".toByteArray(Charsets.UTF_8)
    private val iv = "ivtrestressecret".toByteArray(Charsets.UTF_8)

    init {
        require(key.size == 16) { "cle pas bonne" }
        require(iv.size == 16) { "iv pas bon" }
    }

    fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(transformation)
        val secretKeySpec = SecretKeySpec(key, algorithm)
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        return cipher.doFinal(data)
    }

    fun decrypt(encryptedData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(transformation)
        val secretKeySpec = SecretKeySpec(key, algorithm)
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        return cipher.doFinal(encryptedData)
    }
}