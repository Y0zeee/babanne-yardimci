package org.stypox.dicio.skills.namaz_vakti

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.stypox.dicio.util.TurkceSayi
import java.time.LocalTime

class NamazVaktiOutputTest : StringSpec({
    "numbers are spelled in Turkish words" {
        TurkceSayi.sayi(0) shouldBe "sıfır"
        TurkceSayi.sayi(5) shouldBe "beş"
        TurkceSayi.sayi(12) shouldBe "on iki"
        TurkceSayi.sayi(32) shouldBe "otuz iki"
        TurkceSayi.sayi(59) shouldBe "elli dokuz"
    }

    "sentence gives time and remaining duration in words" {
        val s = NamazVaktiFormat.vakitCumlesi(
            "Öğle ezanı", LocalTime.of(12, 32), LocalTime.of(11, 22),
        )
        s shouldBe "Öğle ezanı on iki otuz ikide. Bir saat on dakika var."
        s.any { it.isDigit() } shouldBe false
    }

    "missing location is reported exactly" {
        NamazVaktiFormat.konumYok() shouldBe "Konumu bulamadım."
    }
})
