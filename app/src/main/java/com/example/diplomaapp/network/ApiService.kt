package com.example.diplomaapp.network

import com.example.diplomaapp.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    @POST("translate")
    suspend fun translate(@Body request: TranslationRequest): TranslationResponse

    @Multipart
    @POST("evaluate_pronunciation")
    suspend fun evaluatePronunciation(
        @Part("phrase_id") phraseId: RequestBody,
        @Part user_audio: MultipartBody.Part
    ): PronunciationResponse
}
