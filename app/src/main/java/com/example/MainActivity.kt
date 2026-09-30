package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppThemeMode
import com.example.data.WageDatabase
import com.example.data.WageRepository
import com.example.ui.WageFlowApp
import com.example.ui.WageViewModel
import com.example.ui.theme.WageFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = WageDatabase.getDatabase(applicationContext)
        val repository = WageRepository(database.wageDao())

        setContent {
            val wageViewModel: WageViewModel = viewModel(
                factory = WageViewModel.provideFactory(repository)
            )
            val uiState by wageViewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (AppThemeMode.fromKey(uiState.settings.themeMode)) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> systemDark
            }

            WageFlowTheme(darkTheme = isDarkTheme) {
                WageFlowApp(viewModel = wageViewModel)
            }
        }
    }
}
