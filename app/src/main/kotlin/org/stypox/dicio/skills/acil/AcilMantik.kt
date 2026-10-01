package org.stypox.dicio.skills.acil

import org.stypox.dicio.config.Kisi
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalTime
import java.util.Locale

/** What the emergency plan needs from the phone; the Android side implements it. */
interface AcilEylemci {
    /** Calls [numara] and blocks until the attempt is over; true if somebody picked up. */
    fun ara(numara: String): Boolean

    /** Sends [metin] to [numara] and blocks until the send result is known; true on success. */
    fun smsGonder(numara: String, metin: String): Boolean
}

/** Android-free decision logic of the emergency skill. */
object AcilMantik {
    const val GERI_SAYIM_MS = 5000L
    const val NUMARA_112 = "112"

    private val IPTAL_KELIMELERI = setOf("dur", "istemiyorum", "iptal")

    enum class SayimSonucu { IPTAL, DEVAM }

    /** Contacts that have an `acil_sira`, ascending; ties keep file order (stable sort). */
    fun acilKisiler(kisiler: List<Kisi>): List<Kisi> =
        kisiler.filter { it.acilSira != null }.sortedBy { it.acilSira }

    /** [iptalMs] is the time of the cancel word since the countdown started, or null. */
    fun geriSayim(iptalMs: Long?, sureMs: Long = GERI_SAYIM_MS): SayimSonucu =
        if (iptalMs != null && iptalMs < sureMs) SayimSonucu.IPTAL else SayimSonucu.DEVAM

    fun iptalKelimesiMi(soz: String): Boolean = soz.trim().lowercase(Locale.forLanguageTag("tr")) in IPTAL_KELIMELERI

    fun smsMetni(
        ad: String,
        enlem: Double?,
        boylam: Double?,
        hucreBilgisi: String?,
        saat: LocalTime,
    ): String {
        val saatMetni = String.format(Locale.ROOT, "(saat %02d:%02d)", saat.hour, saat.minute)
        val konum = if (enlem != null && boylam != null) {
            "Konum: https://maps.google.com/?q=${sayi(enlem)},${sayi(boylam)}"
        } else if (!hucreBilgisi.isNullOrBlank()) {
            "Konum bilinmiyor, baz bilgisi: $hucreBilgisi"
        } else {
            "Konum bilinmiyor"
        }
        return "$ad yardım istiyor. $konum $saatMetni"
    }

    /** Plain decimal with '.' and at most 5 fraction digits (~1 m). */
    private fun sayi(d: Double): String =
        BigDecimal(d).setScale(5, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()

    /**
     * Texts then calls each emergency contact in order until one connects; 112 is called only
     * when nobody did and [acil112] is true. A failed SMS is retried exactly once.
     */
    fun planiYurut(kisiler: List<Kisi>, acil112: Boolean, sms: String, eylemci: AcilEylemci) {
        for (kisi in acilKisiler(kisiler)) {
            if (!eylemci.smsGonder(kisi.numara, sms)) eylemci.smsGonder(kisi.numara, sms)
            if (eylemci.ara(kisi.numara)) return
        }
        if (acil112) eylemci.ara(NUMARA_112)
    }
}
