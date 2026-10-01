package org.stypox.dicio.skills.cevapsiz

import org.stypox.dicio.config.YardimciConfig
import org.stypox.dicio.util.TurkceSayi

data class CevapsizArama(val numara: String, val zaman: Long)

data class CevapsizSonuc(val cumle: String, val aranacakNumara: String?)

const val CEVAPSIZ_PENCERE_MS = 24L * 60 * 60 * 1000
private const val BILINMEYEN = "bilinmeyen numara"

fun cevapsizSon24Saat(liste: List<CevapsizArama>, simdi: Long): List<CevapsizArama> =
    liste.filter { it.zaman in (simdi - CEVAPSIZ_PENCERE_MS)..simdi }

private class Grup(val ad: String, val numara: String, var adet: Int)

fun cevapsizSonucu(liste: List<CevapsizArama>, simdi: Long, config: YardimciConfig): CevapsizSonuc {
    val son = cevapsizSon24Saat(liste, simdi)
    if (son.isEmpty()) return CevapsizSonuc("Cevapsız arama yok", null)

    val gruplar = LinkedHashMap<String, Grup>()
    for (arama in son.sortedByDescending { it.zaman }) {
        val kisi = config.numarayaGoreKisi(arama.numara)
        val anahtar = kisi?.numara ?: arama.numara.filter { it.isDigit() }.takeLast(10)
        gruplar.getOrPut(anahtar) {
            Grup(kisi?.adlar?.first() ?: BILINMEYEN, kisi?.numara ?: arama.numara, 0)
        }.adet++
    }

    val parcalar = gruplar.values.map { it.ad + " " + TurkceSayi.sayi(it.adet) + " kere" }
    val cumle = if (parcalar.size == 1) {
        "Bugün " + parcalar[0] + " aradı"
    } else {
        "Bugün " + parcalar.dropLast(1).joinToString(", ") + " ve " + parcalar.last() + " aradı"
    }
    return CevapsizSonuc(cumle, gruplar.values.first().numara)
}
