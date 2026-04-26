package com.rycl.uncertainvoice

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.PitchShifter
import kotlin.concurrent.thread

class AudioProcessor {

    private var dispatcher: AudioDispatcher? = null

    // Fungsi untuk mulai memproses suara secara real-time
    // pitchValue: 1.0 (normal), 2.0 (Chipmunk), 0.5 (Deep/Monster)
    fun startChangingVoice(pitchValue: Double) {
        // 1. Ambil audio dari Mic HP (Sample Rate 44100)
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 1024, 0)
        
        // 2. Tambahkan efek Pitch Shifting
        val pitchShifter = PitchShifter(pitchValue, 44100.0, 1024, 0)
        dispatcher?.addAudioProcessor(pitchShifter)
        
        // 3. Jalankan proses di thread terpisah agar HP tidak lag
        thread {
            dispatcher?.run()
        }
    }

    fun stopProcessing() {
        dispatcher?.stop()
    }
}

