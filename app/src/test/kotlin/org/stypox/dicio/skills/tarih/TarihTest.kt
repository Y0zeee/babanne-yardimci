package org.stypox.dicio.skills.tarih

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.stypox.dicio.util.TurkceSayi
import java.io.File

class TarihTest : StringSpec({
    "tr sentence files exist and other languages are untouched" {
        File("src/main/sentences/tr/tarih.yml").exists() shouldBe true
        File("src/main/sentences/tr/namaz_vakti.yml").exists() shouldBe true
        File("src/main/sentences/en/tarih.yml").exists() shouldBe false
    }

    "year-sized numbers are spelled without digits" {
        TurkceSayi.sayi(2026) shouldBe "iki bin yirmi altı"
    }
})
