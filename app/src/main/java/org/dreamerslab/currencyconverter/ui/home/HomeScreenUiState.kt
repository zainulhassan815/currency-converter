package org.dreamerslab.currencyconverter.ui.home

import androidx.compose.runtime.Immutable
import org.dreamerslab.currencyconverter.data.models.Currency
import org.dreamerslab.currencyconverter.data.models.ExchangeRate

@Immutable
data class FormData(
    val fromCurrency: Currency = Currency.Empty,
    val toCurrency: Currency = Currency.Empty,
    val fromAmount: String = "",
    val toAmount: String = ""
)

@Immutable
data class CurrencyWithExchangeRate(
    val favoriteCurrency: Currency,
    val baseCurrency: Currency,
    val exchangeRate: Double,
    val resultAmount: Double
)

@Immutable
sealed class HomeScreenUiState {
    @Immutable
    data object Loading : HomeScreenUiState()

    @Immutable
    data class Success(
        val currencies: List<Currency>,
        val exchangeRates: HashMap<String, ExchangeRate>,
        val favorites: List<CurrencyWithExchangeRate>
    ) : HomeScreenUiState()
}