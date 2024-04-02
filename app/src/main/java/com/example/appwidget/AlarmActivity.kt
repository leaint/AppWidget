package com.example.appwidget

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import com.example.appwidget.databinding.ActivityAlarmBinding

class AlarmActivity : ComponentActivity() {
    private lateinit var B: ActivityAlarmBinding
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        B = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(B.root)


        val myDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {

                if (velocityY > 100) {
                    finish()
                }
                return true
            }
        })

        B.textView2.setOnTouchListener { v, event ->
            run {
                if (myDetector.onTouchEvent(event)) {
                    finish()
                    true
                } else {
                    false
                }
            }
        }


        val message = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE)
        if (message != null) {
            B.textView2.setText("\n\n\n\n$message\n\n\nClick")
        }
    }


    override fun onStart() {
        super.onStart()
        val ringtone = intent.getStringExtra(AlarmClock.EXTRA_RINGTONE)
        if (ringtone != null) {
            mediaPlayer?.release()
            mediaPlayer = null
            var maxCount = 5
            mediaPlayer = MediaPlayer.create(this, Uri.parse(ringtone)).apply {
                setOnCompletionListener { _ ->
                    if (maxCount-- < 0) {
                        mediaPlayer?.stop()

                    }
                }
                isLooping = true
                setAudioStreamType(AudioManager.STREAM_MUSIC)
//                prepare()
                start()
            }

        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}