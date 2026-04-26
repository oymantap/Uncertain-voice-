package com.rycl.uncertainvoice

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Panggil layout XML yang sudah kita buat tadi
        setContentView(R.layout.activity_main)

        // 2. Hubungkan tombol dengan ID yang ada di XML
        val btnStart = findViewById<Button>(R.id.btn_start_service)

        btnStart.setOnClickListener {
            checkOverlayPermission()
        }
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Izinkan Overlay dulu, Rycl!", Toast.LENGTH_SHORT).show()
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, 123)
            } else {
                startFloatingService()
            }
        } else {
            startFloatingService()
        }
    }

    private fun startFloatingService() {
        startService(Intent(this, FloatingService::class.java))
        // Jangan langsung finish() dulu biar user bisa baca tutorial atau klik sosmed kamu
        Toast.makeText(this, "Floating Active!", Toast.LENGTH_SHORT).show()
    }
}
