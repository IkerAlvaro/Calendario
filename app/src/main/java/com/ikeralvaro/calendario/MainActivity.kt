package com.ikeralvaro.calendario

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ikeralvaro.calendario.ads.AdBanner
import com.ikeralvaro.calendario.ads.Ads
import com.ikeralvaro.calendario.data.ThemeMode
import com.ikeralvaro.calendario.ui.AppBackend
import com.ikeralvaro.calendario.ui.CalendarApp
import com.ikeralvaro.calendario.ui.CalendarViewModel
import com.ikeralvaro.calendario.ui.LocalSettings
import com.ikeralvaro.calendario.ui.theme.CalendarioTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CalendarViewModel by viewModels {
        CalendarViewModel.factory { AppBackend(application as CalendarioApp) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Ads.gatherConsent(this)
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val privacyRequired by Ads.privacyOptionsRequired.collectAsStateWithLifecycle()
            val dark = when (settings.theme) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            DisposableEffect(dark) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }
            CalendarioTheme(darkTheme = dark) {
                CompositionLocalProvider(LocalSettings provides settings) {
                    CalendarApp(
                        viewModel,
                        banner = { AdBanner() },
                        privacyOptionsRequired = privacyRequired,
                        onPrivacyOptions = { Ads.showPrivacyOptions(this@MainActivity) },
                    )
                }
            }
        }
    }
}
