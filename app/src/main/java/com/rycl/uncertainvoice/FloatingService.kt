package com.rycl.uncertainvoice

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingBall: ImageView
    private var isRecording = false
    
    // Panggil class AudioProcessor yang sudah kita bahas sebelumnya
    private val audioProcessor = AudioProcessor()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 1. Buat tampilan bola (Pakai icon sistem dulu biar nggak error build)
        floatingBall = ImageView(this)
        floatingBall.setImageResource(android.R.drawable.ic_btn_speak_now) 

        // 2. Atur posisi dan tipe jendela (Overlay)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 100

        // 3. Fitur Geser & Klik
        floatingBall.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingBall, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val diffX = Math.abs(event.rawX - initialTouchX)
                        val diffY = Math.abs(event.rawY - initialTouchY)

                        // Jika jarak geser kecil, dianggap KLIK
                        if (diffX < 10 && diffY < 10) {
                            toggleVoiceChanger()
                        }
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatingBall, params)
    }

    private fun toggleVoiceChanger() {
        if (!isRecording) {
            // Mulai rekam & ubah suara (Contoh: Pitch 2.0 = Suara Chipmunk)
            try {
                audioProcessor.startChangingVoice(2.0)
                isRecording = true
                floatingBall.alpha = 0.5f // Tandanya lagi aktif
                Toast.makeText(this, "Voice Changer Aktif!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal akses Mic!", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Berhenti
            audioProcessor.stopProcessing()
            isRecording = false
            floatingBall.alpha = 1.0f
            Toast.makeText(this, "Voice Changer Mati", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioProcessor.stopProcessing()
        if (::floatingBall.isInitialized) windowManager.removeView(floatingBall)
    }
}
