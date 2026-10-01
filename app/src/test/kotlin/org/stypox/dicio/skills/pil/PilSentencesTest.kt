package org.stypox.dicio.skills.pil

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.util.MatchHelper
import org.stypox.dicio.sentences.Sentences

private fun scoreOf(data: StandardRecognizerData<*>?, q: String): Float =
    data!!.score(MatchHelper(null, q), q).first.score()

private fun bestSkill(q: String): String {
    val scores = mapOf(
        "pil" to scoreOf(Sentences.Pil["tr"], q),
        "cevapsiz" to scoreOf(Sentences.Cevapsiz["tr"], q),
        "sms" to scoreOf(Sentences.SmsOku["tr"], q),
        "saat" to scoreOf(Sentences.CurrentTime["tr"], q),
        "namaz" to scoreOf(Sentences.NamazVakti["tr"], q),
    )
    return scores.maxByOrNull { it.value }!!.key
}

class PilSentencesTest : StringSpec({
    "tr battery question matches the pil skill" {
        bestSkill("şarjım ne kadar") shouldBe "pil"
    }

    "tr missed call question matches the cevapsiz skill" {
        bestSkill("kim aradı") shouldBe "cevapsiz"
    }

    "tr message questions match the sms_oku skill" {
        bestSkill("mesaj var mı") shouldBe "sms"
        bestSkill("mesajları oku") shouldBe "sms"
    }
})
