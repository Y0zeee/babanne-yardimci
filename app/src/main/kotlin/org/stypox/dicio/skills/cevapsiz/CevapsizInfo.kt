package org.stypox.dicio.skills.cevapsiz

import android.Manifest
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Permission
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences
import org.stypox.dicio.util.PERMISSION_CALL_PHONE

object CevapsizInfo : SkillInfo("cevapsiz") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_cevapsiz)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_cevapsiz)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.PhoneMissed)

    override val neededPermissions: List<Permission> = listOf(
        Permission.NormalPermission(
            name = R.string.perm_read_call_log,
            id = Manifest.permission.READ_CALL_LOG,
        ),
        PERMISSION_CALL_PHONE,
    )

    override fun build(ctx: SkillContext): Skill<*>? {
        val data = Sentences.Cevapsiz[ctx.sentencesLanguage] ?: return null
        val yesNoData = Sentences.UtilYesNo[ctx.sentencesLanguage] ?: return null
        return CevapsizSkill(CevapsizInfo, data, yesNoData)
    }
}
