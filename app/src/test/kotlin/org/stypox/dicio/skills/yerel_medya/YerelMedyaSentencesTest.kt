package org.stypox.dicio.skills.yerel_medya

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.util.MatchHelper
import org.stypox.dicio.sentences.Sentences

private fun scoreOf(data: StandardRecognizerData<*>?, q: String): Float =
    data!!.score(MatchHelper(null, q), q).first.score()

private fun bestSkill(q: String): String {
    val scores = mapOf(
        "yerel_medya" to scoreOf(Sentences.YerelMedya["tr"], q),
        "media" to scoreOf(Sentences.Media["tr"], q),
        "acil_iptal" to scoreOf(Sentences.AcilIptal["tr"], q),
        "open" to scoreOf(Sentences.Open["tr"], q),
        "telephone" to scoreOf(Sentences.Telephone["tr"], q),
        "flashlight" to scoreOf(Sentences.Flashlight["tr"], q),
    )
    return scores.maxByOrNull { it.value }!!.key
}

class YerelMedyaSentencesTest : StringSpec({
    "tr media phrases match the yerel_medya skill" {
        for (q in listOf(
            "Yasin oku", "Fatiha oku", "Mülk suresi", "Kuran aç", "Türkü aç",
            "radyo aç", "dur", "sesi aç", "sesi kıs",
        )) {
            bestSkill(q) shouldBe "yerel_medya"
        }
    }

    "tr flashlight phrases match flashlight turn_on and turn_off" {
        val data = Sentences.Flashlight["tr"]!!
        bestSkill("feneri aç") shouldBe "flashlight"
        bestSkill("feneri kapat") shouldBe "flashlight"
        data.score(MatchHelper(null, "feneri aç"), "feneri aç").second shouldBe
            Sentences.Flashlight.TurnOn
        data.score(MatchHelper(null, "feneri kapat"), "feneri kapat").second shouldBe
            Sentences.Flashlight.TurnOff
    }

    "yerel_medya has no sentences in other languages" {
        Sentences.YerelMedya["en"] shouldBe null
    }
})
