package org.stypox.dicio.skills.acil

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.provider.CallLog
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import org.stypox.dicio.config.YardimciConfig
import org.stypox.dicio.skills.telephone.TelephoneSkill
import java.time.LocalTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/** Android side of the emergency plan: real calls, SMS and location. Blocking; run off-main. */
@SuppressLint("MissingPermission")
class AcilYurutucu(private val context: Context) : AcilEylemci {

    override fun ara(numara: String): Boolean {
        val baslangic = System.currentTimeMillis()
        try {
            TelephoneSkill.call(context, numara)
        } catch (e: Exception) {
            Log.e(TAG, "call failed", e)
            return false
        }
        val bitis = baslangic + ARAMA_PENCERESI_MS
        var aramaBasladi = false
        while (System.currentTimeMillis() < bitis) {
            Thread.sleep(YOKLAMA_MS)
            if (baglandiMi(baslangic)) return true
            val durum = aramaDurumu()
            if (durum == TelephonyManager.CALL_STATE_OFFHOOK) {
                aramaBasladi = true
            } else if (aramaBasladi && durum == TelephonyManager.CALL_STATE_IDLE) {
                // call ended; the log entry may lag a little
                Thread.sleep(YOKLAMA_MS)
                return baglandiMi(baslangic)
            }
        }
        // heuristic: still off-hook after the whole window, so it was most likely answered
        return aramaDurumu() == TelephonyManager.CALL_STATE_OFFHOOK
    }

    private fun baglandiMi(baslangic: Long): Boolean = try {
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.DURATION),
            CallLog.Calls.DATE + " >= ? AND " + CallLog.Calls.TYPE + " = ?",
            arrayOf(baslangic.toString(), CallLog.Calls.OUTGOING_TYPE.toString()),
            null,
        )?.use { c ->
            var sure = false
            while (c.moveToNext()) if (c.getLong(0) > 0) sure = true
            sure
        } ?: false
    } catch (e: Exception) {
        false
    }

    @Suppress("DEPRECATION")
    private fun aramaDurumu(): Int = try {
        (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).callState
    } catch (e: Exception) {
        TelephonyManager.CALL_STATE_IDLE
    }

    override fun smsGonder(numara: String, metin: String): Boolean {
        val eylem = "${context.packageName}.ACIL_SMS_${System.nanoTime()}"
        return try {
            val yonetici = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val parcalar = yonetici.divideMessage(metin)
            val bekleyen = CountDownLatch(parcalar.size)
            val basarisiz = AtomicInteger(0)
            val alici = object : BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: Intent?) {
                    if (resultCode != android.app.Activity.RESULT_OK) basarisiz.incrementAndGet()
                    bekleyen.countDown()
                }
            }
            ContextCompat.registerReceiver(
                context, alici, IntentFilter(eylem), ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            try {
                val gonderildi = ArrayList<PendingIntent>()
                for (i in parcalar.indices) {
                    gonderildi.add(
                        PendingIntent.getBroadcast(
                            context, i, Intent(eylem).setPackage(context.packageName),
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                        )
                    )
                }
                yonetici.sendMultipartTextMessage(numara, null, parcalar, gonderildi, null)
                bekleyen.await(SMS_BEKLEME_MS, TimeUnit.MILLISECONDS) && basarisiz.get() == 0
            } finally {
                context.unregisterReceiver(alici)
            }
        } catch (e: Exception) {
            Log.e(TAG, "sms failed", e)
            false
        }
    }

    /** Builds the SMS text from the best location source available right now. */
    fun smsMetni(sahipAdi: String): String {
        val konum = konumBul()
        val yedek = if (konum == null) YardimciConfig.load(context).konumYedek else null
        val enlem = konum?.latitude ?: yedek?.enlem
        val boylam = konum?.longitude ?: yedek?.boylam
        val hucre = if (enlem == null) hucreOzeti() else null
        return AcilMantik.smsMetni(sahipAdi, enlem, boylam, hucre, LocalTime.now())
    }

    private fun konumBul(): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null
        val taze = tazeKonum(lm)
        if (taze != null) return taze
        return try {
            lm.getProviders(true).mapNotNull { lm.getLastKnownLocation(it) }
                .maxByOrNull { it.time }
        } catch (e: Exception) {
            null
        }
    }

    /** A short fresh fix (API 30+); null on timeout or when unsupported. */
    private fun tazeKonum(lm: LocationManager): Location? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        return try {
            val saglayici = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                .firstOrNull { lm.isProviderEnabled(it) } ?: return null
            val kilit = CountDownLatch(1)
            var sonuc: Location? = null
            lm.getCurrentLocation(saglayici, null, ContextCompat.getMainExecutor(context)) {
                sonuc = it
                kilit.countDown()
            }
            kilit.await(TAZE_KONUM_MS, TimeUnit.MILLISECONDS)
            sonuc
        } catch (e: Exception) {
            null
        }
    }

    private fun hucreOzeti(): String? = try {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val hucreler: List<CellInfo> = tm.allCellInfo.orEmpty()
        val h = hucreler.firstOrNull { it.isRegistered } ?: hucreler.firstOrNull()
        when (h) {
            is CellInfoLte -> "LTE hücre ${h.cellIdentity.ci} bölge ${h.cellIdentity.tac}"
            is CellInfoGsm -> "GSM hücre ${h.cellIdentity.cid} bölge ${h.cellIdentity.lac}"
            null -> null
            else -> h.javaClass.simpleName.removePrefix("CellInfo") + " hücresi"
        }
    } catch (e: Exception) {
        null
    }

    private companion object {
        const val TAG = "AcilYurutucu"
        const val ARAMA_PENCERESI_MS = 30_000L
        const val YOKLAMA_MS = 1_000L
        const val SMS_BEKLEME_MS = 30_000L
        const val TAZE_KONUM_MS = 8_000L
    }
}
