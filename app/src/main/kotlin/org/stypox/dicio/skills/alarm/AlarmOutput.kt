package org.stypox.dicio.skills.alarm

import org.dicio.skill.context.SkillContext
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.util.TurkceSayi
import java.time.LocalDateTime
import java.util.Locale

class AlarmOutput(private val metin: String) : HeadlineSpeechSkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String = metin

    companion object {
        /** One short sentence, numbers in words, e.g. "Yarın sabah yedide uyandıracağım." */
        fun onayMetni(alarm: LocalDateTime, now: LocalDateTime): String {
            val gun = if (alarm.toLocalDate() == now.toLocalDate()) "" else "yarın "
            val kisim = when (alarm.hour) {
                in 5..11 -> "sabah "
                12 -> "öğlen "
                in 13..17 -> "öğleden sonra "
                in 18..20 -> "akşam "
                else -> "gece "
            }
            val saat = TurkceSayi.sayi(if (alarm.hour % 12 == 0) 12 else alarm.hour % 12)
            val zaman = when (alarm.minute) {
                0 -> TurkceSayi.bulunmaEki(saat)
                30 -> saat + " buçukta"
                else -> saat + " " + TurkceSayi.bulunmaEki(TurkceSayi.sayi(alarm.minute))
            }
            return (gun + kisim + zaman + " uyandıracağım.")
                .replaceFirstChar { it.titlecase(Locale("tr")) }
        }
    }
}
