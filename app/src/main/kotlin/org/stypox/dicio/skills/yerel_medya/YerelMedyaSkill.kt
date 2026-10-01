package org.stypox.dicio.skills.yerel_medya

import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import androidx.core.content.getSystemService
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.sentences.Sentences.YerelMedya
import java.io.File

class YerelMedyaSkill(correspondingSkillInfo: SkillInfo, data: StandardRecognizerData<YerelMedya>) :
    StandardRecognizerSkill<YerelMedya>(correspondingSkillInfo, data) {

    private fun dosyalar(klasor: String): List<String>? =
        File(klasor).list()?.toList()

    private fun cal(yollar: List<String>, yokCumlesi: String, karisik: Boolean = false) =
        YerelMedyaOutput(
            if (yollar.isEmpty()) yokCumlesi
            else if (YerelOynatici.cal(yollar, karisik)) "Tamam"
            else "Çalamadım"
        )

    private fun ses(ctx: SkillContext, yon: Int): SkillOutput {
        val am = ctx.android.getSystemService<AudioManager>()
            ?: return YerelMedyaOutput("Sesi ayarlayamadım")
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, yon, AudioManager.FLAG_SHOW_UI)
        return YerelMedyaOutput("Tamam")
    }

    private fun radyo(ctx: SkillContext): SkillOutput {
        val pm = ctx.android.packageManager
        val cumle = YerelMedyaMantik.radyoCumlesi {
            try {
                pm.getPackageInfo(it, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }
        val intent = pm.getLaunchIntentForPackage(YerelMedyaMantik.RADYO_PAKETI)
        if (cumle != null || intent == null) {
            return YerelMedyaOutput(cumle ?: YerelMedyaMantik.RADYO_YOK_CUMLESI)
        }
        ctx.android.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return YerelMedyaOutput("Radyo açıldı")
    }

    override suspend fun generateOutput(ctx: SkillContext, inputData: YerelMedya): SkillOutput =
        when (inputData) {
            is YerelMedya.Sure -> {
                val s = YerelMedyaMantik.sureCozumle(
                    inputData.ad ?: "",
                    dosyalar(YerelMedyaMantik.KURAN_KLASORU)?.toSet().orEmpty(),
                )
                if (s.yol != null) cal(listOf(s.yol), YerelMedyaMantik.SURE_YOK_CUMLESI)
                else YerelMedyaOutput(s.cumle ?: YerelMedyaMantik.SURE_YOK_CUMLESI)
            }
            YerelMedya.Kuran -> cal(
                YerelMedyaMantik.kuranYollari(dosyalar(YerelMedyaMantik.KURAN_KLASORU)),
                YerelMedyaMantik.KURAN_YOK_CUMLESI,
            )
            YerelMedya.Turku -> cal(
                YerelMedyaMantik.turkuYollari(dosyalar(YerelMedyaMantik.TURKU_KLASORU)),
                YerelMedyaMantik.TURKU_YOK_CUMLESI,
                karisik = true,
            )
            YerelMedya.Radyo -> radyo(ctx)
            YerelMedya.Dur -> {
                YerelOynatici.dur()
                YerelMedyaOutput("Tamam")
            }
            YerelMedya.SesAc -> ses(ctx, AudioManager.ADJUST_RAISE)
            YerelMedya.SesKis -> ses(ctx, AudioManager.ADJUST_LOWER)
        }
}

class YerelMedyaOutput(private val metin: String) : HeadlineSpeechSkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String = metin
}
