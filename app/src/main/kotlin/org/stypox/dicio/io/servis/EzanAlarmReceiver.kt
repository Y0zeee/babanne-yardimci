package org.stypox.dicio.io.servis

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.stypox.dicio.config.YardimciConfig
import org.stypox.dicio.skills.namaz_vakti.EZAN_DOSYASI
import org.stypox.dicio.skills.namaz_vakti.EzanEylemi
import org.stypox.dicio.skills.namaz_vakti.EzanPlani
import org.stypox.dicio.skills.namaz_vakti.KonumKaynagi
import org.stypox.dicio.skills.namaz_vakti.NamazHesap
import org.stypox.dicio.skills.pil.PilKonusma
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Fires at ezan time: plays ezan.mp3 or speaks the announcement, then schedules the next one. */
class EzanAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val ad = intent.getStringExtra(EXTRA_AD)
        if (ad != null) {
            val config = YardimciConfig.load(context)
            val eylem = EzanPlani.ezanEylemi(config.ezanSesi, File(EZAN_DOSYASI).exists())
            try {
                if (eylem == EzanEylemi.OYNAT) oynat() else soyle(context, ad)
            } catch (e: Exception) {
                Log.e(TAG, "Ezan çalınamadı", e)
            }
        }
        planla(context)
    }

    private fun oynat() {
        val mp = MediaPlayer()
        mp.setDataSource(EZAN_DOSYASI)
        mp.setOnCompletionListener { it.release() }
        mp.setOnErrorListener { p, _, _ -> p.release(); true }
        mp.prepare()
        mp.start()
    }

    private fun soyle(context: Context, ad: String) {
        val konusma = PilKonusma(context)
        // TTS initializes asynchronously: give it time, then release it
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({ konusma.soyle(EzanPlani.ezanCumlesi(ad)) }, 2000)
        handler.postDelayed({ konusma.kapat() }, 15000)
    }

    companion object {
        private val TAG = EzanAlarmReceiver::class.simpleName
        private const val EXTRA_AD = "ezan_adi"

        /** Schedules the next ezan with setAlarmClock; does nothing (but logs) without a location. */
        fun planla(context: Context) {
            val config = YardimciConfig.load(context)
            val konum = KonumKaynagi.bul(context, config)
            if (konum == null) {
                Log.w(TAG, "Konum yok, ezan alarmı kurulmadı")
                return
            }
            val simdi = LocalDateTime.now()
            val bugun = simdi.toLocalDate()
            val ezan = EzanPlani.sonraki(
                hesapla(konum.enlem, konum.boylam, bugun),
                simdi,
                hesapla(konum.enlem, konum.boylam, bugun.plusDays(1)),
            )

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, EzanAlarmReceiver::class.java).putExtra(EXTRA_AD, ezan.ad)
            val pi = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val ms = ezan.zaman.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            try {
                am.setAlarmClock(AlarmManager.AlarmClockInfo(ms, pi), pi)
                Log.d(TAG, "Sonraki ezan: ${ezan.ad} ${ezan.zaman}")
            } catch (e: SecurityException) {
                Log.e(TAG, "Tam alarm izni yok", e)
            }
        }

        private fun hesapla(enlem: Double, boylam: Double, gun: LocalDate) =
            NamazHesap.hesapla(
                enlem, boylam, gun,
                ZoneId.systemDefault().rules.getOffset(gun.atTime(12, 0)).totalSeconds / 60,
            )
    }
}
