package org.stypox.dicio.skills.namaz_vakti

import org.dicio.skill.context.SkillContext
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.util.TurkceSayi
import java.time.Duration
import java.time.LocalTime

/** Builds the Turkish sentences spoken by the namaz_vakti skill; times are spelled in words. */
object NamazVaktiFormat {
    fun konumYok() = "Konumu bulamadım."

    /** E.g. "Öğle ezanı on iki otuz ikide. Bir saat on dakika var." */
    fun vakitCumlesi(ad: String, vakit: LocalTime, simdi: LocalTime): String {
        val vakitYazi = "$ad ${TurkceSayi.bulunmaEki(TurkceSayi.saat(vakit))}."
        val kalan = Duration.between(simdi.withSecond(0).withNano(0), vakit).toMinutes()
        return when {
            kalan < 0 -> vakitYazi
            kalan == 0L -> "$vakitYazi Vakit girdi."
            else -> "$vakitYazi ${kalanYazi(kalan.toInt())} var."
        }
    }

    fun kalanCumlesi(ad: String, kalanDakika: Int): String =
        if (kalanDakika <= 0) "$ad vakti girdi." else "${kalanYazi(kalanDakika)} var."

    fun kalanYazi(dakika: Int): String {
        val saat = dakika / 60
        val dk = dakika % 60
        val parts = ArrayList<String>()
        if (saat > 0) parts.add("${TurkceSayi.sayi(saat)} saat")
        if (dk > 0) parts.add("${TurkceSayi.sayi(dk)} dakika")
        return parts.joinToString(" ").replaceFirstChar { it.uppercase() }
    }
}

class NamazVaktiOutput(private val metin: String) : HeadlineSpeechSkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String = metin
}
