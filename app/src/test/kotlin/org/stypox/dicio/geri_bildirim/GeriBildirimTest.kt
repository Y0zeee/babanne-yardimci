package org.stypox.dicio.geri_bildirim

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.stypox.dicio.config.Kisi

/**
 * Expected API (pure Kotlin, no Android deps):
 *  - `enum GeriBildirim { LISTENING, UNDERSTOOD, NOT_UNDERSTOOD }`
 *  - `TitresimDeseni.sec(GeriBildirim): LongArray` (alternating pause/vibrate ms, starts with 0)
 *  - `AnlamamaSayaci` immutable: `basarisiz()`, `basarili()`, `adim(acilKisi: Kisi?): AnlamamaAdimi`
 *  - `AnlamamaAdimi`: `TekrarIste`, `AramaMiVakitMi`, `KisiyiAra(kisi: Kisi)`
 */
class GeriBildirimTest : StringSpec({
    val kisi = Kisi(
        adlar = listOf("Örnek Kişi"),
        numara = "+90 555 000 00 01",
        acilSira = 1,
        otomatikAc = false,
    )

    fun sayac(basarisiz: Int): AnlamamaSayaci {
        var s = AnlamamaSayaci()
        repeat(basarisiz) { s = s.basarisiz() }
        return s
    }

    "listening is one short vibration" {
        val p = TitresimDeseni.sec(GeriBildirim.LISTENING)
        p.size shouldBe 2
        p[0] shouldBe 0L
    }

    "understood is two short vibrations" {
        val p = TitresimDeseni.sec(GeriBildirim.UNDERSTOOD)
        p.size shouldBe 4
        p[1] shouldBe TitresimDeseni.sec(GeriBildirim.LISTENING)[1]
    }

    "not understood is one long vibration" {
        val p = TitresimDeseni.sec(GeriBildirim.NOT_UNDERSTOOD)
        p.size shouldBe 2
        (p[1] > TitresimDeseni.sec(GeriBildirim.LISTENING)[1]) shouldBe true
    }

    "first failure asks to repeat, second asks call or time" {
        sayac(1).adim(kisi) shouldBe AnlamamaAdimi.TekrarIste
        sayac(2).adim(kisi) shouldBe AnlamamaAdimi.AramaMiVakitMi
    }

    "third failure offers to call the acil_sira 1 person" {
        sayac(3).adim(kisi) shouldBe AnlamamaAdimi.KisiyiAra(kisi)
    }

    "third failure without an emergency person falls back to step 2" {
        sayac(3).adim(null) shouldBe AnlamamaAdimi.AramaMiVakitMi
    }

    "success resets the counter" {
        val s = sayac(2).basarili()
        s.basarisiz().adim(kisi) shouldBe AnlamamaAdimi.TekrarIste
    }
})
