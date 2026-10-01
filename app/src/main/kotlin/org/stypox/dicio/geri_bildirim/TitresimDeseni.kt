package org.stypox.dicio.geri_bildirim

enum class GeriBildirim { LISTENING, UNDERSTOOD, NOT_UNDERSTOOD }

/** Vibration patterns as alternating pause/vibrate durations in ms (always starting with a pause). */
object TitresimDeseni {
    private const val KISA = 80L
    private const val UZUN = 600L
    private const val ARA = 120L

    fun sec(geriBildirim: GeriBildirim): LongArray = when (geriBildirim) {
        GeriBildirim.LISTENING -> longArrayOf(0, KISA)
        GeriBildirim.UNDERSTOOD -> longArrayOf(0, KISA, ARA, KISA)
        GeriBildirim.NOT_UNDERSTOOD -> longArrayOf(0, UZUN)
    }
}
