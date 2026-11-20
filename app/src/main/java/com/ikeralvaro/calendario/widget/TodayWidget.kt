package com.ikeralvaro.calendario.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.ikeralvaro.calendario.CalendarioApp
import com.ikeralvaro.calendario.MainActivity
import com.ikeralvaro.calendario.data.Occurrence
import com.ikeralvaro.calendario.data.occurrencesOn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JTextStyle
import java.util.Locale

private val Es = Locale.forLanguageTag("es-ES")
private val Red = Color(0xFFE8111E)
private val bg = ColorProvider(day = Color.White, night = Color(0xFF1A1A1D))
private val fg = ColorProvider(day = Color(0xFF1C1C1E), night = Color(0xFFF2F2F7))
private val muted = ColorProvider(day = Color(0xFF7C7C84), night = Color(0xFF9A9AA3))
private val white = ColorProvider(day = Color.White, night = Color.White)

class TodayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val events = (context.applicationContext as CalendarioApp).repository.all()
        val today = LocalDate.now()
        val now = LocalDateTime.now()
        // Today's remaining events, then tomorrow's.
        val items = buildList {
            events.occurrencesOn(today).filter { it.event.allDay || it.end.isAfter(now) }.forEach { add(today to it) }
            events.occurrencesOn(today.plusDays(1)).forEach { add(today.plusDays(1) to it) }
        }.take(4)
        provideContent { Content(today, items) }
    }

    @Composable
    private fun Content(today: LocalDate, items: List<Pair<LocalDate, Occurrence>>) {
        val fmt = DateTimeFormatter.ofPattern("HH:mm")
        Column(
            GlanceModifier.fillMaxSize().cornerRadius(24.dp).background(bg)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Row(
                GlanceModifier.fillMaxWidth().background(ColorProvider(Red, Red)).padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(today.dayOfMonth.toString(), style = TextStyle(color = white, fontSize = 26.sp, fontWeight = FontWeight.Bold))
                Spacer(GlanceModifier.width(10.dp))
                Column {
                    Text(today.dayOfWeek.getDisplayName(JTextStyle.FULL, Es).replaceFirstChar { it.titlecase(Es) },
                        style = TextStyle(color = white, fontSize = 14.sp, fontWeight = FontWeight.Bold))
                    Text(today.month.getDisplayName(JTextStyle.FULL, Es).replaceFirstChar { it.titlecase(Es) },
                        style = TextStyle(color = white, fontSize = 12.sp))
                }
            }
            Column(GlanceModifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp)) {
                if (items.isEmpty()) {
                    Text("Día libre ✨", style = TextStyle(color = fg, fontSize = 15.sp, fontWeight = FontWeight.Medium))
                    Text("No tienes nada más hoy", style = TextStyle(color = muted, fontSize = 12.sp))
                }
                var lastDay: LocalDate? = today
                items.forEach { (day, o) ->
                    if (day != lastDay) {
                        Text("Mañana", style = TextStyle(color = muted, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            modifier = GlanceModifier.padding(top = 4.dp))
                        lastDay = day
                    }
                    val c = Color(o.event.color)
                    Row(GlanceModifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Spacer(GlanceModifier.width(4.dp).height(28.dp).cornerRadius(2.dp).background(ColorProvider(c, c)))
                        Spacer(GlanceModifier.width(8.dp))
                        Column {
                            Text(o.event.title.ifBlank { "(Sin título)" }, maxLines = 1,
                                style = TextStyle(color = fg, fontSize = 14.sp, fontWeight = FontWeight.Medium))
                            Text(if (o.event.allDay) "Todo el día" else "${o.start.format(fmt)} – ${o.end.format(fmt)}",
                                style = TextStyle(color = muted, fontSize = 12.sp))
                        }
                    }
                }
            }
        }
    }

    companion object {
        suspend fun refresh(context: Context) = runCatching { TodayWidget().updateAll(context) }
    }
}

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}
