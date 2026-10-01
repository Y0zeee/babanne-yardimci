package org.stypox.dicio.skills.sms_oku

import android.Manifest
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sms
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Permission
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences

object SmsOkuInfo : SkillInfo("sms_oku") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_sms_oku)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_sms_oku)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.Sms)

    override val neededPermissions: List<Permission> = listOf(
        Permission.NormalPermission(
            name = R.string.perm_read_sms,
            id = Manifest.permission.READ_SMS,
        ),
    )

    override fun build(ctx: SkillContext): Skill<*>? {
        val data = Sentences.SmsOku[ctx.sentencesLanguage] ?: return null
        return SmsOkuSkill(SmsOkuInfo, data)
    }
}
