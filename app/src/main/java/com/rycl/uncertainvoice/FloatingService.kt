package com.rycl.uncertainvoice

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.media.*
import android.os.IBinder
import android.view.*
import android.widget.*
import java.io.File
import kotlin.concurrent.thread

class FloatingService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var isRecording = false
    private var audioFile: File? = null
    private var pitchValue = 1.0f // Normal

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100; y = 100
        }

        val menuLayout = floatingView.findViewById<LinearLayout>(R.id.menu_layout)
        val btnRecord = floatingView.findViewById<ImageButton>(R.id.btn_record)
        val btnPlay = floatingView.findViewById<ImageButton>(R.id.btn_play)
        val btnClose = floatingView.findViewById<ImageButton>(R.id.btn_close) // Tambahin di XML nanti
        val spinnerPitch = floatingView.findViewById<Spinner>(R.id.spinner_pitch) // Tambahin di XML nanti

        // 1. Logika Klik Muncul Menu
        floatingView.findViewById<ImageView>(R.id.collapsed_iv).setOnClickListener {
            menuLayout.visibility = if (menuLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // 2. Logika Rekam (Tahan untuk Rekam)
        btnRecord.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startRecording()
                MotionEvent.ACTION_UP -> stopRecording()
            }
            true
        }

        // 3. Logika Play (Ubah Suara di Sini)
        btnPlay.setOnClickListener { playVoice() }

        // 4. Tombol Matikan Floating
        btnClose.setOnClickListener { stopSelf() }

        windowManager.addView(floatingView, params)
    }

    private fun startRecording() {
        isRecording = true
        audioFile = File(externalCacheDir, "temp_voice.pcm")
        Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show()
        // Di sini harusnya ada fungsi nulis byte ke file (logic AudioRecord)
    }

    private fun stopRecording() {
        isRecording = false
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
    }

    private fun playVoice() {
        // Logika AudioTrack dengan pitchValue
        // Semakin tinggi pitch, suara makin kayak Chipmunk
        Toast.makeText(this, "Playing with Pitch: $pitchValue", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }
}
