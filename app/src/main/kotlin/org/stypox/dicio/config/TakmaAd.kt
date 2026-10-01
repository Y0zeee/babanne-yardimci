package org.stypox.dicio.config

import java.util.Locale

/** Maps Turkish suffixed nicknames (oğlumu, kızıma, bizim oğlanı...) to their base form. */
object TakmaAd {
    private val TR = Locale("tr", "TR")

    // possessive nicknames ending in "m" (oğlum, kızım) + a case suffix
    private val POSSESSIVE = Regex("^(.*m)(?:nın|nin|nun|nün|ın|in|un|ün|ı|i|u|ü|a|e|n)$")

    // nicknames like "oğlan" + a case suffix
    private val OGLAN = Regex("^(.*lan)(?:ın|in|ı|i|a|e)$")

    fun normalize(text: String): String {
        val lower = text.trim().lowercase(TR).replace(Regex("\\s+"), " ")
        val last = lower.substringAfterLast(' ')
        val prefix = lower.removeSuffix(last)
        return prefix + stripSuffix(last)
    }

    private fun stripSuffix(word: String): String {
        POSSESSIVE.find(word)?.let { return it.groupValues[1] }
        OGLAN.find(word)?.let { return it.groupValues[1] }
        return word
    }
}
