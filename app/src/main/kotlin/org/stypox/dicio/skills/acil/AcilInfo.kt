package org.stypox.dicio.skills.acil

import android.Manifest
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Permission
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences
import org.stypox.dicio.util.PERMISSION_CALL_PHONE

object AcilInfo : SkillInfo("acil") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_acil)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_acil)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.Warning)

    override val neededPermissions: List<Permission> = listOf(
        PERMISSION_CALL_PHONE,
        Permission.NormalPermission(
            name = R.string.perm_send_sms,
            id = Manifest.permission.SEND_SMS,
        ),
        Permission.NormalPermission(
            name = R.string.perm_location,
            id = Manifest.permission.ACCESS_FINE_LOCATION,
        ),
    )

    override fun build(ctx: SkillContext): Skill<*>? {
        val data = Sentences.Acil[ctx.sentencesLanguage] ?: return null
        val iptalData = Sentences.AcilIptal[ctx.sentencesLanguage] ?: return null
        return AcilSkill(AcilInfo, data, iptalData)
    }
}
