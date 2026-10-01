package org.stypox.dicio.skills.sms_oku

import android.net.Uri
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.config.YardimciConfig
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.sentences.Sentences.SmsOku

class SmsOkuSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<SmsOku>) :
    StandardRecognizerSkill<SmsOku>(correspondingSkillInfo, data) {

    override suspend fun generateOutput(ctx: SkillContext, inputData: SmsOku): SkillOutput {
        val liste = ArrayList<OkunmamisSms>()
        ctx.android.contentResolver.query(
            Uri.parse("content://sms/inbox"),
            arrayOf("address", "body"),
            "read = 0",
            null,
            "date DESC",
        )?.use { c ->
            while (c.moveToNext()) {
                liste.add(OkunmamisSms(c.getString(0).orEmpty(), c.getString(1).orEmpty()))
            }
        }
        return SmsOkuOutput(smsOkuCumlesi(liste, YardimciConfig.load(ctx.android)))
    }
}

class SmsOkuOutput(private val metin: String) : HeadlineSpeechSkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String = metin
}
