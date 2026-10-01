package org.stypox.dicio.skills.namaz_vakti

import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

data class Vakitler(
    val imsak: LocalTime,
    val gunes: LocalTime,
    val ogle: LocalTime,
    val ikindi: LocalTime,
    val aksam: LocalTime,
    val yatsi: LocalTime,
)

/**
 * Offline prayer-time calculation (pure Kotlin, no dependency).
 *
 * Method: Diyanet İşleri Başkanlığı (Turkey), as in the batoulapps/adhan `TURKEY` method
 * (https://github.com/batoulapps/adhan-java, MIT; algorithms from Jean Meeus, "Astronomical
 * Algorithms", and the NOAA solar calculator):
 *  - imsak (fajr) at sun depression 18°, yatsı (isha) at 17°;
 *  - güneş/akşam at sun altitude -0.833° (refraction + solar radius);
 *  - ikindi (asr): Shafi'i/Diyanet, shadow length = object length + noon shadow (factor 1);
 *  - temkin (ihtiyat) minutes added by Diyanet: güneş -7, öğle +5, ikindi +4, akşam +7
 *    (imsak and yatsı 0).
 * The parameters are taken from memory of those sources (no network while writing); results are
 * expected to be within about ±2 minutes of the published Diyanet tables.
 */
object NamazHesap {
    private const val IMSAK_ACI = 18.0
    private const val YATSI_ACI = 17.0
    private const val GUNES_ACI = 0.833
    private const val TEMKIN_GUNES = -7
    private const val TEMKIN_OGLE = 5
    private const val TEMKIN_IKINDI = 4
    private const val TEMKIN_AKSAM = 7

    private fun rad(d: Double) = Math.toRadians(d)
    private fun deg(r: Double) = Math.toDegrees(r)
    private fun norm360(d: Double) = d - 360.0 * floor(d / 360.0)

    private class Gunes(val declination: Double, val equationOfTimeMin: Double)

    private fun gunesKonumu(jd: Double): Gunes {
        val t = (jd - 2451545.0) / 36525.0
        val l0 = norm360(280.4664567 + 36000.76983 * t + 0.0003032 * t * t)
        val m = rad(norm360(357.52911 + 35999.05029 * t - 0.0001537 * t * t))
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(m) +
            (0.019993 - 0.000101 * t) * sin(2 * m) + 0.000289 * sin(3 * m)
        val omega = rad(125.04 - 1934.136 * t)
        val lambda = rad(l0 + c - 0.00569 - 0.00478 * sin(omega))
        val eps = rad(23.439291 - 0.0130042 * t + 0.00256 * cos(omega))
        val decl = asin(sin(eps) * sin(lambda))
        val ra = norm360(deg(atan2(cos(eps) * sin(lambda), cos(lambda))))
        var eot = l0 - 0.0057183 - ra
        eot -= 360.0 * Math.round(eot / 360.0)
        return Gunes(decl, eot * 4.0)
    }

    /**
     * Hour angle in hours for the sun at [yukseklik] degrees (negative = below the horizon),
     * clamped so extreme latitudes degrade instead of producing NaN.
     */
    private fun saatAcisi(enlem: Double, decl: Double, yukseklik: Double): Double {
        val phi = rad(enlem)
        val cosH = (sin(rad(yukseklik)) - sin(phi) * sin(decl)) / (cos(phi) * cos(decl))
        return deg(acos(cosH.coerceIn(-1.0, 1.0))) / 15.0
    }

    /**
     * @param utcDakika offset of the local time zone from UTC, in minutes (Turkey: 180)
     */
    fun hesapla(enlem: Double, boylam: Double, tarih: LocalDate, utcDakika: Int): Vakitler {
        val jd0 = tarih.toEpochDay() + 2440587.5 // Julian day at 0h UT
        // solar noon in UT hours, refined twice with the sun position at that moment
        var ogleUt = 12.0 - boylam / 15.0
        repeat(2) {
            val g = gunesKonumu(jd0 + ogleUt / 24.0)
            ogleUt = 12.0 - boylam / 15.0 - g.equationOfTimeMin / 60.0
        }
        val g = gunesKonumu(jd0 + ogleUt / 24.0)

        val yerelOgle = ogleUt + utcDakika / 60.0 // exact (without temkin), local hours
        val asrYuksekligi = deg(atan(1.0 / (1.0 + tan(abs(rad(enlem) - g.declination)))))

        fun dakika(saat: Double, temkin: Int): LocalTime {
            val toplam = (saat * 60.0).roundToInt() + temkin
            return LocalTime.ofSecondOfDay((((toplam % 1440) + 1440) % 1440) * 60L)
        }

        val gunesH = saatAcisi(enlem, g.declination, -GUNES_ACI)
        return Vakitler(
            imsak = dakika(yerelOgle - saatAcisi(enlem, g.declination, -IMSAK_ACI), 0),
            gunes = dakika(yerelOgle - gunesH, TEMKIN_GUNES),
            ogle = dakika(yerelOgle, TEMKIN_OGLE),
            ikindi = dakika(yerelOgle + saatAcisi(enlem, g.declination, asrYuksekligi), TEMKIN_IKINDI),
            aksam = dakika(yerelOgle + gunesH, TEMKIN_AKSAM),
            yatsi = dakika(yerelOgle + saatAcisi(enlem, g.declination, -YATSI_ACI), 0),
        )
    }
}
