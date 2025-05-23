package com.example.diplomaapp

import com.example.diplomaapp.model.TranslationRequest
import com.example.diplomaapp.network.RetrofitClient
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*

class TranslateFragment : Fragment(R.layout.fragment_translate) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val spinner = view.findViewById<Spinner>(R.id.spinnerDirection)
        val inputText = view.findViewById<EditText>(R.id.inputText)
        val outputText = view.findViewById<TextView>(R.id.outputText)
        val translateBtn = view.findViewById<Button>(R.id.buttonTranslate)

        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf("ru2agx", "agx2ru"))

        translateBtn.setOnClickListener {
            val direction = spinner.selectedItem.toString()
            val request = TranslationRequest(inputText.text.toString(), direction)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.instance.translate(request)
                    activity?.runOnUiThread { outputText.text = response.translation }
                } catch (e: Exception) {
                    activity?.runOnUiThread { outputText.text = "Ошибка: ${e.message}" }
                }
            }
        }
    }
}
