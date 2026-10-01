package org.stypox.dicio.ui.kurulum

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import org.stypox.dicio.R
import org.stypox.dicio.io.input.SttState

private val YESIL = Color(0xFF2E7D32)
private val KIRMIZI = Color(0xFFC62828)
private val YAZI = 22.sp

@Composable
fun KurulumScreen(
    navigationIcon: @Composable () -> Unit,
    viewModel: KurulumViewModel = hiltViewModel(),
) {
    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.kurulum_baslik)) },
                navigationIcon = navigationIcon,
            )
        }
    ) { padding ->
        KurulumIcerik(viewModel, Modifier.padding(padding))
    }
}

@Composable
private fun KurulumIcerik(viewModel: KurulumViewModel, modifier: Modifier) {
    val context = LocalContext.current
    // bumped to recompose the status rows after returning from a permission dialog / settings
    var yenile by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(null) {
        yenile++
        onPauseOrDispose {}
    }
    val izinIstegi = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { yenile++ }
    val sttDurumu by viewModel.sttDurumu.collectAsState()

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Baslik(stringResource(R.string.kurulum_izinler)) }
        for (grup in viewModel.izinGruplari) {
            item(key = grup.ad) {
                val verildi = yenile >= 0 && viewModel.izinVerildi(grup)
                DurumSatiri(
                    ad = stringResource(grup.ad),
                    gorunum = KurulumMantik.izinGorunumu(verildi),
                    dugmeMetni = stringResource(R.string.kurulum_izin_ver),
                    onClick = { izinIstegi.launch(grup.tumIzinler) },
                )
            }
        }

        item { Baslik(stringResource(R.string.kurulum_sistem)) }
        item {
            val asistan = yenile >= 0 && varsayilanAsistanMi(context)
            DurumSatiri(
                ad = stringResource(R.string.kurulum_varsayilan_asistan),
                gorunum = KurulumMantik.izinGorunumu(asistan),
                dugmeMetni = stringResource(R.string.kurulum_ayarlari_ac),
                onClick = {
                    guvenliAc(context, Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))
                },
            )
        }
        item {
            Dugme(stringResource(R.string.kurulum_otomatik_baslat)) {
                guvenliAc(context, miuiOtomatikBaslatIntent())
            }
        }
        item {
            Dugme(stringResource(R.string.kurulum_pil_kisitlama_yok)) {
                guvenliAc(context, miuiPilIntent(context))
            }
        }

        item { Baslik(stringResource(R.string.kurulum_ses_modeli)) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Metin(stringResource(sttDurumMetni(sttDurumu)))
                if (sttDurumu == SttState.NotDownloaded ||
                    sttDurumu is SttState.ErrorDownloading
                ) {
                    Dugme(stringResource(R.string.kurulum_modeli_indir)) {
                        viewModel.modeliIndir()
                    }
                }
            }
        }

        item { Baslik(stringResource(R.string.kurulum_kisiler)) }
        item {
            val kisiler = remember(yenile) { viewModel.kisiler() }
            if (kisiler.isEmpty()) {
                Metin(stringResource(R.string.kurulum_kisi_yok))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (kisi in kisiler) {
                        val sira = kisi.acilSira?.let {
                            stringResource(R.string.kurulum_acil_sira, it)
                        } ?: ""
                        Metin(
                            "${kisi.adlar.joinToString(", ")} · " +
                                "${KurulumMantik.maskeleNumara(kisi.numara)} $sira"
                        )
                    }
                }
            }
        }

        item { Baslik(stringResource(R.string.kurulum_dosyalar)) }
        item {
            val kuran = remember(yenile) { viewModel.kuranSayisi() }
            val turku = remember(yenile) { viewModel.turkuSayisi() }
            Metin(stringResource(R.string.kurulum_dosya_sayisi, kuran, turku))
        }

        item { Baslik(stringResource(R.string.kurulum_dene)) }
        item {
            Dugme(stringResource(R.string.kurulum_dene_saat)) { viewModel.saatiSoyle() }
        }
        item { Metin("") }
    }
}

@Composable
private fun Baslik(metin: String) {
    Text(
        text = metin,
        fontSize = 26.sp,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(top = 12.dp),
    )
}

@Composable
private fun Metin(metin: String) {
    Text(text = metin, fontSize = YAZI)
}

@Composable
private fun Dugme(metin: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text = metin, fontSize = YAZI, modifier = Modifier.padding(vertical = 6.dp))
    }
}

@Composable
private fun DurumSatiri(
    ad: String,
    gorunum: KurulumMantik.IzinGorunumu,
    dugmeMetni: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = ad, fontSize = YAZI)
            Text(
                text = gorunum.etiket,
                fontSize = YAZI,
                color = if (gorunum.yesil) YESIL else KIRMIZI,
            )
        }
        if (gorunum.dugmeGorunur) {
            Button(onClick = onClick) { Text(text = dugmeMetni, fontSize = 18.sp) }
        }
    }
}

private fun sttDurumMetni(durum: SttState?): Int = when (durum) {
    null -> R.string.kurulum_stt_yok
    SttState.NotDownloaded -> R.string.kurulum_stt_inmedi
    is SttState.Downloading, is SttState.Unzipping, SttState.Downloaded ->
        R.string.kurulum_stt_iniyor
    is SttState.ErrorDownloading, is SttState.ErrorUnzipping, is SttState.ErrorLoading ->
        R.string.kurulum_stt_hata
    SttState.NotLoaded, is SttState.Loading, SttState.Loaded, SttState.Listening ->
        R.string.kurulum_stt_hazir
    else -> R.string.kurulum_stt_yok
}

private fun varsayilanAsistanMi(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
    return try {
        context.getSystemService(RoleManager::class.java)
            ?.isRoleHeld(RoleManager.ROLE_ASSISTANT) == true
    } catch (e: Exception) {
        false
    }
}

private fun miuiOtomatikBaslatIntent() = Intent().setComponent(
    ComponentName(
        "com.miui.securitycenter",
        "com.miui.permcenter.autostart.AutoStartManagementActivity",
    )
)

private fun miuiPilIntent(context: Context) = Intent().setComponent(
    ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity")
).putExtra("package_name", context.packageName)
    .putExtra("package_label", context.applicationInfo.loadLabel(context.packageManager))

/** Starts [intent]; if it cannot be resolved, opens this app's details page instead. */
private fun guvenliAc(context: Context, intent: Intent) {
    try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", context.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e2: Exception) {
            // nothing else to try, but never crash
        }
    }
}
