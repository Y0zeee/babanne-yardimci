package org.stypox.dicio.ui.kurulum

import android.Manifest
import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import org.stypox.dicio.R
import org.stypox.dicio.config.Kisi
import org.stypox.dicio.config.YardimciConfig
import org.stypox.dicio.di.SpeechOutputDeviceWrapper
import org.stypox.dicio.di.SttInputDeviceWrapper
import org.stypox.dicio.io.input.SttState
import org.stypox.dicio.skills.yerel_medya.YerelMedyaMantik
import org.stypox.dicio.util.checkPermissions
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class IzinGrubu(val ad: Int, val izinler: List<String>) {
    val tumIzinler: Array<String> get() = izinler.toTypedArray()
}

@HiltViewModel
class KurulumViewModel @Inject constructor(
    application: Application,
    private val sttWrapper: SttInputDeviceWrapper,
    private val konusma: SpeechOutputDeviceWrapper,
) : AndroidViewModel(application) {
    val sttDurumu: StateFlow<SttState?> = sttWrapper.uiState

    val izinGruplari: List<IzinGrubu> = listOf(
        IzinGrubu(R.string.kurulum_izin_mikrofon, listOf(Manifest.permission.RECORD_AUDIO)),
        IzinGrubu(
            R.string.kurulum_izin_telefon,
            listOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE)
        ),
        IzinGrubu(R.string.kurulum_izin_rehber, listOf(Manifest.permission.READ_CONTACTS)),
        IzinGrubu(
            R.string.kurulum_izin_konum,
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ),
        IzinGrubu(
            R.string.kurulum_izin_sms,
            listOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS
            )
        ),
        IzinGrubu(R.string.kurulum_izin_arama_kaydi, listOf(Manifest.permission.READ_CALL_LOG)),
        IzinGrubu(
            R.string.kurulum_izin_bildirim,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                emptyList() // no runtime permission before Android 13
            }
        ),
    )

    fun izinVerildi(grup: IzinGrubu): Boolean =
        checkPermissions(getApplication(), *grup.tumIzinler)

    fun kisiler(): List<Kisi> = YardimciConfig.load(getApplication()).kisiler

    fun kuranSayisi(): Int = mp3Say(YerelMedyaMantik.KURAN_KLASORU)
    fun turkuSayisi(): Int = mp3Say(YerelMedyaMantik.TURKU_KLASORU)

    private fun mp3Say(klasor: String): Int = try {
        KurulumMantik.mp3Say(File(klasor).list()?.toList())
    } catch (e: Exception) {
        0
    }

    /** Clicking the STT device advances its state machine: download, unzip, load. */
    fun modeliIndir() {
        sttWrapper.onClick { }
    }

    fun saatiSoyle() {
        val saat = SimpleDateFormat("HH:mm", Locale.forLanguageTag("tr")).format(Date())
        konusma.speak(getApplication<Application>().getString(R.string.kurulum_saat_cumle, saat))
    }
}
