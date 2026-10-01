package org.stypox.dicio.skills.timer

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.dicio.numbers.ParserFormatter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.context.SpeechOutputDevice
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.util.MatchHelper
import org.stypox.dicio.mocked
import org.stypox.dicio.sentences.Sentences
import java.util.Locale

private fun scoreOf(data: StandardRecognizerData<*>?, q: String): Float =
    data!!.score(MatchHelper(null, q), q).first.score()

/** Turkish context: dicio-numbers has no Turkish formatter, so parserFormatter is null. */
private object TrContext : SkillContext {
    override val android get() = mocked()
    override val locale: Locale get() = Locale("tr")
    override val sentencesLanguage: String get() = "tr"
    override val parserFormatter: ParserFormatter? get() = null
    override val speechOutputDevice: SpeechOutputDevice get() = mocked()
    override val previousOutput: SkillOutput get() = mocked()
    override val standardMatchHelper: MatchHelper get() = mocked()
}

class TurkceTimerTest : StringSpec({
    "tr duration sentences go to the timer skill, not alarm" {
        for (q in listOf("on dakika sonra haber ver", "yarım saat sonra söyle")) {
            val timer = scoreOf(Sentences.Timer["tr"], q)
            val alarm = scoreOf(Sentences.Alarm["tr"], q)
            (timer > 0f) shouldBe true
            (timer > alarm) shouldBe true
        }
    }

    "timer skill is buildable in tr without a parser formatter" {
        TimerInfo.build(TrContext) shouldNotBe null
    }
})
