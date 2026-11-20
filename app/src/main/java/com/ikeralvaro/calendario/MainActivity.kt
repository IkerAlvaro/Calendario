package com.ikeralvaro.calendario

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.ikeralvaro.calendario.ui.CalendarApp
import com.ikeralvaro.calendario.ui.CalendarViewModel
import com.ikeralvaro.calendario.ui.theme.CalendarioTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CalendarViewModel by viewModels {
        CalendarViewModel.factory((application as CalendarioApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 33) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent { CalendarioTheme { CalendarApp(viewModel) } }
    }
}
