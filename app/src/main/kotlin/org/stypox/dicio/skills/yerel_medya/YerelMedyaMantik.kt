package org.stypox.dicio.skills.yerel_medya

/** Plain-code logic of the local media skill: no Android classes, so it is unit-testable. */
object YerelMedyaMantik {
    const val KURAN_KLASORU = "/sdcard/Yardimci/kuran"
    const val TURKU_KLASORU = "/sdcard/Yardimci/turku"
    const val RADYO_PAKETI = "com.miui.fm"
    const val SURE_YOK_CUMLESI = "Bu sûre telefonda yok"
    const val KURAN_YOK_CUMLESI = "Telefonda Kur'an yok"
    const val TURKU_YOK_CUMLESI = "Telefonda türkü yok"
    const val RADYO_YOK_CUMLESI = "Radyo uygulaması yok"

    /** Spoken fragment (ascii-folded) -> file name without extension. */
    private val sureler = listOf(
        "yasin" to "yasin",
        "fatiha" to "fatiha",
        "mulk" to "mulk",
        "rahman" to "rahman",
        "amme" to "amme",
    )

    class SureSonucu(val yol: String?, val cumle: String?)

    /** Lowercases the Turkish way and folds diacritics/dotless i so spoken variants compare equal. */
    fun sadelestir(metin: String): String =
        metin.lowercase(java.util.Locale.forLanguageTag("tr"))
            .map {
                when (it) {
                    'â', 'ä' -> 'a'
                    'î', 'ı', 'ï' -> 'i'
                    'û', 'ü' -> 'u'
                    'ö', 'ô' -> 'o'
                    'ş' -> 's'
                    'ç' -> 'c'
                    'ğ' -> 'g'
                    else -> it
                }
            }
            .joinToString("")

    fun sureDosyaAdi(soylenen: String): String? {
        val s = sadelestir(soylenen)
        return sureler.firstOrNull { s.contains(it.first) }?.second?.plus(".mp3")
    }

    fun sureCozumle(soylenen: String, mevcutDosyalar: Set<String>): SureSonucu {
        val ad = sureDosyaAdi(soylenen)
        return if (ad != null && ad in mevcutDosyalar) SureSonucu("$KURAN_KLASORU/$ad", null)
        else SureSonucu(null, SURE_YOK_CUMLESI)
    }

    private fun mp3Yollari(klasor: String, dosyalar: List<String>?): List<String> =
        dosyalar.orEmpty()
            .filter { it.lowercase().endsWith(".mp3") }
            .sorted()
            .map { "$klasor/$it" }

    fun turkuYollari(dosyalar: List<String>?): List<String> = mp3Yollari(TURKU_KLASORU, dosyalar)

    fun kuranYollari(dosyalar: List<String>?): List<String> = mp3Yollari(KURAN_KLASORU, dosyalar)

    /** Null means the radio app exists and may be opened. */
    fun radyoCumlesi(paketVarMi: (String) -> Boolean): String? =
        if (paketVarMi(RADYO_PAKETI)) null else RADYO_YOK_CUMLESI
}
