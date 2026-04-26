package com.rycl.uncertainvoice

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnActivate = findViewById<Button>(R.id.btn_activate)
        val btnStop = findViewById<Button>(R.id.btn_stop_service)
        val statusEngine = findViewById<TextView>(R.id.status_engine)

        btnActivate.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, 123)
            } else {
                startFloatingService()
            }
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, FloatingService::class.java))
            btnStop.visibility = View.GONE
            btnActivate.visibility = View.VISIBLE
            statusEngine.text = "● ENGINE STATUS: READY"
            statusEngine.setTextColor(android.graphics.Color.GREEN)
        }
    }

    private fun startFloatingService() {
        startService(Intent(this, FloatingService::class.java))
        findViewById<Button>(R.id.btn_activate).visibility = View.GONE
        findViewById<Button>(R.id.btn_stop_service).visibility = View.VISIBLE
        findViewById<TextView>(R.id.status_engine).text = "● ENGINE STATUS: ACTIVE"
        findViewById<TextView>(R.id.status_engine).setTextColor(android.graphics.Color.CYAN)
        Toast.makeText(this, "Node Active!", Toast.LENGTH_SHORT).show()
    }
}
