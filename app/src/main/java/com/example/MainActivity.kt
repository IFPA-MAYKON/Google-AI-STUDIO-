package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.calculator.ui.CalculatorScreen
import com.example.calculator.viewmodel.CalculatorViewModel
import com.example.ui.theme.CalculatorOmniTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorOmniTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CalculatorScreen(viewModel = viewModel)
                }
            }
        }
    }
}
