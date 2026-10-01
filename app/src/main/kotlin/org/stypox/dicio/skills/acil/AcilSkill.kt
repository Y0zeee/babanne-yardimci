package org.stypox.dicio.skills.acil

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dicio.skill.context.SkillContext
import org.dicio.skill.skill.InteractionPlan
import org.dicio.skill.skill.SkillInfo
import org.dicio.skill.skill.SkillOutput
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.StandardRecognizerSkill
import org.stypox.dicio.config.YardimciConfig
import org.stypox.dicio.io.graphical.HeadlineSpeechSkillOutput
import org.stypox.dicio.sentences.Sentences.Acil
import org.stypox.dicio.sentences.Sentences.AcilIptal

/** Time the "Yardım çağırıyorum" sentence takes to be spoken, before the 5 s window starts. */
private const val KONUSMA_PAYI_MS = 3000L

private val acilScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

/** One countdown; [iptal] records when (if ever) the user said a cancel word. */
class AcilGeriSayim {
    private val baslangicMs = System.currentTimeMillis() + KONUSMA_PAYI_MS

    @Volatile
    private var iptalMs: Long? = null

    fun iptal() {
        iptalMs = maxOf(0L, System.currentTimeMillis() - baslangicMs)
    }

    suspend fun bekleVeSonuc(): AcilMantik.SayimSonucu {
        delay(baslangicMs + AcilMantik.GERI_SAYIM_MS - System.currentTimeMillis())
        return AcilMantik.geriSayim(iptalMs)
    }
}

class AcilSkill(
    correspondingSkillInfo: SkillInfo,
    data: StandardRecognizerData<Acil>,
    private val iptalData: StandardRecognizerData<AcilIptal>,
) : StandardRecognizerSkill<Acil>(correspondingSkillInfo, data) {

    override suspend fun generateOutput(ctx: SkillContext, inputData: Acil): SkillOutput {
        val sayim = AcilGeriSayim()
        val context = ctx.android.applicationContext
        acilScope.launch {
            if (sayim.bekleVeSonuc() == AcilMantik.SayimSonucu.DEVAM) acilYurut(context)
        }
        return AcilOutput(AcilInfo, sayim, iptalData)
    }
}

private fun acilYurut(context: Context) {
    val config = YardimciConfig.load(context)
    val yurutucu = AcilYurutucu(context)
    val metin = yurutucu.smsMetni(config.sahipAdi)
    AcilMantik.planiYurut(config.kisiler, config.acil112, metin, yurutucu)
}

class AcilOutput(
    private val info: SkillInfo,
    private val sayim: AcilGeriSayim,
    private val iptalData: StandardRecognizerData<AcilIptal>,
) : HeadlineSpeechSkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String =
        "Yardım çağırıyorum. İstemiyorsan dur de."

    override fun getInteractionPlan(ctx: SkillContext): InteractionPlan {
        val iptal = object : StandardRecognizerSkill<AcilIptal>(info, iptalData) {
            override suspend fun generateOutput(ctx: SkillContext, inputData: AcilIptal): SkillOutput {
                sayim.iptal()
                return AcilIptalOutput
            }
        }
        return InteractionPlan.ReplaceSubInteraction(
            reopenMicrophone = true,
            nextSkills = listOf(iptal),
        )
    }
}

object AcilIptalOutput : HeadlineSpeechSkillOutput {
    override fun getSpeechOutput(ctx: SkillContext): String = "Tamam, vazgeçtim."
}
