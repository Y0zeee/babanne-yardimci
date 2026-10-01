package org.stypox.dicio.skills.tarih

import org.dicio.skill.context.SkillContext
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput

class TarihOutput(private val metin: String) : HeadlineSpeechSkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String = metin
}
