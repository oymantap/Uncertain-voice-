package com.rycl.uncertainvoice

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import android.view.MotionEvent
import android.view.View

// Tambahkan variabel di atas class FloatingService
private var isRecording = false
private val audioProcessor = AudioProcessor()

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingBall: ImageView

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 1. Buat tampilan bola (Logo kamu)
        floatingBall = ImageView(this)
        floatingBall.setImageResource(R.drawable.logo_kamu) // Ganti dengan logo di folder res/drawable

        // 2. Atur posisi dan tipe jendela (Overlay)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Izin tampil di atas app lain
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 100

        // 3. Tambahkan fitur geser (Drag) agar bola bisa dipindah
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
                    // Di dalam setOnTouchListener, bagian ACTION_UP:
MotionEvent.ACTION_UP -> {
    val diffX = (event.rawX - initialTouchX).toInt()
    val diffY = (event.rawY - initialTouchY).toInt()

    // Jika user cuma klik (bukan geser)
    if (diffX < 10 && diffY < 10) {
        if (!isRecording) {
            // Mulai rekam & ubah suara (Contoh: Pitch 1.5 buat suara anak kecil)
            audioProcessor.startChangingVoice(1.5)
            floatingBall.alpha = 0.5f // Kasih efek transparan pas lagi rekam
            isRecording = true
        } else {
            // Berhenti
            audioProcessor.stopProcessing()
            floatingBall.alpha = 1.0f
            isRecording = false
        }
    }
    return true
}
                }
                return false
            }
        })

        windowManager.addView(floatingBall, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingBall.isInitialized) windowManager.removeView(floatingBall)
    }
}

