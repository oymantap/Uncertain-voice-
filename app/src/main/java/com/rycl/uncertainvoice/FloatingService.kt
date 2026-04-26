package com.rycl.uncertainvoice

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.media.*
import android.os.IBinder
import android.view.*
import android.widget.*
import java.io.File
import java.io.FileOutputStream

class FloatingService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var floatingView: View
    private var recorder: AudioRecord? = null
    private var isRecording = false
    private var audioFile: File? = null
    private var currentPitch = 1.0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100; y = 100
        }

        val mainIcon = floatingView.findViewById<ImageView>(R.id.collapsed_iv)
        val menuLayout = floatingView.findViewById<LinearLayout>(R.id.menu_layout)
        
        // FITUR GESER
        mainIcon.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0; private var initialY = 0
            private var initialTouchX = 0f; private var initialTouchY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x; initialY = params.y
                        initialTouchX = event.rawX; initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (Math.abs(event.rawX - initialTouchX) < 10) v.performClick()
                        return true
                    }
                }
                return false
            }
        })

        mainIcon.setOnClickListener { 
            menuLayout.visibility = if (menuLayout.visibility == View.GONE) View.VISIBLE else View.GONE 
        }

        // TUTUP MENU DOANG
        floatingView.findViewById<ImageButton>(R.id.btn_close_menu).setOnClickListener { 
            menuLayout.visibility = View.GONE 
        }

        // TAHAN RECORD, LEPAS SIMPAN
        floatingView.findViewById<ImageButton>(R.id.btn_record).setOnTouchListener { _, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> startRecording()
                MotionEvent.ACTION_UP -> stopRecording()
            }
            true
        }

        // HAPUS (TONG SAMPAH)
        floatingView.findViewById<ImageButton>(R.id.btn_delete).setOnClickListener {
            audioFile?.delete()
            Toast.makeText(this, "Voice Purged!", Toast.LENGTH_SHORT).show()
        }

        // PILIHAN SUARA (CEWE & ALIEN)
        floatingView.findViewById<ImageButton>(R.id.btn_voice_girl).setOnClickListener {
            currentPitch = 1.8f
            Toast.makeText(this, "Mode: Girl / Child", Toast.LENGTH_SHORT).show()
        }
        floatingView.findViewById<ImageButton>(R.id.btn_voice_alien).setOnClickListener {
            currentPitch = 0.5f
            Toast.makeText(this, "Mode: Alien / Robot", Toast.LENGTH_SHORT).show()
        }

        // PLAY REAL VOICE CHANGER
        floatingView.findViewById<ImageButton>(R.id.btn_play).setOnClickListener { playVoice() }

        windowManager.addView(floatingView, params)
    }

    private fun startRecording() {
        audioFile = File(externalCacheDir, "raw_voice.pcm")
        isRecording = true
        Thread {
            val bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            recorder = AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
            recorder?.startRecording()
            val os = FileOutputStream(audioFile)
            val data = ByteArray(bufferSize)
            while (isRecording) {
                recorder?.read(data, 0, bufferSize)
                os.write(data)
            }
            os.close()
        }.start()
        Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        isRecording = false
        recorder?.stop(); recorder?.release(); recorder = null
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
    }

    private fun playVoice() {
        if (audioFile == null || !audioFile!!.exists()) return
        val data = audioFile!!.readBytes()
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(44100).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(data.size).setTransferMode(AudioTrack.MODE_STATIC).build()
        
        track.write(data, 0, data.size)
        track.playbackParams = PlaybackParams().setPitch(currentPitch)
        track.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }
}
