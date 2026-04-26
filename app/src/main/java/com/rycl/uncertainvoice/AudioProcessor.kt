package com.rycl.uncertainvoice

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import kotlin.concurrent.thread

class AudioProcessor {
    private var isRunning = false
    private val sampleRate = 44100

    fun startChangingVoice(pitch: Double) {
        isRunning = true
        thread {
            val minBufSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val record = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufSize)
            
            // Trik ubah suara: Mainkan di sample rate yang berbeda (Lebih cepat = Chipmunk)
            val playRate = (sampleRate * pitch).toInt() 
            val track = AudioTrack(3, playRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufSize, AudioTrack.MODE_STREAM)

            val buffer = ShortArray(minBufSize)
            record.startRecording()
            track.play()

            while (isRunning) {
                val read = record.read(buffer, 0, buffer.size)
                track.write(buffer, 0, read)
            }

            record.stop()
            record.release()
            track.stop()
            track.release()
        }
    }

    fun stopProcessing() {
        isRunning = false
    }
}
