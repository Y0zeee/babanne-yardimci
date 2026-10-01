package org.stypox.dicio.skills.sms_oku

import org.stypox.dicio.config.YardimciConfig

data class OkunmamisSms(val gonderen: String, val metin: String)

private const val KISA_KOD_SINIRI = 6
private const val BILINMEYEN = "bilinmeyen numara"
private const val REKLAM_NOTU = "Bir de reklam mesajı var"

/** Alphanumeric sender ids (TURKCELL) and short codes (3434) are operator/ad messages. */
fun smsGonderenReklamMi(gonderen: String): Boolean {
    val g = gonderen.filterNot { it.isWhitespace() }
    if (g.isEmpty()) return true
    if (g.any { it.isLetter() }) return true
    return !g.startsWith("+") && !g.startsWith("0") && g.count { it.isDigit() } <= KISA_KOD_SINIRI
}

fun smsOkuCumlesi(mesajlar: List<OkunmamisSms>, config: YardimciConfig): String {
    val (reklam, gercek) = mesajlar.partition { smsGonderenReklamMi(it.gonderen) }
    val parcalar = gercek.map {
        val ad = config.numarayaGoreKisi(it.gonderen)?.adlar?.first() ?: BILINMEYEN
        ad + " yazmış: " + it.metin.trim()
    }.toMutableList()
    if (parcalar.isEmpty()) parcalar.add("Yeni mesaj yok")
    if (reklam.isNotEmpty()) parcalar.add(REKLAM_NOTU)
    return parcalar.joinToString(". ")
}
