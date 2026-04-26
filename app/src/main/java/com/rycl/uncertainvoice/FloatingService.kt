package com.rycl.uncertainvoice

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.*
import android.graphics.drawable.GradientDrawable
import android.graphics.Color

class FloatingService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var isMenuOpen = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // Buat View Utama (Bola Floating)
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)

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

        val rootBase = floatingView.findViewById<RelativeLayout>(R.id.root_container)
        val mainIcon = floatingView.findViewById<ImageView>(R.id.collapsed_iv)
        val menuLayout = floatingView.findViewById<LinearLayout>(R.id.menu_layout)

        // Logika Klik: Munculkan/Sembunyikan Menu
        mainIcon.setOnClickListener {
            isMenuOpen = !isMenuOpen
            menuLayout.visibility = if (isMenuOpen) View.VISIBLE else View.GONE
        }

        // Tombol-Tombol Menu
        floatingView.findViewById<ImageButton>(R.id.btn_record).setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) { /* Mulai Rekam */ }
            if (event.action == MotionEvent.ACTION_UP) { /* Berhenti & Proses */ }
            true
        }

        floatingView.findViewById<ImageButton>(R.id.btn_delete).setOnClickListener {
            Toast.makeText(this, "Rekaman Dibuang", Toast.LENGTH_SHORT).show()
        }

        // Dragging Logic (Biar bisa digeser)
        mainIcon.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0; private var initialY = 0
            private var initialTouchX = 0f; private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x; initialY = params.y
                        initialTouchX = event.rawX; initialTouchY = event.rawY
                        return false // Biar onClick tetep jalan
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatingView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }
}

