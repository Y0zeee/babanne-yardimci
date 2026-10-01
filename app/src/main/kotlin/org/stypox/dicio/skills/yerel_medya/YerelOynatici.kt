package org.stypox.dicio.skills.yerel_medya

import android.media.MediaPlayer

/** The single shared player: starting something new always stops what played before. */
object YerelOynatici {
    private var oynatici: MediaPlayer? = null

    @Synchronized
    fun cal(yollar: List<String>, karisik: Boolean = false): Boolean {
        dur()
        val sira = if (karisik) yollar.shuffled() else yollar
        return sira.isNotEmpty() && sonrakiniCal(sira, 0)
    }

    private fun sonrakiniCal(sira: List<String>, indeks: Int): Boolean {
        val mp = MediaPlayer()
        return try {
            mp.setDataSource(sira[indeks])
            mp.setOnCompletionListener {
                synchronized(this) {
                    if (oynatici === mp) {
                        dur()
                        if (indeks + 1 < sira.size) sonrakiniCal(sira, indeks + 1)
                    }
                }
            }
            mp.setOnErrorListener { _, _, _ ->
                synchronized(this) { if (oynatici === mp) dur() }
                true
            }
            mp.prepare()
            oynatici = mp
            mp.start()
            true
        } catch (e: Exception) {
            mp.release()
            false
        }
    }

    @Synchronized
    fun dur() {
        oynatici?.let {
            try {
                it.stop()
            } catch (_: IllegalStateException) {
            }
            it.release()
        }
        oynatici = null
    }
}
