package com.rycl.uncertainvoice

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
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
                startService(Intent(this, FloatingService::class.java))
                btnActivate.visibility = View.GONE
                btnStop.visibility = View.VISIBLE
                statusEngine.text = "● ENGINE STATUS: ACTIVE"
                statusEngine.setTextColor(android.graphics.Color.CYAN)
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
}
