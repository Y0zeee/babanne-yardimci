package org.stypox.dicio.skills.namaz_vakti

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs

private val TARIH = LocalDate.of(2026, 10, 2)
private const val UTC_3 = 180 // Turkey: UTC+3 all year

private data class Sehir(val ad: String, val enlem: Double, val boylam: Double)

private val ANKARA = Sehir("Ankara", 39.93, 32.86)
private val ISTANBUL = Sehir("Istanbul", 41.01, 28.98)
private val DIYARBAKIR = Sehir("Diyarbakir", 37.91, 40.24)

/**
 * Diyanet-published times (HH:MM: imsak, gunes, ogle, ikindi, aksam, yatsi) per city for [TARIH].
 *
 * TODO(verify): fill these from https://namazvakitleri.diyanet.gov.tr/ (record the URL and the
 * date of lookup here). They could not be fetched while writing this test (no network), so they
 * are intentionally left empty rather than guessed; the Diyanet comparison below is disabled
 * until they are filled in.
 */
private val DIYANET: Map<Sehir, List<String>> = emptyMap()

private fun LocalTime.dk() = hour * 60 + minute

class NamazHesapTest : StringSpec({
    "computed times match Diyanet within 2 minutes".config(enabled = DIYANET.isNotEmpty()) {
        for ((sehir, beklenen) in DIYANET) {
            val v = NamazHesap.hesapla(sehir.enlem, sehir.boylam, TARIH, UTC_3)
            val hesap = listOf(v.imsak, v.gunes, v.ogle, v.ikindi, v.aksam, v.yatsi)
            hesap.zip(beklenen).forEach { (h, b) ->
                (abs(h.dk() - LocalTime.parse(b).dk()) <= 2) shouldBe true
            }
        }
    }

    "six times are ordered and noon is near solar noon for each city" {
        // solar noon (UTC+3, early October, equation of time ~ +10 min), plus a few temkin minutes
        val ogleAraligi = mapOf(
            ANKARA to (12 * 60 + 30..12 * 60 + 55),
            ISTANBUL to (12 * 60 + 40..13 * 60 + 10),
            DIYARBAKIR to (12 * 60 + 0..12 * 60 + 25),
        )
        for ((sehir, aralik) in ogleAraligi) {
            val v = NamazHesap.hesapla(sehir.enlem, sehir.boylam, TARIH, UTC_3)
            val l = listOf(v.imsak, v.gunes, v.ogle, v.ikindi, v.aksam, v.yatsi).map { it.dk() }
            l.zipWithNext().all { (a, b) -> a < b } shouldBe true
            v.ogle.dk() shouldBeInRange aralik
        }
    }
})
