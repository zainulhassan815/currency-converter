package org.dreamerslab.currencyconverter.data.models

import androidx.compose.runtime.Immutable

@Immutable
data class ExchangeRate(
    val baseCurrency: Currency,
    val targetCurrency: Currency,
    val rate: Double
)
