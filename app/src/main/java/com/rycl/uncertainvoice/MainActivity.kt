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
        
        // Bikin tombol sederhana buat aktifin bola
        val btnStart = Button(this).apply {
            text = "Aktifkan Uncertain Voice"
        }
        setContentView(btnStart)

        btnStart.setOnClickListener {
            checkOverlayPermission()
        }
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // Kirim user ke pengaturan buat aktifin izin overlay
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
        finish() // Tutup aplikasi utama, biarkan bola melayang sendiri
    }
}

