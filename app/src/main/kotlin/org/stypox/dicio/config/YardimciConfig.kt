package org.stypox.dicio.config

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import java.io.File

data class Kisi(
    val adlar: List<String>,
    val numara: String,
    val acilSira: Int?,
    val otomatikAc: Boolean,
)

data class Ilac(val ad: String, val saat: String)

/** Fallback location (used when there is no GPS fix), e.g. the grandmother's district centre. */
data class KonumYedek(val enlem: Double, val boylam: Double, val ilce: String?)

data class YardimciConfig(
    val kisiler: List<Kisi> = emptyList(),
    val konumYedek: KonumYedek? = null,
    val ilaclar: List<Ilac> = emptyList(),
    val ezanSesi: Boolean = false,
    val ttsHizi: Float = DEFAULT_TTS_HIZI,
    val acil112: Boolean = true,
    val sahipAdi: String = DEFAULT_SAHIP_ADI,
) {
    /** Finds the configured person whose nickname matches [spoken] (suffixes are ignored). */
    fun kisiBul(spoken: String): Kisi? {
        val wanted = TakmaAd.normalize(spoken)
        if (wanted.isEmpty()) return null
        return kisiler.firstOrNull { k -> k.adlar.any { TakmaAd.normalize(it) == wanted } }
    }

    /** Finds the configured person owning [number], comparing the last 10 digits. */
    fun numarayaGoreKisi(number: String): Kisi? {
        val wanted = number.filter { it.isDigit() }.takeLast(NUMARA_HANE)
        if (wanted.isEmpty()) return null
        return kisiler.firstOrNull { it.numara.filter { c -> c.isDigit() }.takeLast(NUMARA_HANE) == wanted }
    }

    companion object {
        private const val NUMARA_HANE = 10
        const val DEFAULT_TTS_HIZI = 0.8f
        const val DEFAULT_SAHIP_ADI = "Babaanne"
        const val FILE_NAME = "yardımcı.json"

        fun parse(text: String?): YardimciConfig {
            if (text.isNullOrBlank()) return YardimciConfig()
            return try {
                val root = Json.parseToJsonElement(text) as? JsonObject ?: return YardimciConfig()
                YardimciConfig(
                    kisiler = (root["kisiler"] as? JsonArray).orEmpty().mapNotNull(::parseKisi),
                    konumYedek = parseKonumYedek(root["konum_yedek"]),
                    ilaclar = (root["ilaclar"] as? JsonArray).orEmpty().mapNotNull(::parseIlac),
                    ezanSesi = (root["ezan_sesi"] as? JsonPrimitive)?.booleanOrNull ?: false,
                    ttsHizi = (root["tts_hizi"] as? JsonPrimitive)?.floatOrNull
                        ?.takeIf { it > 0f } ?: DEFAULT_TTS_HIZI,
                    acil112 = (root["acil_112"] as? JsonPrimitive)?.booleanOrNull ?: true,
                    sahipAdi = (root["sahip_adi"] as? JsonPrimitive)?.contentOrNull
                        ?.takeIf { it.isNotBlank() } ?: DEFAULT_SAHIP_ADI,
                )
            } catch (e: Exception) {
                YardimciConfig()
            }
        }

        private fun parseKisi(e: JsonElement): Kisi? {
            val o = e as? JsonObject ?: return null
            val adlar = (o["adlar"] as? JsonArray).orEmpty()
                .mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
            val numara = (o["numara"] as? JsonPrimitive)?.contentOrNull ?: return null
            if (adlar.isEmpty()) return null
            return Kisi(
                adlar = adlar,
                numara = numara,
                acilSira = (o["acil_sira"] as? JsonPrimitive)?.intOrNull,
                otomatikAc = (o["otomatik_ac"] as? JsonPrimitive)?.booleanOrNull ?: false,
            )
        }

        private fun parseKonumYedek(e: JsonElement?): KonumYedek? {
            val o = e as? JsonObject ?: return null
            val enlem = (o["enlem"] as? JsonPrimitive)?.doubleOrNull ?: return null
            val boylam = (o["boylam"] as? JsonPrimitive)?.doubleOrNull ?: return null
            return KonumYedek(enlem, boylam, (o["ilce"] as? JsonPrimitive)?.contentOrNull)
        }

        private fun parseIlac(e: JsonElement): Ilac? {
            val o = e as? JsonObject ?: return null
            val ad = (o["ad"] as? JsonPrimitive)?.contentOrNull ?: return null
            return Ilac(ad, (o["saat"] as? JsonPrimitive)?.contentOrNull ?: "")
        }

        /** Reads `<externalFilesDir>/yardımcı.json`; any problem yields the defaults. */
        fun load(context: Context): YardimciConfig = try {
            val dir = context.getExternalFilesDir(null)
            val file = if (dir == null) null else File(dir, FILE_NAME)
            parse(if (file != null && file.isFile) file.readText() else null)
        } catch (e: Exception) {
            YardimciConfig()
        }
    }
}
