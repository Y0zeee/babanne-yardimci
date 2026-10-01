package org.stypox.dicio.skills.alarm

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.dicio.skill.standard.StandardRecognizerData
import org.dicio.skill.standard.util.MatchHelper
import org.stypox.dicio.sentences.Sentences
import java.io.File
import java.time.LocalDateTime

private fun at(day: Int, h: Int, m: Int = 0) = LocalDateTime.of(2026, 10, day, h, m)

private fun coz(text: String, now: LocalDateTime) = TurkceSaatCozucu.coz(text, now)

private fun scoreOf(data: StandardRecognizerData<*>?, q: String): Float =
    data!!.score(MatchHelper(null, q), q).first.score()

class AlarmTest : StringSpec({
    "explicit day-part hours" {
        coz("sabah yedide", at(2, 5)) shouldBe at(2, 7)
        coz("sabah yedide", at(2, 10)) shouldBe at(3, 7)
        coz("öğleden sonra üçte", at(2, 10)) shouldBe at(2, 15)
        coz("akşam sekizde", at(2, 10)) shouldBe at(2, 20)
        coz("gece on ikide", at(2, 10)) shouldBe at(3, 0)
        coz("öğlen on ikide", at(2, 10)) shouldBe at(2, 12)
    }

    "yarın moves to the next day" {
        coz("yarın sabah altıda", at(2, 10)) shouldBe at(3, 6)
        coz("yarın sabah altıda", at(2, 3)) shouldBe at(3, 6)
    }

    "ambiguous hour resolves to the next suitable time" {
        coz("saat beşte", at(2, 10)) shouldBe at(2, 17)
        coz("saat beşte", at(2, 3)) shouldBe at(2, 5)
        coz("yedi buçukta", at(2, 10)) shouldBe at(2, 19, 30)
        coz("yedi buçukta", at(2, 5)) shouldBe at(2, 7, 30)
    }

    "çeyrek geçe and kala" {
        coz("dokuzu çeyrek geçe", at(2, 5)) shouldBe at(2, 9, 15)
        coz("dokuza çeyrek kala", at(2, 5)) shouldBe at(2, 8, 45)
    }

    "unparseable input returns null" {
        coz("bugün hava nasıl", at(2, 10)) shouldBe null
        coz("", at(2, 10)) shouldBe null
    }

    "alarm sentences match the alarm skill" {
        for (q in listOf(
            "sabah yedide uyandır", "saat beşte uyandır", "yedi buçukta uyandır",
            "öğleden sonra üçte haber ver", "yarın sabah altıda kaldır",
        )) {
            val alarm = scoreOf(Sentences.Alarm["tr"], q)
            val timer = scoreOf(Sentences.Timer["tr"], q)
            (alarm > 0f) shouldBe true
            (alarm > timer) shouldBe true
        }
    }

    "spoken confirmation is one short sentence with number words" {
        val s = AlarmOutput.onayMetni(at(3, 7), at(2, 10))
        s shouldContain "yedi"
        s shouldContain "uyandıracağım"
        s.any { it.isDigit() } shouldBe false
        s.count { it == '.' } shouldBe 1
    }

    "alarm skill fires ACTION_SET_ALARM with the expected extras" {
        val src = File("src/main/kotlin/org/stypox/dicio/skills/alarm/AlarmSkill.kt").readText()
        for (w in listOf(
            "ACTION_SET_ALARM", "EXTRA_HOUR", "EXTRA_MINUTES", "EXTRA_MESSAGE",
            "EXTRA_SKIP_UI", "Yardımcı",
        )) src shouldContain w
    }

    "manifest declares SET_ALARM and the skill is registered" {
        File("src/main/AndroidManifest.xml").readText() shouldContain
            "com.android.alarm.permission.SET_ALARM"
        File("src/main/kotlin/org/stypox/dicio/eval/SkillHandler.kt").readText() shouldContain "AlarmInfo"
        File("src/main/sentences/skill_definitions.yml").readText() shouldContain "id: alarm"
    }

    "other languages gain no alarm sentences" {
        for (l in listOf("de", "fr", "it", "es", "ru", "pt")) {
            File("src/main/sentences/$l/alarm.yml").exists() shouldBe false
        }
        File("src/main/sentences/tr/alarm.yml").exists() shouldBe true
    }
})
