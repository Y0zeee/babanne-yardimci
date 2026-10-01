package org.stypox.dicio.skills.tarih

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences

object TarihInfo : SkillInfo("tarih") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_tarih)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_tarih)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.DateRange)

    override fun build(ctx: SkillContext): Skill<*>? {
        val data = Sentences.Tarih[ctx.sentencesLanguage] ?: return null
        return TarihSkill(TarihInfo, data)
    }
}
