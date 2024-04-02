package com.example.appwidget

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.ComponentActivity
import com.example.appwidget.databinding.ActivityTtsBinding
import java.util.Locale

class TTS : ComponentActivity() {

    private lateinit var B: ActivityTtsBinding
    private lateinit var textToSpeech: TextToSpeech
    private var pitch = 1.0f
    private var speakRate = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        B = ActivityTtsBinding.inflate(layoutInflater)
        setContentView(B.root)

        B.pitchSeekbar.progress = (pitch * 10f).toInt()
        B.speakRateSeekbar.progress = (speakRate * 10f).toInt()

        val onSeekBarChangeListener = object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (seekBar) {
                    B.pitchSeekbar -> {
                        pitch = progress.toFloat() / 10f
                        textToSpeech.setPitch(pitch)
                    }

                    B.speakRateSeekbar -> {
                        speakRate = progress.toFloat() / 10f
                        textToSpeech.setSpeechRate(speakRate)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        }

        B.speakRateSeekbar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        B.pitchSeekbar.setOnSeekBarChangeListener(onSeekBarChangeListener)

        textToSpeech = TextToSpeech(
            this
        ) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.CHINESE
                textToSpeech.engines.forEach { info ->
                    {
                        info.name
                    }
                }
                textToSpeech.setPitch(pitch)
                textToSpeech.setSpeechRate(speakRate)
            }
        }


        B.playStopBtn.setOnClickListener {
            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()
            } else {
                textToSpeech.speak(
                    B.ttsEdittext.text.toString(),
                    TextToSpeech.QUEUE_FLUSH,
                    Bundle.EMPTY,
                    null
                )
            }
        }


        B.createShortcut.setOnClickListener {
            val shortService = getSystemService(ShortcutManager::class.java)

            if (shortService.isRequestPinShortcutSupported) {
                val pinShortcutInfo = ShortcutInfo.Builder(this, "TTS")
                    .setIntent(
                        Intent(
                            applicationContext,
                            TTS::class.java
                        ).setAction(Intent.ACTION_VIEW)
                    )
                    .setShortLabel("TTs")
                    .setIcon(Icon.createWithResource(applicationContext, R.mipmap.ic_launcher))
                    .build()


                shortService.requestPinShortcut(pinShortcutInfo, null)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}