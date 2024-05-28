package org.dreamerslab.currencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import org.dreamerslab.currencyconverter.ui.home.Home
import org.dreamerslab.currencyconverter.ui.home.HomeViewModel
import org.dreamerslab.currencyconverter.ui.styles.CurrencyConverterTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CurrencyConverterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: HomeViewModel by viewModels()
                    Home(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
