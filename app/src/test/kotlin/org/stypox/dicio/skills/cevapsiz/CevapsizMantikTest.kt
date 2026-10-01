package org.stypox.dicio.skills.cevapsiz

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.stypox.dicio.config.Kisi
import org.stypox.dicio.config.YardimciConfig

private const val SAAT = 3_600_000L
private const val SIMDI = 1_000_000_000_000L

private val config = YardimciConfig(
    kisiler = listOf(
        Kisi(listOf("oğlun", "ahmet"), "+90 555 000 00 01", null, false),
    ),
)

class CevapsizMantikTest : StringSpec({
    "calls older than 24 hours are dropped" {
        val liste = listOf(
            CevapsizArama("+905550000001", SIMDI - 2 * SAAT),
            CevapsizArama("+905550000001", SIMDI - 25 * SAAT),
        )
        cevapsizSon24Saat(liste, SIMDI).size shouldBe 1
    }

    "calls are grouped per contact and spoken with the alias" {
        val liste = listOf(
            CevapsizArama("+905550000001", SIMDI - SAAT),
            CevapsizArama("0555 000 00 01", SIMDI - 2 * SAAT),
        )
        val sonuc = cevapsizSonucu(liste, SIMDI, config)
        sonuc.cumle shouldBe "Bugün oğlun iki kere aradı"
        sonuc.aranacakNumara shouldBe "+90 555 000 00 01"
    }

    "no missed calls gives a short sentence and no call-back" {
        val sonuc = cevapsizSonucu(emptyList(), SIMDI, config)
        sonuc.cumle shouldBe "Cevapsız arama yok"
        sonuc.aranacakNumara shouldBe null
    }
})
