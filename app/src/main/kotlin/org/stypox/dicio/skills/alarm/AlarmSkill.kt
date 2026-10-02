package org.stypox.dicio.skills.alarm

import android.content.Intent
import android.provider.AlarmClock
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.sentences.Sentences.Alarm
import java.time.LocalDateTime

class AlarmSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<Alarm>) :
    StandardRecognizerSkill<Alarm>(correspondingSkillInfo, data) {

    override suspend fun generateOutput(ctx: SkillContext, inputData: Alarm): SkillOutput {
        val now = LocalDateTime.now()
        val zaman = when (inputData) {
            is Alarm.Kur -> inputData.zaman?.let { TurkceSaatCozucu.coz(it, now) }
        } ?: return AlarmOutput("Saati anlayamadım.")

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, zaman.hour)
            putExtra(AlarmClock.EXTRA_MINUTES, zaman.minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, "Yardımcı")
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.android.startActivity(intent)
        return AlarmOutput(AlarmOutput.onayMetni(zaman, now))
    }
}
