package org.stypox.dicio.skills.sms_oku

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.stypox.dicio.config.Kisi
import org.stypox.dicio.config.YardimciConfig

private val config = YardimciConfig(
    kisiler = listOf(Kisi(listOf("kızın"), "+90 555 000 00 02", null, false)),
)

class SmsOkuMantikTest : StringSpec({
    "alphanumeric ids and short codes are ads" {
        smsGonderenReklamMi("TURKCELL") shouldBe true
        smsGonderenReklamMi("AKBANK") shouldBe true
        smsGonderenReklamMi("3434") shouldBe true
    }

    "real numbers are kept" {
        smsGonderenReklamMi("+90 555 000 00 02") shouldBe false
        smsGonderenReklamMi("05550000003") shouldBe false
    }

    "reading names the alias and unknown numbers" {
        val cumle = smsOkuCumlesi(
            listOf(
                OkunmamisSms("+905550000002", "merhaba"),
                OkunmamisSms("+905550000003", "selam"),
            ),
            config,
        )
        cumle shouldContain "kızın"
        cumle shouldContain "bilinmeyen numara"
        cumle shouldNotContain "Bir de reklam mesajı var"
    }

    "ad note is appended only when ads were filtered" {
        val cumle = smsOkuCumlesi(
            listOf(
                OkunmamisSms("+905550000002", "merhaba"),
                OkunmamisSms("TURKCELL", "kampanya"),
            ),
            config,
        )
        cumle shouldContain "Bir de reklam mesajı var"
        cumle shouldNotContain "kampanya"
    }
})
