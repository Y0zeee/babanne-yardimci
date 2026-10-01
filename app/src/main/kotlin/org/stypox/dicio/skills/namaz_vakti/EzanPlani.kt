package org.stypox.dicio.skills.namaz_vakti

import java.time.LocalDate
import java.time.LocalDateTime

const val EZAN_DOSYASI = "/sdcard/Yardimci/ezan.mp3"

data class SonrakiEzan(val ad: String, val zaman: LocalDateTime)

enum class EzanEylemi { OYNAT, SOYLE }

/** Pure logic that picks the next ezan (güneş has none) and what to do when it is time. */
object EzanPlani {
    private fun gunun(tarih: LocalDate, v: Vakitler) = listOf(
        SonrakiEzan("Sabah", tarih.atTime(v.imsak)),
        SonrakiEzan("Öğle", tarih.atTime(v.ogle)),
        SonrakiEzan("İkindi", tarih.atTime(v.ikindi)),
        SonrakiEzan("Akşam", tarih.atTime(v.aksam)),
        SonrakiEzan("Yatsı", tarih.atTime(v.yatsi)),
    )

    /**
     * First ezan strictly after [simdi] among today's times, else tomorrow's imsak
     * ([yarin] are tomorrow's times).
     */
    fun sonraki(bugun: Vakitler, simdi: LocalDateTime, yarin: Vakitler): SonrakiEzan {
        val tarih = simdi.toLocalDate()
        return gunun(tarih, bugun).firstOrNull { it.zaman.isAfter(simdi) }
            ?: gunun(tarih.plusDays(1), yarin).first()
    }

    fun ezanCumlesi(ad: String) = "$ad ezanı okunuyor."

    fun ezanEylemi(ezanSesi: Boolean, dosyaVar: Boolean) =
        if (ezanSesi && dosyaVar) EzanEylemi.OYNAT else EzanEylemi.SOYLE
}
