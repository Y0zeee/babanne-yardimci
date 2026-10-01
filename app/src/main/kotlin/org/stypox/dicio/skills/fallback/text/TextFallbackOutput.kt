package org.stypox.dicio.skills.fallback.text

import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.InteractionPlan
import org.dicio.skill.standard.StandardRecognizerData
import org.stypox.dicio.R
import org.stypox.dicio.geri_bildirim.AnlamamaAdimi
import org.stypox.dicio.geri_bildirim.AnlamamaSayaci
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.sentences.Sentences
import org.stypox.dicio.skills.telephone.ConfirmCallOutput
import org.stypox.dicio.util.getString

class TextFallbackOutput(
    val sayac: AnlamamaSayaci,
    private val adim: AnlamamaAdimi,
    yesNoData: StandardRecognizerData<Sentences.UtilYesNo>?,
) : HeadlineSpeechSkillOutput {
    // without yes/no sentences the call question could not be answered, so it is not asked
    private val aramaOnayi: ConfirmCallOutput? =
        (adim as? AnlamamaAdimi.KisiyiAra)?.let { a ->
            yesNoData?.let {
                ConfirmCallOutput(
                    a.kisi.adlar.first(), a.kisi.numara, it, R.string.eval_no_match_call_person
                )
            }
        }

    override fun getSpeechOutput(ctx: SkillContext): String = aramaOnayi?.getSpeechOutput(ctx)
        ?: ctx.getString(
            if (adim == AnlamamaAdimi.TekrarIste) R.string.eval_no_match_repeat
            else R.string.eval_no_match_call_or_time
        )

    // the microphone is reopened, but the skill provided will never actually match (except for
    // the yes/no one), so the previous batch of skills will be used instead
    override fun getInteractionPlan(ctx: SkillContext) =
        aramaOnayi?.getInteractionPlan(ctx) ?: InteractionPlan.Continue(reopenMicrophone = true)
}
