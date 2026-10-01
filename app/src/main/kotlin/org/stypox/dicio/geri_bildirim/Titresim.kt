package org.stypox.dicio.geri_bildirim

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object Titresim {
    /** Vibrates with the pattern for [geriBildirim]; does nothing if there is no vibrator. */
    fun yap(context: Context, geriBildirim: GeriBildirim) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(VibratorManager::class.java)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator == null || !vibrator.hasVibrator()) return
            vibrator.vibrate(
                VibrationEffect.createWaveform(TitresimDeseni.sec(geriBildirim), -1)
            )
        } catch (e: Exception) {
            // feedback is optional, never let it break the interaction
        }
    }
}
