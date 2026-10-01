package org.stypox.dicio.skills.namaz_vakti

import android.Manifest
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.Permission
import org.dicio.skill.skill.Skill
import org.dicio.skill.skill.SkillInfo
import org.stypox.dicio.R
import org.stypox.dicio.sentences.Sentences

object NamazVaktiInfo : SkillInfo("namaz_vakti") {
    override fun name(context: Context) =
        context.getString(R.string.skill_name_namaz_vakti)

    override fun sentenceExample(context: Context) =
        context.getString(R.string.skill_sentence_example_namaz_vakti)

    @Composable
    override fun icon() =
        rememberVectorPainter(Icons.Default.Schedule)

    override val neededPermissions: List<Permission> = listOf(
        Permission.NormalPermission(
            name = R.string.perm_location,
            id = Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    override fun build(ctx: SkillContext): Skill<*>? {
        val data = Sentences.NamazVakti[ctx.sentencesLanguage] ?: return null
        return NamazVaktiSkill(NamazVaktiInfo, data)
    }
}
