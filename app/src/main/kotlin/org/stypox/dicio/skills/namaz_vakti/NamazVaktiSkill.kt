package org.stypox.dicio.skills.namaz_vakti

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.config.YardimciConfig
import org.stypox.dicio.sentences.Sentences.NamazVakti
import org.stypox.dicio.util.TurkceSayi
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

class NamazVaktiSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<NamazVakti>) :
    StandardRecognizerSkill<NamazVakti>(correspondingSkillInfo, data) {

    private class Vakit(val ad: String, val belirtec: String, val saat: (Vakitler) -> LocalTime)

    private val vakitler = listOf(
        Vakit("İmsak", "imsak") { it.imsak },
        Vakit("Güneş", "güneş") { it.gunes },
        Vakit("Öğle ezanı", "öğle") { it.ogle },
        Vakit("İkindi ezanı", "ikindi") { it.ikindi },
        Vakit("Akşam ezanı", "akşam") { it.aksam },
        Vakit("Yatsı ezanı", "yatsı") { it.yatsi },
    )

    override suspend fun generateOutput(ctx: SkillContext, inputData: NamazVakti): SkillOutput {
        val konum = KonumKaynagi.bul(ctx.android, YardimciConfig.load(ctx.android))
            ?: return NamazVaktiOutput(NamazVaktiFormat.konumYok())

        val simdi = LocalDateTime.now()
        val bugunVakitler = hesapla(konum, simdi.toLocalDate())

        val metin = when (inputData) {
            is NamazVakti.Vakit -> {
                val v = bul(inputData.ad)
                if (v == null) {
                    siradaki(konum, simdi)
                } else {
                    NamazVaktiFormat.vakitCumlesi(v.ad, v.saat(bugunVakitler), simdi.toLocalTime())
                }
            }
            is NamazVakti.Kalan -> {
                val v = bul(inputData.ad)
                if (v == null) {
                    siradaki(konum, simdi)
                } else {
                    val dk = Duration.between(simdi.toLocalTime(), v.saat(bugunVakitler)).toMinutes()
                    NamazVaktiFormat.kalanCumlesi(v.ad, dk.toInt())
                }
            }
            is NamazVakti.Siradaki -> siradaki(konum, simdi)
            is NamazVakti.Hepsi -> vakitler.joinToString(" ") {
                "${it.ad} ${TurkceSayi.bulunmaEki(TurkceSayi.saat(it.saat(bugunVakitler)))}."
            }
        }
        return NamazVaktiOutput(metin)
    }

    private fun bul(soz: String?): Vakit? {
        val s = soz?.trim()?.lowercase(Locale.forLanguageTag("tr")) ?: return null
        return vakitler.firstOrNull { s.startsWith(it.belirtec.take(4)) }
    }

    private fun hesapla(konum: Konum, gun: LocalDate): Vakitler {
        val utc = ZoneId.systemDefault().rules.getOffset(gun.atTime(12, 0)).totalSeconds / 60
        return NamazHesap.hesapla(konum.enlem, konum.boylam, gun, utc)
    }

    /** The next prayer time from now (tomorrow's imsak after yatsı). */
    private fun siradaki(konum: Konum, simdi: LocalDateTime): String {
        val bugun = hesapla(konum, simdi.toLocalDate())
        val t = simdi.toLocalTime().withSecond(0).withNano(0)
        vakitler.firstOrNull { it.saat(bugun) > t }?.let {
            return NamazVaktiFormat.vakitCumlesi(it.ad, it.saat(bugun), t)
        }
        val yarin = hesapla(konum, simdi.toLocalDate().plusDays(1))
        val dk = 24 * 60 - t.toSecondOfDay() / 60 + yarin.imsak.toSecondOfDay() / 60
        val imsak = "İmsak ${TurkceSayi.bulunmaEki(TurkceSayi.saat(yarin.imsak))}."
        return "$imsak ${NamazVaktiFormat.kalanYazi(dk)} var."
    }
}
