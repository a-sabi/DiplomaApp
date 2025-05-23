package com.example.diplomaapp.util

import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import java.io.*

class WavAudioRecorder {
    private var isRecording = false
    private var thread: Thread? = null
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private lateinit var audioRecord: AudioRecord

    fun start(filePath: String, context: Context) {
        if (context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) return

        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)

        val pcmFile = File(filePath.replace(".wav", ".pcm"))
        isRecording = true
        audioRecord.startRecording()

        thread = Thread {
            FileOutputStream(pcmFile).use { os ->
                val buffer = ByteArray(bufferSize)
                while (isRecording) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) os.write(buffer, 0, read)
                }
            }
            audioRecord.stop()
            audioRecord.release()
            pcmToWav(pcmFile, File(filePath))
            pcmFile.delete()
        }
        thread?.start()
    }

    fun stop() {
        isRecording = false
        thread?.join()
    }

    private fun pcmToWav(pcmFile: File, wavFile: File) {
        val totalAudioLen = pcmFile.length()
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = 16 * sampleRate * channels / 8
        val header = ByteArray(44)
        val out = FileOutputStream(wavFile)
        val input = FileInputStream(pcmFile)

        writeWavHeader(header, totalAudioLen, totalDataLen, byteRate, channels)
        out.write(header, 0, 44)

        val buffer = ByteArray(1024)
        while (true) {
            val read = input.read(buffer)
            if (read == -1) break
            out.write(buffer, 0, read)
        }

        input.close()
        out.close()
    }

    private fun writeWavHeader(header: ByteArray, audioLen: Long, dataLen: Long, byteRate: Int, channels: Int) {
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        writeInt(header, 4, dataLen.toInt())
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        writeInt(header, 16, 16)
        writeShort(header, 20, 1)
        writeShort(header, 22, channels.toShort())
        writeInt(header, 24, sampleRate)
        writeInt(header, 28, byteRate)
        writeShort(header, 32, (channels * 16 / 8).toShort())
        writeShort(header, 34, 16)
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        writeInt(header, 40, audioLen.toInt())
    }

    private fun writeInt(b: ByteArray, offset: Int, value: Int) {
        b[offset] = (value and 0xff).toByte()
        b[offset + 1] = ((value shr 8) and 0xff).toByte()
        b[offset + 2] = ((value shr 16) and 0xff).toByte()
        b[offset + 3] = ((value shr 24) and 0xff).toByte()
    }

    private fun writeShort(b: ByteArray, offset: Int, value: Short) {
        b[offset] = (value.toInt() and 0xff).toByte()
        b[offset + 1] = ((value.toInt() shr 8) and 0xff).toByte()
    }
}
