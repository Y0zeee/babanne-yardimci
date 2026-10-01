package org.stypox.dicio.skills.namaz_vakti

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private val BUGUN = LocalDate.of(2026, 10, 2)
private val YARIN = BUGUN.plusDays(1)

private val VAKITLER = Vakitler(
    imsak = LocalTime.of(5, 16),
    gunes = LocalTime.of(6, 39),
    ogle = LocalTime.of(12, 43),
    ikindi = LocalTime.of(16, 1),
    aksam = LocalTime.of(18, 38),
    yatsi = LocalTime.of(19, 55),
)

// Tomorrow's times differ slightly; the rollover must use these, not today's.
private val YARIN_VAKITLER = VAKITLER.copy(imsak = LocalTime.of(5, 17))

private fun sonraki(simdi: LocalDateTime) =
    EzanPlani.sonraki(VAKITLER, simdi, YARIN_VAKITLER)

/**
 * Assumed API (EzanPlani, package org.stypox.dicio.skills.namaz_vakti):
 *  - `EzanPlani.sonraki(bugun: Vakitler, simdi: LocalDateTime, yarin: Vakitler): SonrakiEzan`
 *    with `SonrakiEzan(ad: String, zaman: LocalDateTime)`; the next ezan is strictly after [simdi].
 *  - `EzanPlani.ezanCumlesi(ad: String): String`
 *  - `EzanPlani.ezanEylemi(ezanSesi: Boolean, dosyaVar: Boolean): EzanEylemi` (OYNAT / SOYLE)
 */
class EzanPlaniTest : StringSpec({
    "imsaktan once siradaki Sabah" {
        val s = sonraki(BUGUN.atTime(3, 0))
        s.ad shouldBe "Sabah"
        s.zaman shouldBe BUGUN.atTime(5, 16)
    }

    "imsak ile ogle arasinda gunes atlanir, Ogle gelir" {
        val s = sonraki(BUGUN.atTime(6, 39))
        s.ad shouldBe "Öğle"
        s.zaman shouldBe BUGUN.atTime(12, 43)
    }

    "ogle ile ikindi arasinda Ikindi gelir" {
        val s = sonraki(BUGUN.atTime(14, 0))
        s.ad shouldBe "İkindi"
        s.zaman shouldBe BUGUN.atTime(16, 1)
    }

    "tam ezan vaktinde siradaki bir sonraki ezandir" {
        val s = sonraki(BUGUN.atTime(12, 43))
        s.ad shouldBe "İkindi"
        s.zaman shouldBe BUGUN.atTime(16, 1)
    }

    "aksam ile yatsi arasinda Yatsi gelir" {
        val s = sonraki(BUGUN.atTime(19, 0))
        s.ad shouldBe "Yatsı"
        s.zaman shouldBe BUGUN.atTime(19, 55)
    }

    "yatsidan sonra yarinin imsagi (Sabah)" {
        val s = sonraki(BUGUN.atTime(19, 55))
        s.ad shouldBe "Sabah"
        s.zaman shouldBe YARIN.atTime(5, 17)
    }

    "5 ezanin cumlesi" {
        EzanPlani.ezanCumlesi("Sabah") shouldBe "Sabah ezanı okunuyor."
        EzanPlani.ezanCumlesi("Öğle") shouldBe "Öğle ezanı okunuyor."
        EzanPlani.ezanCumlesi("İkindi") shouldBe "İkindi ezanı okunuyor."
        EzanPlani.ezanCumlesi("Akşam") shouldBe "Akşam ezanı okunuyor."
        EzanPlani.ezanCumlesi("Yatsı") shouldBe "Yatsı ezanı okunuyor."
    }

    "ezanSesi acik ve dosya varsa OYNAT" {
        EzanPlani.ezanEylemi(ezanSesi = true, dosyaVar = true) shouldBe EzanEylemi.OYNAT
    }

    "ezanSesi kapali veya dosya yoksa SOYLE" {
        EzanPlani.ezanEylemi(ezanSesi = false, dosyaVar = true) shouldBe EzanEylemi.SOYLE
        EzanPlani.ezanEylemi(ezanSesi = true, dosyaVar = false) shouldBe EzanEylemi.SOYLE
        EzanPlani.ezanEylemi(ezanSesi = false, dosyaVar = false) shouldBe EzanEylemi.SOYLE
    }
})
