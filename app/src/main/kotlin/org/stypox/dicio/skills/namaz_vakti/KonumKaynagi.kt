package org.stypox.dicio.skills.namaz_vakti

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import org.stypox.dicio.config.YardimciConfig

data class Konum(val enlem: Double, val boylam: Double)

/**
 * Finds the position offline: last GPS fix (cached in SharedPreferences), else the cached fix,
 * else the `konum_yedek` of yardımcı.json, else null.
 */
object KonumKaynagi {
    private const val PREFS = "namaz_konum"
    private const val ENLEM = "enlem"
    private const val BOYLAM = "boylam"

    @SuppressLint("MissingPermission") // checked below
    fun bul(context: Context, config: YardimciConfig): Konum? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        try {
            val izin = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
            if (izin) {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let {
                    prefs.edit()
                        .putLong(ENLEM, it.latitude.toRawBits())
                        .putLong(BOYLAM, it.longitude.toRawBits())
                        .apply()
                    return Konum(it.latitude, it.longitude)
                }
            }
        } catch (e: Exception) {
            // fall through to the cache and the fallback
        }
        if (prefs.contains(ENLEM) && prefs.contains(BOYLAM)) {
            return Konum(
                Double.fromBits(prefs.getLong(ENLEM, 0)),
                Double.fromBits(prefs.getLong(BOYLAM, 0)),
            )
        }
        return config.konumYedek?.let { Konum(it.enlem, it.boylam) }
    }
}
