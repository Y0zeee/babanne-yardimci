package org.stypox.dicio.skills.tarih

import android.icu.util.IslamicCalendar
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.sentences.Sentences.Tarih
import org.stypox.dicio.util.TurkceSayi
import java.time.LocalDate
import java.util.GregorianCalendar

private val GUNLER = listOf(
    "pazartesi", "salı", "çarşamba", "perşembe", "cuma", "cumartesi", "pazar",
)
private val AYLAR = listOf(
    "ocak", "şubat", "mart", "nisan", "mayıs", "haziran",
    "temmuz", "ağustos", "eylül", "ekim", "kasım", "aralık",
)
private val HICRI_AYLAR = listOf(
    "muharrem", "safer", "rebiülevvel", "rebiülahir", "cemaziyelevvel", "cemaziyelahir",
    "recep", "şaban", "ramazan", "şevval", "zilkade", "zilhicce",
)

class TarihSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<Tarih>) :
    StandardRecognizerSkill<Tarih>(correspondingSkillInfo, data) {

    override suspend fun generateOutput(ctx: SkillContext, inputData: Tarih): SkillOutput {
        val bugun = LocalDate.now()
        val metin = when (inputData) {
            is Tarih.Gun -> "Bugün ${gun(bugun)}."
            is Tarih.Miladi -> "Bugün ${miladi(bugun)}."
            is Tarih.Hicri -> "Bugün hicri ${hicri(bugun)}."
        }
        return TarihOutput(metin)
    }

    private fun gun(d: LocalDate) = GUNLER[d.dayOfWeek.value - 1]

    private fun miladi(d: LocalDate) =
        "${TurkceSayi.sayi(d.dayOfMonth)} ${AYLAR[d.monthValue - 1]} " +
            "${TurkceSayi.sayi(d.year)} ${gun(d)}"

    /**
     * Hijri date from the ICU Islamic calendar. It is calculated, so it may be ±1 day off the
     * Diyanet calendar.
     */
    private fun hicri(d: LocalDate): String {
        val cal = IslamicCalendar()
        cal.timeInMillis = GregorianCalendar(d.year, d.monthValue - 1, d.dayOfMonth, 12, 0).timeInMillis
        return "${TurkceSayi.sayi(cal.get(IslamicCalendar.DAY_OF_MONTH))} " +
            "${HICRI_AYLAR[cal.get(IslamicCalendar.MONTH)]} " +
            TurkceSayi.sayi(cal.get(IslamicCalendar.YEAR))
    }
}
