package org.stypox.dicio.ui.kurulum

/** Plain-code logic of the family setup screen: no Android classes, so it is unit-testable. */
object KurulumMantik {
    const val IZIN_VAR = "Tamam"
    const val IZIN_YOK = "Eksik"

    class IzinGorunumu(val yesil: Boolean, val etiket: String, val dugmeGorunur: Boolean)

    fun izinGorunumu(verildi: Boolean): IzinGorunumu =
        if (verildi) IzinGorunumu(true, IZIN_VAR, false) else IzinGorunumu(false, IZIN_YOK, true)

    /** Hides all but the last two digits, e.g. "+90 555 000 00 07" -> "***07". */
    fun maskeleNumara(numara: String): String {
        val son = numara.filter { it.isDigit() }.takeLast(2)
        return if (son.isEmpty()) "" else "***$son"
    }

    /** Counts the names ending in ".mp3" (any case); a missing folder (null) counts as zero. */
    fun mp3Say(adlar: List<String>?): Int =
        adlar.orEmpty().count { it.lowercase().endsWith(".mp3") }
}
