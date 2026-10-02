package org.stypox.dicio.skills.alarm

import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale

/** Pure-Kotlin parser of spoken Turkish clock times ("sabah yedide") and durations ("on dakika"). */
object TurkceSaatCozucu {
    private val SAYILAR = mapOf(
        "bir" to 1, "iki" to 2, "üç" to 3, "dört" to 4, "dörd" to 4, "beş" to 5, "altı" to 6,
        "yedi" to 7, "sekiz" to 8, "dokuz" to 9, "on" to 10, "yirmi" to 20, "otuz" to 30,
        "kırk" to 40, "elli" to 50,
    )
    private val EKLER = setOf(
        "", "de", "da", "te", "ta", "e", "a", "i", "ı", "u", "ü",
        "ye", "ya", "yi", "yı", "yu", "yü",
    )
    private val TR = Locale("tr")

    private fun parcala(metin: String) = metin.lowercase(TR)
        .split(Regex("[^\\p{L}\\p{N}]+")).filter { it.isNotEmpty() }

    /** Value of the single number word [t] (case suffixes allowed), or null. */
    private fun tekSayi(t: String): Int? {
        t.toIntOrNull()?.let { return it }
        for ((kok, deger) in SAYILAR) {
            if (t.startsWith(kok) && t.substring(kok.length) in EKLER) return deger
        }
        return null
    }

    /** Reads a number at [i] (also "on iki", "yirmi beş"); returns value and next index. */
    private fun sayiOku(t: List<String>, i: Int): Pair<Int, Int>? {
        val ilk = tekSayi(t[i]) ?: return null
        if ((SAYILAR[t[i]] ?: 0) >= 10 && i + 1 < t.size) {
            val birler = tekSayi(t[i + 1])
            if (birler != null && birler in 1..9 && t[i + 1].toIntOrNull() == null) {
                return Pair(ilk + birler, i + 2)
            }
        }
        return Pair(ilk, i + 1)
    }

    /** Parses [text] into the next matching date-time after [now], or null if no hour was found. */
    fun coz(text: String, now: LocalDateTime): LocalDateTime? {
        val t = parcala(text)
        var yarin = false
        var kisim: String? = null
        var saat: Int? = null
        var ofset = 0
        var i = 0
        while (i < t.size) {
            val w = t[i]
            when {
                w == "yarın" -> yarin = true
                w == "sabah" || w == "öğlen" || w == "akşam" || w == "gece" -> kisim = w
                w == "öğle" -> kisim = "öğlen"
                w == "öğleden" && t.getOrNull(i + 1) == "sonra" -> { kisim = "öğleden"; i++ }
                saat == null -> sayiOku(t, i)?.let { (v, j) ->
                    if (v in 1..23) { saat = v; i = j - 1 }
                }
                w.startsWith("buçuk") -> ofset += 30
                w == "çeyrek" && (t.getOrNull(i + 1) == "geçe" || t.getOrNull(i + 1) == "kala") -> {
                    ofset += if (t[i + 1] == "geçe") 15 else -15
                    i++
                }
                else -> sayiOku(t, i)?.let { (v, j) ->
                    val yon = t.getOrNull(j)
                    if (yon == "geçe" || yon == "kala") {
                        ofset += if (yon == "geçe") v else -v
                        i = j
                    }
                }
            }
            i++
        }
        val h = saat ?: return null

        val adaylar: List<Int> = if (h > 12) listOf(h) else when (kisim) {
            "sabah" -> listOf(h % 12)
            "öğlen" -> listOf(if (h in 1..4) h + 12 else h)
            "öğleden" -> listOf(if (h == 12) 12 else h + 12)
            "akşam" -> listOf(h % 12 + 12)
            "gece" -> listOf(if (h == 12) 0 else if (h <= 5) h else h + 12)
            else -> listOf(h % 12, h % 12 + 12)
        }
        val gun = if (yarin) now.toLocalDate().plusDays(1) else now.toLocalDate()
        val zamanlar = adaylar.map { gun.atTime(it, 0).plusMinutes(ofset.toLong()) }.sorted()
        if (yarin) return zamanlar.first()
        return zamanlar.firstOrNull { it.isAfter(now) } ?: zamanlar.first().plusDays(1)
    }

    /** Parses a spoken duration ("on dakika", "yarım saat", "bir buçuk saat"), or null. */
    fun sure(text: String): Duration? {
        val t = parcala(text)
        var toplam = Duration.ZERO
        var i = 0
        while (i < t.size) {
            if (t[i] == "yarım" && t.getOrNull(i + 1)?.startsWith("saat") == true) {
                toplam = toplam.plusMinutes(30)
                i += 2
                continue
            }
            val oku = sayiOku(t, i)
            if (oku == null) { i++; continue }
            val (v, sonraki) = oku
            val yarim = t.getOrNull(sonraki)?.startsWith("buçuk") == true
            val j = if (yarim) sonraki + 1 else sonraki
            val birim = t.getOrNull(j) ?: break
            val saniye = when {
                birim.startsWith("saat") -> v * 3600L + (if (yarim) 1800 else 0)
                birim.startsWith("dakika") || birim == "dk" -> v * 60L + (if (yarim) 30 else 0)
                birim.startsWith("saniye") || birim == "sn" -> v.toLong()
                else -> { i++; continue }
            }
            toplam = toplam.plusSeconds(saniye)
            i = j + 1
        }
        return toplam.takeIf { !it.isZero }
    }

    /** Spoken Turkish form of a duration, e.g. "on dakika", "iki saat otuz dakika". */
    fun sureMetni(sure: Duration): String {
        val toplam = sure.seconds.coerceAtLeast(0)
        val parcalar = ArrayList<String>()
        val saat = (toplam / 3600).toInt()
        val dakika = (toplam % 3600 / 60).toInt()
        val saniye = (toplam % 60).toInt()
        if (saat > 0) parcalar.add("${org.stypox.dicio.util.TurkceSayi.sayi(saat)} saat")
        if (dakika > 0) parcalar.add("${org.stypox.dicio.util.TurkceSayi.sayi(dakika)} dakika")
        if (saniye > 0 || parcalar.isEmpty()) {
            parcalar.add("${org.stypox.dicio.util.TurkceSayi.sayi(saniye)} saniye")
        }
        return parcalar.joinToString(" ")
    }
}
