package org.stypox.dicio.skills.pil

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PilMantikTest : StringSpec({
    "level is spoken in Turkish words" {
        pilSeviyeCumlesi(40) shouldBe "Şarjın yüzde kırk"
    }

    "warns once below 20 percent" {
        val t = DusukPilTakipcisi()
        t.guncelle(50, false) shouldBe false
        t.guncelle(19, false) shouldBe true
        t.guncelle(18, false) shouldBe false
        t.guncelle(15, false) shouldBe false
    }

    "re-arms after charging or going above the threshold" {
        val t = DusukPilTakipcisi()
        t.guncelle(19, false) shouldBe true
        t.guncelle(19, true) shouldBe false
        t.guncelle(60, true) shouldBe false
        t.guncelle(19, false) shouldBe true
    }

    "no warning while charging" {
        DusukPilTakipcisi().guncelle(10, true) shouldBe false
    }

    "low battery and plugged-in sentences" {
        DUSUK_PIL_CUMLESI shouldBe "Şarjın azaldı, şarja tak"
        SARJA_TAKILDI_CUMLESI shouldBe "Şarja takıldı"
    }
})
