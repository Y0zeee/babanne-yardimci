package org.stypox.dicio.skills.telephone

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File

private val SENTENCES = File("src/main/sentences")
private val REMOVED = listOf("lyrics", "search", "translation", "weather", "navigation")

class TurkishSentencesTest : StringSpec({
    "tr yes/no accepts the grandmother's words" {
        val text = File(SENTENCES, "tr/util_yes_no.yml").readText()
        val parts = text.split("no:")
        for (w in listOf("he", "olur", "tamam", "hı hı", "ara")) parts[0] shouldContain w
        for (w in listOf("yok", "istemem", "dur", "hayır")) parts[1] shouldContain w
    }

    "tr has no lyrics, search, translation, weather, navigation sentences" {
        for (n in REMOVED) File(SENTENCES, "tr/$n.yml").exists() shouldBe false
    }

    "other languages keep those sentence files" {
        for (n in REMOVED) File(SENTENCES, "en/$n.yml").exists() shouldBe true
    }

    "tr confirm string asks with the name" {
        File("src/main/res/values-tr/strings.xml").readText() shouldContain "arıyorum, olur mu?"
    }
})
