package com.rycl.uncertainvoice

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val translations = mapOf(
        "en" to listOf("ENGINE STATUS: READY", "ACTIVATE FLOATING NODE", "SUBSCRIBE", "SYSTEM_GUIDE:", "> Click Activate\n> Grant Overlay\n> Record Voice\n> Select Neon Filter"),
        "id" to listOf("STATUS MESIN: SIAP", "AKTIFKAN NODE MELAYANG", "SUBSCRIBE", "PANDUAN_SISTEM:", "> Klik Aktifkan\n> Berikan Izin Overlay\n> Rekam Suara\n> Pilih Filter Neon"),
        "cn" to listOf("引擎状态: 就绪", "激活悬浮节点", "订阅", "系统指南:", "> 点击上方激活按钮\n> 授予悬浮窗权限\n> 点击悬浮图标录音\n> 选择霓虹滤镜")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnActivate = findViewById<Button>(R.id.btn_activate)
        val btnStop = findViewById<Button>(R.id.btn_stop_service)
        val statusEngine = findViewById<TextView>(R.id.status_engine)
        val txtYt = findViewById<TextView>(R.id.txt_yt)
        val guideTitle = findViewById<TextView>(R.id.guide_title)
        val guideSteps = findViewById<TextView>(R.id.guide_steps)

        fun switchLang(lang: String) {
            val data = translations[lang] ?: return
            statusEngine.text = data[0]
            btnActivate.text = data[1]
            txtYt.text = data[2]
            guideTitle.text = data[3]
            guideSteps.text = data[4]
        }

        // Language Click Listeners
        findViewById<Button>(R.id.lang_en).setOnClickListener { switchLang("en") }
        findViewById<Button>(R.id.lang_id).setOnClickListener { switchLang("id") }
        findViewById<Button>(R.id.lang_cn).setOnClickListener { switchLang("cn") }

        btnActivate.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, 123)
            } else {
                startService(Intent(this, FloatingService::class.java))
                btnActivate.visibility = View.GONE
                btnStop.visibility = View.VISIBLE
                statusEngine.text = "● ENGINE ACTIVE"
                statusEngine.setTextColor(Color.CYAN)
            }
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, FloatingService::class.java))
            btnStop.visibility = View.GONE
            btnActivate.visibility = View.VISIBLE
            statusEngine.text = "● ENGINE READY"
            statusEngine.setTextColor(Color.parseColor("#39FF14"))
        }
    }
}
