package org.stypox.dicio.skills.fallback.text

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.stypox.dicio.config.YardimciConfig
import org.stypox.dicio.geri_bildirim.AnlamamaSayaci
import org.stypox.dicio.sentences.Sentences
import org.stypox.dicio.util.RecognizeEverythingSkill

class TextFallbackSkill(correspondingSkillInfo: SkillInfo) :
    RecognizeEverythingSkill(correspondingSkillInfo) {
    override suspend fun generateOutput(ctx: SkillContext, inputData: String): SkillOutput {
        // any other output in between (i.e. a successful skill) resets the counter
        val sayac = ((ctx.previousOutput as? TextFallbackOutput)?.sayac ?: AnlamamaSayaci())
            .basarisiz()
        val acilKisi = YardimciConfig.load(ctx.android).kisiler.firstOrNull { it.acilSira == 1 }
        return TextFallbackOutput(
            sayac = sayac,
            adim = sayac.adim(acilKisi),
            yesNoData = Sentences.UtilYesNo[ctx.sentencesLanguage],
        )
    }
}
