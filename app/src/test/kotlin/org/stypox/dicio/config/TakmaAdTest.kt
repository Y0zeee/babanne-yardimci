package org.stypox.dicio.config

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TakmaAdTest : StringSpec({
    "suffixed forms map to the nickname" {
        TakmaAd.normalize("oğlumu") shouldBe "oğlum"
        TakmaAd.normalize("oğluma") shouldBe "oğlum"
        TakmaAd.normalize("oğlumun") shouldBe "oğlum"
        TakmaAd.normalize("kızımı") shouldBe "kızım"
        TakmaAd.normalize("kızıma") shouldBe "kızım"
    }

    "bizim oğlanı maps to bizim oğlan" {
        TakmaAd.normalize("bizim oğlanı") shouldBe "bizim oğlan"
    }

    "unrelated words are left alone" {
        TakmaAd.normalize("ayşe") shouldBe "ayşe"
    }
})
