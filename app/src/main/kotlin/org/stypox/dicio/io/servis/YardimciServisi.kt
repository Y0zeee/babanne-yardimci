package org.stypox.dicio.io.servis

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import org.stypox.dicio.R
import org.stypox.dicio.skills.pil.PilKonusma
import org.stypox.dicio.skills.pil.PilOlaylari
import org.stypox.dicio.skills.pil.pilSarjda
import org.stypox.dicio.skills.pil.pilYuzdesi

/** Always-on service: low battery warning and ezan alarms, independent of the wake word. */
class YardimciServisi : Service() {
    // Runtime receiver: manifest receivers cannot get BATTERY_CHANGED/POWER_CONNECTED on Android 8+
    private val pilOlaylari = PilOlaylari()
    private var pilKonusma: PilKonusma? = null
    private val pilReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val metin = when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> pilOlaylari.sarjaTakildi()
                Intent.ACTION_BATTERY_CHANGED -> {
                    val yuzde = pilYuzdesi(intent) ?: return
                    pilOlaylari.seviyeDegisti(yuzde, pilSarjda(intent))
                }
                else -> null
            }
            if (metin != null) pilKonusma?.soyle(metin)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        pilKonusma = PilKonusma(this)
        val ilk = registerReceiver(pilReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
        })
        pilYuzdesi(ilk)?.let { pilOlaylari.baslat(it, pilSarjda(ilk)) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            bildirimOlustur()
        } catch (t: Throwable) {
            Log.e(TAG, "Kalıcı bildirim oluşturulamadı", t)
            stopSelf()
            return START_NOT_STICKY
        }
        EzanAlarmReceiver.planla(this)
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(pilReceiver)
        } catch (_: IllegalArgumentException) {
        }
        pilKonusma?.kapat()
        super.onDestroy()
    }

    private fun bildirimOlustur() {
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(
                KANAL_ID,
                getString(R.string.yardimci_servisi_label),
                NotificationManager.IMPORTANCE_LOW,
            ))
        }
        val bildirim = NotificationCompat.Builder(this, KANAL_ID)
            .setSmallIcon(R.drawable.ic_hearing_white)
            .setContentTitle(getString(R.string.yardimci_servisi_bildirim))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setShowWhen(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        ServiceCompat.startForeground(
            this, BILDIRIM_ID, bildirim,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0,
        )
    }

    companion object {
        private val TAG = YardimciServisi::class.simpleName
        private const val KANAL_ID = "org.stypox.dicio.io.servis.YardimciServisi"
        private const val BILDIRIM_ID = 19803673

        /** Call from a foreground part of the app or from BOOT_COMPLETED. */
        fun baslat(context: Context) {
            try {
                ContextCompat.startForegroundService(
                    context, Intent(context, YardimciServisi::class.java))
            } catch (e: Exception) {
                Log.e(TAG, "YardimciServisi başlatılamadı", e)
            }
        }
    }
}
