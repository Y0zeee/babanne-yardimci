package org.stypox.dicio.util

import java.time.LocalTime

/** Spells numbers and clock times in Turkish words, so that text-to-speech never reads digits. */
object TurkceSayi {
    private val BIRLER = listOf("", "bir", "iki", "üç", "dört", "beş", "altı", "yedi", "sekiz", "dokuz")
    private val ONLAR = listOf("", "on", "yirmi", "otuz", "kırk", "elli", "altmış", "yetmiş", "seksen", "doksan")

    /** Spells [n] (0..999999) e.g. 2026 -> "iki bin yirmi altı". */
    fun sayi(n: Int): String {
        require(n in 0..999_999) { "out of range: $n" }
        if (n == 0) return "sıfır"
        val parts = ArrayList<String>()
        val bin = n / 1000
        if (bin > 0) {
            if (bin > 1) parts.add(ucBasamak(bin))
            parts.add("bin")
        }
        ucBasamak(n % 1000).takeIf { it.isNotEmpty() }?.let { parts.add(it) }
        return parts.joinToString(" ")
    }

    private fun ucBasamak(n: Int): String {
        val parts = ArrayList<String>()
        val yuz = n / 100
        if (yuz > 0) {
            if (yuz > 1) parts.add(BIRLER[yuz])
            parts.add("yüz")
        }
        if (n / 10 % 10 > 0) parts.add(ONLAR[n / 10 % 10])
        if (n % 10 > 0) parts.add(BIRLER[n % 10])
        return parts.joinToString(" ")
    }

    /** Spells a clock time: 12:32 -> "on iki otuz iki", 12:00 -> "on iki". */
    fun saat(t: LocalTime): String {
        val s = sayi(t.hour)
        return when {
            t.minute == 0 -> s
            t.minute < 10 -> "$s sıfır ${sayi(t.minute)}"
            else -> "$s ${sayi(t.minute)}"
        }
    }

    /** Appends the locative suffix (-da/-de/-ta/-te) following vowel harmony and consonant hardness. */
    fun bulunmaEki(soz: String): String {
        val sonSesli = soz.lastOrNull { it in "aeıioöuü" } ?: 'e'
        val kalin = sonSesli in "aıou"
        val sert = soz.last() in "çfhkpsşt"
        return soz + (if (sert) "t" else "d") + (if (kalin) "a" else "e")
    }
}
