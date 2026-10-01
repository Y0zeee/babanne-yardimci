package org.stypox.dicio.skills.pil

import android.content.Context
import android.speech.tts.TextToSpeech
import org.stypox.dicio.config.YardimciConfig
import java.util.Locale

/** Speaks short background alerts with the system text-to-speech. */
class PilKonusma(context: Context) {
    private var hazir = false
    private var tts: TextToSpeech? = null

    init {
        val hiz = YardimciConfig.load(context).ttsHizi
        tts = TextToSpeech(context.applicationContext) { durum ->
            if (durum == TextToSpeech.SUCCESS) {
                tts?.language = Locale.forLanguageTag("tr")
                tts?.setSpeechRate(hiz)
                hazir = true
            }
        }
    }

    fun soyle(metin: String) {
        if (hazir) tts?.speak(metin, TextToSpeech.QUEUE_ADD, null, "pil")
    }

    fun kapat() {
        tts?.shutdown()
        tts = null
        hazir = false
    }
}
