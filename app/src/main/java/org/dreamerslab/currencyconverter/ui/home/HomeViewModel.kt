package org.dreamerslab.currencyconverter.ui.home

import androidx.compose.runtime.Immutable
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dreamerslab.currencyconverter.data.models.Currency
import org.dreamerslab.currencyconverter.data.models.ExchangeRate
import org.dreamerslab.currencyconverter.data.repository.ExchangeRatesRepository
import javax.inject.Inject
import kotlin.math.floor

@Immutable
data class FormState(
    val fromCurrency: Currency,
    val toCurrency: Currency,
    val fromAmount: String = "",
    val toAmount: String = ""
)

sealed interface FormEvent {
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

sealed interface HomeScreenState {
    data object Loading : HomeScreenState
    data class Success(
        val currencies: List<Currency>,
        val exchangeRates: List<ExchangeRate>
    ) : HomeScreenState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ExchangeRatesRepository
) : ViewModel() {

    private val _formState: MutableStateFlow<FormState> = MutableStateFlow(
        FormState(
            fromCurrency = Currency.fromCode("USD").getOrThrow(),
            toCurrency = Currency.fromCode("PKR").getOrThrow()
        )
    )
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    val state = combine(
        repository.supportedCountriesFlow,
        repository.exchangeRatesFlow
    ) { currencies, exchangeRates ->
        HomeScreenState.Success(
            currencies = currencies,
            exchangeRates = exchangeRates
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeScreenState.Loading
    )

    fun onEvent(event: FormEvent) {
        viewModelScope.launch {
            val isValid = when (event) {
                is FormEvent.FromAmountChanged -> event.amount.isDigitsOnly()
                is FormEvent.ToAmountChanged -> event.amount.isDigitsOnly()
                else -> true
            }

            if (!isValid) return@launch

            _formState.update {
                when (event) {
                    is FormEvent.FromAmountChanged -> it.copy(fromAmount = event.amount)
                    is FormEvent.FromCurrencyChanged -> it.copy(fromCurrency = event.currency)
                    is FormEvent.ToAmountChanged -> it.copy(toAmount = event.amount)
                    is FormEvent.ToCurrencyChanged -> it.copy(toCurrency = event.currency)
                }
            }

            when (event) {
                is FormEvent.FromAmountChanged -> calculateToAmount()
                is FormEvent.FromCurrencyChanged -> calculateToAmount()
                is FormEvent.ToCurrencyChanged -> calculateToAmount()
                is FormEvent.ToAmountChanged -> calculateFromAmount()
            }
        }
    }

    private fun calculateToAmount() {
        val stateValue = state.value
        val exchangeRates = if (stateValue is HomeScreenState.Success) stateValue.exchangeRates else return
        val formState = formState.value

        if (formState.fromAmount.isBlank()) {
            viewModelScope.launch {
                _formState.update { it.copy(toAmount = "") }
            }
            return
        }

        val amount = when {
            formState.fromCurrency.code != "USD" -> {
                val usdRate = exchangeRates.find { it.targetCurrency.code == formState.fromCurrency.code }?.rate ?: return
                formState.fromAmount.toDouble() / usdRate
            }
            else -> formState.fromAmount.toDouble()
        }

        val rate = exchangeRates.find { it.targetCurrency.code == formState.toCurrency.code }?.rate ?: return
        val resultAmount = amount * rate
        val roundedResult = floor(resultAmount * 100) / 100

        viewModelScope.launch {
            _formState.update {
                it.copy(toAmount = roundedResult.toString())
            }
        }
    }

    private fun calculateFromAmount() {
        val stateValue = state.value
        val exchangeRates = if (stateValue is HomeScreenState.Success) stateValue.exchangeRates else return
        val formState = formState.value

        if (formState.toAmount.isBlank()) {
            viewModelScope.launch {
                _formState.update { it.copy(fromAmount = "") }
            }
            return
        }

        val amount = when {
            formState.toCurrency.code != "USD" -> {
                val usdRate = exchangeRates.find { it.targetCurrency.code == formState.toCurrency.code }?.rate ?: return
                formState.toAmount.toDouble() / usdRate
            }
            else -> formState.toAmount.toDouble()
        }

        val rate = exchangeRates.find { it.targetCurrency.code == formState.fromCurrency.code }?.rate ?: return
        val resultAmount = amount * rate
        val roundedResult = floor(resultAmount * 100) / 100

        viewModelScope.launch {
            _formState.update {
                it.copy(fromAmount = roundedResult.toString())
            }
        }
    }

}