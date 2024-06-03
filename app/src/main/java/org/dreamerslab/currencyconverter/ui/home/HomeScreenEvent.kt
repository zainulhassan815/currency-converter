package org.dreamerslab.currencyconverter.ui.home

import org.dreamerslab.currencyconverter.data.models.Currency

sealed interface HomeScreenEvent

sealed interface FormEvent : HomeScreenEvent {
    data class InitialDataLoaded(
        val fromCurrency: Currency,
        val toCurrency: Currency
    ) : FormEvent

    data class FromCurrencyChanged(
        val currency: Currency
    ) : FormEvent

    data class ToCurrencyChanged(
        val currency: Currency
    ) : FormEvent

    data class FromAmountChanged(
        val amount: String
    ) : FormEvent

    data class ToAmountChanged(
        val amount: String
    ) : FormEvent
}

sealed interface FavoritesEvent : HomeScreenEvent {
    data class AddFavorite(
        val currency: Currency
    ) : FavoritesEvent

    data class RemoveFavorite(
        val currency: Currency
    ) : FavoritesEvent
}