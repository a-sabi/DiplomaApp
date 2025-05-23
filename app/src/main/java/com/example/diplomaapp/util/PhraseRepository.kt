package com.example.diplomaapp.util

import android.content.Context
import com.example.diplomaapp.model.Phrase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PhraseRepository {
    private val phrases = mutableListOf<Phrase>()

    fun loadFromAssets(context: Context) {
        val json = context.assets.open("phrases.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Phrase>>() {}.type
        phrases.clear()
        phrases.addAll(Gson().fromJson(json, type))
    }

    fun getAll(): List<Phrase> = phrases
    fun getById(id: Int): Phrase? = phrases.find { it.id == id }
}
