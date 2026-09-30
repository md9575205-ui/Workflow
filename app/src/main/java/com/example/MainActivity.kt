package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
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
            WageFlowTheme {
                val wageViewModel: WageViewModel = viewModel(
                    factory = WageViewModel.provideFactory(repository)
                )
                WageFlowApp(viewModel = wageViewModel)
            }
        }
    }
}
