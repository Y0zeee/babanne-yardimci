package org.stypox.dicio.ui.kurulum

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class KurulumMantikTest : StringSpec({
    "granted permission is green without a button" {
        val g = KurulumMantik.izinGorunumu(true)
        g.yesil shouldBe true
        g.dugmeGorunur shouldBe false
    }

    "denied permission is red with an 'İzin ver' button" {
        val g = KurulumMantik.izinGorunumu(false)
        g.yesil shouldBe false
        g.dugmeGorunur shouldBe true
    }

    "granted and denied labels differ" {
        (KurulumMantik.izinGorunumu(true).etiket != KurulumMantik.izinGorunumu(false).etiket) shouldBe true
    }

    "number mask keeps only the last two digits" {
        val m = KurulumMantik.maskeleNumara("+90 555 000 00 07")
        m.endsWith("07") shouldBe true
        m.count { it.isDigit() } shouldBe 2
    }

    "number mask handles short or empty input" {
        KurulumMantik.maskeleNumara("").count { it.isDigit() } shouldBe 0
        KurulumMantik.maskeleNumara("5").count { it.isDigit() } shouldBe 1
    }

    "file count counts only mp3 files, case-insensitive" {
        KurulumMantik.mp3Say(listOf("yasin.mp3", "Fatiha.MP3", "notlar.txt", "mulk.mp3.bak")) shouldBe 2
    }

    "file count of a missing folder is zero" {
        KurulumMantik.mp3Say(null) shouldBe 0
        KurulumMantik.mp3Say(emptyList()) shouldBe 0
    }
})
