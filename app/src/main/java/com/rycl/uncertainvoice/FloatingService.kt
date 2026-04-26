package com.rycl.uncertainvoice

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.media.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.view.ContextThemeWrapper
import java.io.File
import java.io.FileOutputStream

class FloatingService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    private var recorder: AudioRecord? = null
    private var isRecording = false
    private var audioFile: File? = null
    private var currentPitch = 1.0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val themedContext = ContextThemeWrapper(this, androidx.appcompat.R.style.Theme_AppCompat_NoActionBar)
        floatingView = LayoutInflater.from(themedContext).inflate(R.layout.layout_floating_widget, null)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 100; y = 100 }

        val mainIcon = floatingView.findViewById<ImageView>(R.id.collapsed_iv)
        val menuLayout = floatingView.findViewById<LinearLayout>(R.id.menu_layout)
        
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
                        if (Math.abs(event.rawX - initialTouchX) < 10) 
                            menuLayout.visibility = if (menuLayout.visibility == View.GONE) View.VISIBLE else View.GONE
                        return true
                    }
                }
                return false
            }
        })

        // RECORD LOGIC
        floatingView.findViewById<ImageButton>(R.id.btn_record).setOnTouchListener { _, e ->
            when(e.action) {
                MotionEvent.ACTION_DOWN -> startRecording()
                MotionEvent.ACTION_UP -> stopRecording()
            }
            true
        }

        // FILTER BUTTONS (FIXED: Gak bakal close floating)
        floatingView.findViewById<ImageButton>(R.id.btn_voice_girl).setOnClickListener { 
            currentPitch = 1.8f 
            showMsg("Filter: Girl")
        }
        floatingView.findViewById<ImageButton>(R.id.btn_voice_alien).setOnClickListener { 
            currentPitch = 0.5f 
            showMsg("Filter: Alien")
        }
        floatingView.findViewById<ImageButton>(R.id.btn_play).setOnClickListener { playVoice() }
        floatingView.findViewById<ImageButton>(R.id.btn_delete).setOnClickListener { 
            audioFile?.delete()
            showMsg("Deleted")
        }

        windowManager.addView(floatingView, params)
    }

    private fun showMsg(txt: String) {
        Handler(Looper.getMainLooper()).post { Toast.makeText(this, txt, Toast.LENGTH_SHORT).show() }
    }

    private fun startRecording() {
        audioFile = File(externalCacheDir, "v.pcm")
        isRecording = true
        Thread {
            try {
                val bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                recorder = AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
                recorder?.startRecording()
                val os = FileOutputStream(audioFile)
                val data = ByteArray(bufferSize)
                while (isRecording) { recorder?.read(data, 0, bufferSize); os.write(data) }
                os.close()
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun stopRecording() { 
        isRecording = false
        try {
            recorder?.stop()
            recorder?.release()
            recorder = null 
        } catch (e: Exception) {}
    }

    private fun playVoice() {
        if (audioFile == null || !audioFile!!.exists()) return
        Thread {
            try {
                val data = audioFile!!.readBytes()
                val track = AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                    .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(44100).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(data.size).setTransferMode(AudioTrack.MODE_STATIC).build()
                track.write(data, 0, data.size)
                track.playbackParams = PlaybackParams().setPitch(currentPitch)
                track.play()
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    override fun onDestroy() { super.onDestroy(); if (::floatingView.isInitialized) windowManager.removeView(floatingView) }
}

