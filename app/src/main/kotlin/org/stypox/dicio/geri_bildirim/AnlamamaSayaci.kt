package org.stypox.dicio.geri_bildirim

import org.stypox.dicio.config.Kisi

sealed interface AnlamamaAdimi {
    data object TekrarIste : AnlamamaAdimi
    data object AramaMiVakitMi : AnlamamaAdimi
    data class KisiyiAra(val kisi: Kisi) : AnlamamaAdimi
}

/** Immutable counter of consecutive "not understood" results. */
data class AnlamamaSayaci(val ardisikBasarisiz: Int = 0) {
    fun basarisiz() = copy(ardisikBasarisiz = ardisikBasarisiz + 1)

    fun basarili() = AnlamamaSayaci()

    /** [acilKisi] is the person with acil_sira == 1, or null if none is configured. */
    fun adim(acilKisi: Kisi?): AnlamamaAdimi = when {
        ardisikBasarisiz <= 1 -> AnlamamaAdimi.TekrarIste
        ardisikBasarisiz == 2 || acilKisi == null -> AnlamamaAdimi.AramaMiVakitMi
        else -> AnlamamaAdimi.KisiyiAra(acilKisi)
    }
}
