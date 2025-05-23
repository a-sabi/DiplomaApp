package com.example.diplomaapp.model

data class TranslationRequest(val text: String, val direction: String)
data class TranslationResponse(val translation: String)
data class PronunciationResponse(val distance: Float)
