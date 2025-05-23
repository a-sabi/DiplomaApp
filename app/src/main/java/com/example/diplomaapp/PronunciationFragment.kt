package com.example.diplomaapp

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.diplomaapp.model.Phrase
import com.example.diplomaapp.network.RetrofitClient
import com.example.diplomaapp.util.PhraseRepository
import com.example.diplomaapp.util.WavAudioRecorder
import kotlinx.coroutines.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File

class PronunciationFragment : Fragment(R.layout.fragment_pronunciation) {
    private var selectedPhrase: Phrase? = null
    private val recorder = WavAudioRecorder()
    private val filePath by lazy { File(requireContext().cacheDir, "recorded.wav").absolutePath }
    private var isRecording = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val phraseText = view.findViewById<TextView>(R.id.phraseText)
        val selectButton = view.findViewById<Button>(R.id.buttonSelectPhrase)
        val playButton = view.findViewById<Button>(R.id.buttonPlayRef)
        val recordButton = view.findViewById<Button>(R.id.buttonRecord)
        val evaluateButton = view.findViewById<Button>(R.id.buttonEvaluate)
        val resultText = view.findViewById<TextView>(R.id.resultText)

        PhraseRepository.loadFromAssets(requireContext())

        selectButton.setOnClickListener {
            val phrases = PhraseRepository.getAll()
            AlertDialog.Builder(requireContext())
                .setTitle("Выбор фразы")
                .setItems(phrases.map { it.text }.toTypedArray()) { _, which ->
                    selectedPhrase = phrases[which]
                    phraseText.text = "${selectedPhrase!!.text} — ${selectedPhrase!!.agx_placeholder}"
                }.show()
        }

        playButton.setOnClickListener {
            try {
                val afd = requireContext().assets.openFd(selectedPhrase!!.audio_path)
                MediaPlayer().apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    prepare()
                    start()
                }
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Ошибка воспроизведения", Toast.LENGTH_SHORT).show()
            }
        }

        recordButton.setOnClickListener {
            if (requireContext().checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Нет разрешения на запись", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isRecording) {
                recorder.start(filePath, requireContext())
                isRecording = true
                recordButton.text = "⏹ Закончить запись"
                Toast.makeText(requireContext(), "Запись начата", Toast.LENGTH_SHORT).show()
            } else {
                recorder.stop()
                isRecording = false
                recordButton.text = "🎙 Запись"
                Toast.makeText(requireContext(), "Запись завершена", Toast.LENGTH_SHORT).show()
            }
        }

        evaluateButton.setOnClickListener {
            recorder.stop()
            isRecording = false

            val audioFile = File(filePath)
            val requestFile = audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("user_audio", audioFile.name, requestFile)
            val idBody = selectedPhrase!!.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = RetrofitClient.instance.evaluatePronunciation(idBody, audioPart)
                    activity?.runOnUiThread {
                        resultText.text = "Оценка: %.2f — %s".format(result.distance, scoreToText(result.distance))
                    }
                } catch (e: Exception) {
                    activity?.runOnUiThread {
                        resultText.text = "Ошибка при оценке: ${e.message}"
                    }
                }
            }
        }
    }

    private fun scoreToText(score: Float) = when {
        score < 20 -> "Отлично!"
        score < 40 -> "Хорошо"
        score < 60 -> "Можно лучше"
        else -> "Плохо"
    }
}
