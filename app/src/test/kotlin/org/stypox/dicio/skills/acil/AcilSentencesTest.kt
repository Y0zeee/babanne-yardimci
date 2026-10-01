package org.stypox.dicio.skills.acil

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.util.MatchHelper
import org.stypox.dicio.sentences.Sentences

private fun scoreOf(data: StandardRecognizerData<*>?, q: String): Float =
    data!!.score(MatchHelper(null, q), q).first.score()

private fun bestSkill(q: String): String {
    val scores = mapOf(
        "acil" to scoreOf(Sentences.Acil["tr"], q),
        "pil" to scoreOf(Sentences.Pil["tr"], q),
        "cevapsiz" to scoreOf(Sentences.Cevapsiz["tr"], q),
        "sms" to scoreOf(Sentences.SmsOku["tr"], q),
        "saat" to scoreOf(Sentences.CurrentTime["tr"], q),
        "namaz" to scoreOf(Sentences.NamazVakti["tr"], q),
    )
    return scores.maxByOrNull { it.value }!!.key
}

class AcilSentencesTest : StringSpec({
    "tr emergency words match the acil skill" {
        for (q in listOf("imdat", "yardım et", "acil", "düştüm")) {
            bestSkill(q) shouldBe "acil"
        }
    }

    "acil skill has no sentences in other languages" {
        Sentences.Acil["en"] shouldBe null
    }
})
