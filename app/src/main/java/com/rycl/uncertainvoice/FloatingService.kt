package com.rycl.uncertainvoice

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.*
import java.io.File

class FloatingService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var isRecording = false
    private var audioFile: File? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // Memanggil layout yang sudah kita perbaiki tadi
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        // Deklarasi ID yang ADA di XML
        val menuLayout = floatingView.findViewById<LinearLayout>(R.id.menu_layout)
        val btnRecord = floatingView.findViewById<ImageButton>(R.id.btn_record)
        val btnPlay = floatingView.findViewById<ImageButton>(R.id.btn_play)
        val btnClose = floatingView.findViewById<ImageButton>(R.id.btn_close)
        val mainIcon = floatingView.findViewById<ImageView>(R.id.collapsed_iv)

        // 1. Klik icon untuk buka/tutup menu
        mainIcon.setOnClickListener {
            menuLayout.visibility = if (menuLayout.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // 2. Logika Rekam (Simple Toast dulu biar gak error)
        btnRecord.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isRecording = true
                    Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show()
                }
                MotionEvent.ACTION_UP -> {
                    isRecording = false
                    Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }

        // 3. Tombol Play
        btnPlay.setOnClickListener {
            Toast.makeText(this, "Playing Voice...", Toast.LENGTH_SHORT).show()
        }

        // 4. Tombol Matikan (Close)
        btnClose.setOnClickListener {
            stopSelf()
        }

        windowManager.addView(floatingView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
