package org.stypox.dicio.skills.namaz_vakti

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.util.MatchHelper
import org.stypox.dicio.sentences.Sentences

private fun scoreOf(data: StandardRecognizerData<*>?, q: String): Float =
    data!!.score(MatchHelper(null, q), q).first.score()

private fun idOf(q: String): String =
    Sentences.NamazVakti["tr"]!!.score(MatchHelper(null, q), q).second::class.simpleName!!

/** Which of the three skills scores best for [q]. */
private fun bestSkill(q: String): String {
    val scores = mapOf(
        "namaz" to scoreOf(Sentences.NamazVakti["tr"], q),
        "tarih" to scoreOf(Sentences.Tarih["tr"], q),
        "saat" to scoreOf(Sentences.CurrentTime["tr"], q),
    )
    return scores.maxByOrNull { it.value }!!.key
}

class NamazVaktiSentencesTest : StringSpec({
    "tr prayer questions match the namaz_vakti skill" {
        for (q in listOf(
            "öğle ne zaman", "akşam ezanı kaçta", "akşama ne kadar var",
            "sıradaki vakit ne", "bugünkü namaz vakitleri", "imsak kaçta",
        )) {
            bestSkill(q) shouldBe "namaz"
        }
    }

    "tr date questions match the tarih skill" {
        for (q in listOf("bugün günlerden ne", "bugün ayın kaçı", "hicri tarih ne")) {
            bestSkill(q) shouldBe "tarih"
        }
    }

    "different prayer intents map to different sentence ids" {
        idOf("akşama ne kadar var") shouldNotBe idOf("bugünkü namaz vakitleri")
        idOf("öğle ne zaman") shouldNotBe idOf("sıradaki vakit ne")
    }

    "tr time question still goes to current_time" {
        bestSkill("saat kaç") shouldBe "saat"
    }
})
