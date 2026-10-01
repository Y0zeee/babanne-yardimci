package org.stypox.dicio.skills.yerel_medya

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File

class YerelMedyaMantikTest : StringSpec({
    "spoken surah variants map to canonical file names" {
        for (q in listOf("yasin oku", "yasini oku", "yâsîn")) {
            YerelMedyaMantik.sureDosyaAdi(q) shouldBe "yasin.mp3"
        }
        YerelMedyaMantik.sureDosyaAdi("fatiha") shouldBe "fatiha.mp3"
        YerelMedyaMantik.sureDosyaAdi("fatihayı oku") shouldBe "fatiha.mp3"
        YerelMedyaMantik.sureDosyaAdi("mülk suresi") shouldBe "mulk.mp3"
        YerelMedyaMantik.sureDosyaAdi("rahman") shouldBe "rahman.mp3"
        YerelMedyaMantik.sureDosyaAdi("amme cüzü") shouldBe "amme.mp3"
    }

    "unknown surah name gives null" {
        YerelMedyaMantik.sureDosyaAdi("bilinmeyen sure") shouldBe null
    }

    "existing surah file resolves to its path" {
        val s = YerelMedyaMantik.sureCozumle("yasin oku", setOf("yasin.mp3"))
        s.yol shouldBe "/sdcard/Yardimci/kuran/yasin.mp3"
        s.cumle shouldBe null
    }

    "missing surah file gives the spoken 'not on phone' sentence" {
        val s = YerelMedyaMantik.sureCozumle("yasin oku", setOf("fatiha.mp3"))
        s.yol shouldBe null
        s.cumle shouldBe "Bu sûre telefonda yok"
    }

    "empty or missing turku folder gives a short Turkish sentence" {
        YerelMedyaMantik.turkuYollari(null) shouldBe emptyList()
        YerelMedyaMantik.turkuYollari(emptyList()) shouldBe emptyList()
        YerelMedyaMantik.TURKU_YOK_CUMLESI shouldBe "Telefonda türkü yok"
    }

    "turku list keeps only mp3 files, in order, with full paths" {
        YerelMedyaMantik.turkuYollari(listOf("b.mp3", "a.mp3", "not.txt")) shouldBe listOf(
            "/sdcard/Yardimci/turku/a.mp3",
            "/sdcard/Yardimci/turku/b.mp3",
        )
    }

    "radio: package present means open, absent means spoken sentence" {
        YerelMedyaMantik.RADYO_PAKETI shouldBe "com.miui.fm"
        var sorulan: String? = null
        YerelMedyaMantik.radyoCumlesi { sorulan = it; false } shouldBe "Radyo uygulaması yok"
        sorulan shouldBe "com.miui.fm"
        YerelMedyaMantik.radyoCumlesi { true } shouldBe null
    }

    "manifest declares queries for com.miui.fm and READ_EXTERNAL_STORAGE" {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        manifest.contains("<queries>") shouldBe true
        manifest.contains("com.miui.fm") shouldBe true
        manifest.contains("android.permission.READ_EXTERNAL_STORAGE") shouldBe true
    }
})
