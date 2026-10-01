package org.stypox.dicio.skills.acil

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.stypox.dicio.config.Kisi
import org.stypox.dicio.config.YardimciConfig
import java.time.LocalTime

private fun kisi(no: Int, sira: Int?) =
    Kisi(adlar = listOf("kisi$no"), numara = "+90 555 000 00 0$no", acilSira = sira, otomatikAc = false)

/** Fake executor: no real call/SMS/location. Records every action in [log]. */
private class Sahte(
    val baglananlar: Set<String> = emptySet(),
    val smsSonuclari: MutableList<Boolean> = mutableListOf(),
) : AcilEylemci {
    val log = mutableListOf<String>()
    override fun ara(numara: String): Boolean {
        log += "ara:$numara"
        return numara in baglananlar
    }
    override fun smsGonder(numara: String, metin: String): Boolean {
        log += "sms:$numara"
        return if (smsSonuclari.isEmpty()) true else smsSonuclari.removeAt(0)
    }
}

class AcilMantikTest : StringSpec({
    "only contacts with acil_sira, ascending, ties keep file order" {
        val list = listOf(kisi(1, 3), kisi(2, null), kisi(3, 1), kisi(4, 3), kisi(5, 2))
        AcilMantik.acilKisiler(list).map { it.numara.last() } shouldBe listOf('3', '5', '1', '4')
    }

    "acil_112 defaults to true when missing and false when set" {
        YardimciConfig.parse("{}").acil112 shouldBe true
        YardimciConfig.parse("{\"acil_112\": false}").acil112 shouldBe false
        YardimciConfig.parse("{}").sahipAdi shouldBe "Babaanne"
    }

    "112 is called only after every contact failed" {
        val f = Sahte()
        AcilMantik.planiYurut(listOf(kisi(1, 1), kisi(2, 2)), true, "x", f)
        f.log.filter { it.startsWith("ara:") } shouldBe
            listOf("ara:+90 555 000 00 01", "ara:+90 555 000 00 02", "ara:112")
    }

    "no 112 when acil_112 is false or someone connected" {
        val f = Sahte()
        AcilMantik.planiYurut(listOf(kisi(1, 1)), false, "x", f)
        f.log.contains("ara:112") shouldBe false

        val g = Sahte(baglananlar = setOf("+90 555 000 00 01"))
        AcilMantik.planiYurut(listOf(kisi(1, 1), kisi(2, 2)), true, "x", g)
        g.log.contains("ara:112") shouldBe false
        g.log.contains("ara:+90 555 000 00 02") shouldBe false
    }

    "no 112 on an unconfigured phone (no emergency contacts)" {
        val f = Sahte()
        AcilMantik.planiYurut(emptyList(), true, "x", f)
        f.log shouldBe emptyList()
        val g = Sahte()
        AcilMantik.planiYurut(listOf(kisi(1, null)), true, "x", g)
        g.log.contains("ara:112") shouldBe false
    }

    "cancel within 5 s cancels, otherwise proceed" {
        AcilMantik.geriSayim(iptalMs = 1000) shouldBe AcilMantik.SayimSonucu.IPTAL
        AcilMantik.geriSayim(iptalMs = 4999) shouldBe AcilMantik.SayimSonucu.IPTAL
        AcilMantik.geriSayim(iptalMs = null) shouldBe AcilMantik.SayimSonucu.DEVAM
        AcilMantik.geriSayim(iptalMs = 5001) shouldBe AcilMantik.SayimSonucu.DEVAM
    }

    "cancel words are recognised" {
        for (w in listOf("dur", "istemiyorum", "iptal")) AcilMantik.iptalKelimesiMi(w) shouldBe true
        AcilMantik.iptalKelimesiMi("imdat") shouldBe false
    }

    "sms text has dot decimals and zero-padded time" {
        AcilMantik.smsMetni("Babaanne", 39.92, 32.85, null, LocalTime.of(8, 5)) shouldBe
            "Babaanne yardım istiyor. Konum: https://maps.google.com/?q=39.92,32.85 (saat 08:05)"
    }

    "sms fallback text uses cell info when there is no coordinate" {
        val t = AcilMantik.smsMetni("Babaanne", null, null, "baz 123", LocalTime.of(14, 30))
        t.startsWith("Babaanne yardım istiyor.") shouldBe true
        t.contains("baz 123") shouldBe true
        t.contains("maps.google.com") shouldBe false
        t.contains("14:30") shouldBe true
    }

    "failed sms is retried exactly once" {
        val f = Sahte(smsSonuclari = mutableListOf(false, true))
        AcilMantik.planiYurut(listOf(kisi(1, 1)), false, "x", f)
        f.log.count { it.startsWith("sms:") } shouldBe 2

        val g = Sahte(smsSonuclari = mutableListOf(false, false, false))
        AcilMantik.planiYurut(listOf(kisi(1, 1)), false, "x", g)
        g.log.count { it.startsWith("sms:") } shouldBe 2
    }
})
