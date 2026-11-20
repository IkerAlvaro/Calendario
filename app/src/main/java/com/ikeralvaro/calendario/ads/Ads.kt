package com.ikeralvaro.calendario.ads

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.ikeralvaro.calendario.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Google AdMob with the UMP consent flow (required in the EU/EEA/UK).
 * Ads are only requested once the user has given (or doesn't need to give) consent.
 */
object Ads {
    private val _ready = MutableStateFlow(false)
    /** True once the Mobile Ads SDK is initialised and ads may be requested. */
    val ready = _ready.asStateFlow()

    private val _privacyOptionsRequired = MutableStateFlow(false)
    /** True if the user must be offered a way to change their consent (shown in Settings). */
    val privacyOptionsRequired = _privacyOptionsRequired.asStateFlow()

    private val started = AtomicBoolean(false)

    fun gatherConsent(activity: Activity) {
        val info = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder().build()
        info.requestConsentInfoUpdate(
            activity, params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                    _privacyOptionsRequired.value =
                        info.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
                    if (info.canRequestAds()) start(activity)
                }
            },
            { _ -> if (info.canRequestAds()) start(activity) },
        )
        // Consent obtained in a previous session: start right away.
        if (info.canRequestAds()) start(activity)
    }

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { _ -> }
    }

    private fun start(context: Context) {
        if (started.getAndSet(true)) return
        val app = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(app) { _ready.value = true }
        }
    }
}

/** Anchored adaptive banner. Reserves its height once ads are allowed, so content never jumps. */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val ready by Ads.ready.collectAsStateWithLifecycle()
    if (!ready) return
    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp
    val size = remember(widthDp) { AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp) }
    var loaded by remember { mutableStateOf(false) }
    val adView = remember(size) {
        AdView(context).apply {
            adUnitId = BuildConfig.BANNER_ID
            setAdSize(size)
            adListener = object : AdListener() {
                override fun onAdLoaded() { loaded = true }
            }
            loadAd(AdRequest.Builder().build())
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(adView, lifecycle) {
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                else -> Unit
            }
        }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs); adView.destroy() }
    }
    Box(
        modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        if (!loaded) Text("Publicidad", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        AndroidView({ adView }, Modifier.fillMaxWidth().height(size.height.dp))
    }
}
