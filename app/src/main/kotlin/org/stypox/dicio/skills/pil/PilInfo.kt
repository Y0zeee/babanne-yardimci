package org.stypox.dicio.skills.pil

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences

object PilInfo : SkillInfo("pil") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_pil)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_pil)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.BatteryFull)

    override fun build(ctx: SkillContext): Skill<*>? {
        val data = Sentences.Pil[ctx.sentencesLanguage] ?: return null
        return PilSkill(PilInfo, data)
    }
}
