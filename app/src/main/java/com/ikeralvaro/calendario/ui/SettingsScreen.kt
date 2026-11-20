package com.ikeralvaro.calendario.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ikeralvaro.calendario.BuildConfig
import com.ikeralvaro.calendario.R
import com.ikeralvaro.calendario.data.Settings
import com.ikeralvaro.calendario.data.ThemeMode
import java.time.DayOfWeek

@Composable
fun SettingsScreen(
    settings: Settings,
    onChange: ((Settings) -> Settings) -> Unit,
    onBack: () -> Unit,
    privacyOptionsRequired: Boolean,
    onPrivacyOptions: () -> Unit,
) {
    BackHandler(onBack = onBack)
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver") }
                Text("Ajustes", style = MaterialTheme.typography.headlineSmall)
            }
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Label("Apariencia")
                Section {
                    IconRow(Icons.Rounded.DarkMode) { Text("Tema", style = MaterialTheme.typography.bodyLarge) }
                    Chips {
                        ThemeMode.entries.forEach { t -> ChoiceChip(t.label, settings.theme == t) { onChange { it.copy(theme = t) } } }
                    }
                }
                Label("Calendario")
                Section {
                    IconRow(Icons.Rounded.CalendarViewWeek) { Text("La semana empieza en", style = MaterialTheme.typography.bodyLarge) }
                    Chips {
                        ChoiceChip("Lunes", settings.firstDay == DayOfWeek.MONDAY) { onChange { it.copy(firstDay = DayOfWeek.MONDAY) } }
                        ChoiceChip("Domingo", settings.firstDay == DayOfWeek.SUNDAY) { onChange { it.copy(firstDay = DayOfWeek.SUNDAY) } }
                    }
                    Spacer(Modifier.height(10.dp))
                    IconRow(Icons.Rounded.Schedule) { Text("Formato de hora", style = MaterialTheme.typography.bodyLarge) }
                    Chips {
                        ChoiceChip("24 horas", settings.use24h) { onChange { it.copy(use24h = true) } }
                        ChoiceChip("12 horas", !settings.use24h) { onChange { it.copy(use24h = false) } }
                    }
                    Spacer(Modifier.height(10.dp))
                    IconRow(Icons.Rounded.NotificationsNone) { Text("Aviso por defecto", style = MaterialTheme.typography.bodyLarge) }
                    Chips {
                        reminderOptions.forEach { (m, l) -> ChoiceChip(l, settings.defaultReminder == m) { onChange { it.copy(defaultReminder = m) } } }
                    }
                }
                if (privacyOptionsRequired) {
                    Label("Privacidad")
                    Section {
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onPrivacyOptions),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconRow(Icons.Rounded.PrivacyTip) {
                                Column(Modifier.weight(1f)) {
                                    Text("Privacidad de anuncios", style = MaterialTheme.typography.bodyLarge)
                                    Text("Cambia tu consentimiento", style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(R.drawable.logo), null, Modifier.size(64.dp).shadow(10.dp, RoundedCornerShape(16.dp)))
                    Spacer(Modifier.height(10.dp))
                    Text("Calendario", style = MaterialTheme.typography.titleMedium)
                    Text("Versión ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text.uppercase(Es), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp))
}

@Composable
private fun Chips(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()).padding(start = 40.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}
