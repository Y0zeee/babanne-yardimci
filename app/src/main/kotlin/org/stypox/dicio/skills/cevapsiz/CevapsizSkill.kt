package org.stypox.dicio.skills.cevapsiz

import android.provider.CallLog
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.InteractionPlan
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.config.YardimciConfig
import org.stypox.dicio.io.graphical.Headline
import org.stypox.dicio.sentences.Sentences
import org.stypox.dicio.sentences.Sentences.Cevapsiz
import org.stypox.dicio.skills.telephone.ConfirmedCallOutput
import org.stypox.dicio.skills.telephone.TelephoneSkill
import org.stypox.dicio.util.RecognizeYesNoSkill

class CevapsizSkill(
    correspondingSkillInfo: SkillInfo,
    data: StandardRecognizerData<Cevapsiz>,
    private val yesNoData: StandardRecognizerData<Sentences.UtilYesNo>,
) : StandardRecognizerSkill<Cevapsiz>(correspondingSkillInfo, data) {

    override suspend fun generateOutput(ctx: SkillContext, inputData: Cevapsiz): SkillOutput {
        val simdi = System.currentTimeMillis()
        val liste = ArrayList<CevapsizArama>()
        ctx.android.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE),
            CallLog.Calls.TYPE + " = ? AND " + CallLog.Calls.DATE + " >= ?",
            arrayOf(CallLog.Calls.MISSED_TYPE.toString(), (simdi - CEVAPSIZ_PENCERE_MS).toString()),
            CallLog.Calls.DATE + " DESC",
        )?.use { c ->
            while (c.moveToNext()) {
                val numara = c.getString(0)
                if (!numara.isNullOrBlank()) liste.add(CevapsizArama(numara, c.getLong(1)))
            }
        }
        val sonuc = cevapsizSonucu(liste, simdi, YardimciConfig.load(ctx.android))
        return CevapsizOutput(sonuc, yesNoData)
    }
}

class CevapsizOutput(
    private val sonuc: CevapsizSonuc,
    private val yesNoData: StandardRecognizerData<Sentences.UtilYesNo>,
) : SkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String =
        if (sonuc.aranacakNumara == null) sonuc.cumle else sonuc.cumle + ". Arayayım mı?"

    override fun getInteractionPlan(ctx: SkillContext): InteractionPlan {
        val numara = sonuc.aranacakNumara ?: return InteractionPlan.FinishInteraction
        val evetHayir = object : RecognizeYesNoSkill(CevapsizInfo, yesNoData) {
            override suspend fun generateOutput(
                ctx: SkillContext,
                inputData: Boolean,
            ): SkillOutput {
                return if (inputData) {
                    TelephoneSkill.call(ctx.android, numara)
                    ConfirmedCallOutput(numara)
                } else {
                    ConfirmedCallOutput(null)
                }
            }
        }
        return InteractionPlan.ReplaceSubInteraction(
            reopenMicrophone = true,
            nextSkills = listOf(evetHayir),
        )
    }

    @Composable
    override fun GraphicalOutput(ctx: SkillContext) {
        Column { Headline(text = getSpeechOutput(ctx)) }
    }
}
