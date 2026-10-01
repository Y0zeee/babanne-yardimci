package org.stypox.dicio.skills.pil

import org.stypox.dicio.util.TurkceSayi

const val DUSUK_PIL_ESIGI = 20
const val DUSUK_PIL_CUMLESI = "Şarjın azaldı, şarja tak"
const val SARJA_TAKILDI_CUMLESI = "Şarja takıldı"

/** E.g. 40 -> "Şarjın yüzde kırk". */
fun pilSeviyeCumlesi(yuzde: Int): String =
    "Şarjın yüzde " + TurkceSayi.sayi(yuzde.coerceIn(0, 100))

/** Warns once when the level falls below the threshold; re-armed by charging or a higher level. */
class DusukPilTakipcisi {
    private var uyarildi = false

    /** Returns true if the low battery warning should be spoken now. */
    fun guncelle(yuzde: Int, sarjda: Boolean): Boolean {
        if (sarjda || yuzde >= DUSUK_PIL_ESIGI) {
            uyarildi = false
            return false
        }
        if (uyarildi) return false
        uyarildi = true
        return true
    }
}

/** Turns battery events into the sentence to speak (or null), keeping all the state. */
class PilOlaylari {
    private val dusuk = DusukPilTakipcisi()
    private var sarjda: Boolean? = null

    /** Remembers the initial state without speaking anything. */
    fun baslat(yuzde: Int, sarjda: Boolean) {
        this.sarjda = sarjda
        dusuk.guncelle(yuzde, sarjda)
    }

    fun sarjaTakildi(): String? {
        val onceki = sarjda
        sarjda = true
        return if (onceki == true) null else SARJA_TAKILDI_CUMLESI
    }

    fun seviyeDegisti(yuzde: Int, sarjda: Boolean): String? {
        val takildi = sarjda && this.sarjda == false
        this.sarjda = sarjda
        if (dusuk.guncelle(yuzde, sarjda)) return DUSUK_PIL_CUMLESI
        return if (takildi) SARJA_TAKILDI_CUMLESI else null
    }
}
