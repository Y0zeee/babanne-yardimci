package org.stypox.dicio.skills.pil

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.sentences.Sentences.Pil

/** Battery level in percent from a (sticky) ACTION_BATTERY_CHANGED intent, or null. */
fun pilYuzdesi(intent: Intent?): Int? {
    val seviye = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: return null
    val olcek = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    if (seviye < 0 || olcek <= 0) return null
    return seviye * 100 / olcek
}

fun pilSarjda(intent: Intent?): Boolean =
    (intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0) != 0

class PilSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<Pil>) :
    StandardRecognizerSkill<Pil>(correspondingSkillInfo, data) {

    override suspend fun generateOutput(ctx: SkillContext, inputData: Pil): SkillOutput {
        val intent = ctx.android.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val yuzde = pilYuzdesi(intent)
        val metin = if (yuzde == null) "Şarjı öğrenemedim" else pilSeviyeCumlesi(yuzde)
        return PilOutput(metin)
    }
}

class PilOutput(private val metin: String) : HeadlineSpeechSkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String = metin
}
