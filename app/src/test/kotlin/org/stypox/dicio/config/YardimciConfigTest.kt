package org.stypox.dicio.config

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File

private val SAMPLE = File("../ornek-yardımcı.json").takeIf { it.exists() }
    ?: File("src/main/assets/ornek-yardımcı.json")

class YardimciConfigTest : StringSpec({
    "missing or invalid input yields safe defaults" {
        for (input in listOf(null, "", "{bozuk json", "[]", "42")) {
            val c = YardimciConfig.parse(input)
            c.kisiler.shouldBeEmpty()
            c.ttsHizi shouldBe 0.8f
        }
    }

    "sample file parses into people with names and numbers" {
        val c = YardimciConfig.parse(SAMPLE.readText())
        (c.kisiler.isNotEmpty()) shouldBe true
        c.kisiler.forEach {
            it.adlar.isNotEmpty() shouldBe true
            it.numara shouldContain "555 000 00"
        }
    }

    "sample file contains only fake numbers" {
        val numbers = Regex("\\+\\d[\\d ]{6,}").findAll(SAMPLE.readText()).map { it.value }.toList()
        numbers.isNotEmpty() shouldBe true
        numbers.forEach { it shouldContain "+90 555 000 00" }
    }
})
