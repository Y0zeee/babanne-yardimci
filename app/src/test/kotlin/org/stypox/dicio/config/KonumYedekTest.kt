package org.stypox.dicio.config

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.io.File

private val SAMPLE = File("../ornek-yardımcı.json").takeIf { it.exists() }
    ?: File("src/main/assets/ornek-yardımcı.json")

class KonumYedekTest : StringSpec({
    "konum_yedek parses enlem, boylam and ilce" {
        val c = YardimciConfig.parse(
            """{"konum_yedek": {"enlem": 39.5, "boylam": 32.25, "ilce": "Ornek"}}""",
        )
        c.konumYedek shouldBe KonumYedek(39.5, 32.25, "Ornek")
    }

    "null, missing or malformed konum_yedek gives null" {
        YardimciConfig.parse("""{"konum_yedek": null}""").konumYedek shouldBe null
        YardimciConfig.parse("""{}""").konumYedek shouldBe null
        YardimciConfig.parse("""{"konum_yedek": "+90 555 000 00 01"}""").konumYedek shouldBe null
        YardimciConfig.parse("""{"konum_yedek": {"enlem": 39.5}}""").konumYedek shouldBe null
    }

    "sample file has a coordinate fallback" {
        YardimciConfig.parse(SAMPLE.readText()).konumYedek shouldNotBe null
    }
})
