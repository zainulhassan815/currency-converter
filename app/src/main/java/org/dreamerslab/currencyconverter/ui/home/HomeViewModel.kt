package org.dreamerslab.currencyconverter.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dreamerslab.currencyconverter.data.dao.FavoriteCurrenciesDao
import org.dreamerslab.currencyconverter.data.models.Currency
import org.dreamerslab.currencyconverter.data.models.ExchangeRate
import org.dreamerslab.currencyconverter.data.repository.ExchangeRatesRepository
import org.dreamerslab.currencyconverter.util.AppDispatchers
import javax.inject.Inject
import kotlin.math.floor

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: ExchangeRatesRepository,
    private val favoriteCurrenciesDao: FavoriteCurrenciesDao,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private val _formEventsFlow: MutableSharedFlow<FormEvent> = MutableSharedFlow()
    private val _formDataFlow: MutableStateFlow<FormData> = MutableStateFlow(FormData())
    val formData: StateFlow<FormData> = _formDataFlow.asStateFlow()

    val uiState: StateFlow<HomeScreenUiState> = combine(
        _formDataFlow,
        repository.supportedCountriesFlow,
        repository.exchangeRatesFlow,
        favoriteCurrenciesDao.getAll()
    ) { formData, currencies, exchangeRates, favorites ->
        withContext(dispatchers.default) {
            HomeScreenUiState.Success(
                currencies = currencies,
                exchangeRates = exchangeRates,
                favorites = favorites.map {
                    val amount = formData.fromAmount.toDoubleOrNull() ?: 0.0
                    val exchangeRate = calculateResult(
                        fromCurrency = formData.fromCurrency,
                        toCurrency = it,
                        amount = 1.0,
                        exchangeRates = exchangeRates
                    )
                    val result = exchangeRate * amount

                    CurrencyWithExchangeRate(
                        favoriteCurrency = it,
                        baseCurrency = formData.fromCurrency,
                        exchangeRate = exchangeRate.roundToTwoDecimals(),
                        resultAmount = result.roundToTwoDecimals()
                    )
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeScreenUiState.Loading
    )

    init {
        viewModelScope.launch {
            _formEventsFlow
                .filter { event ->
                    when (event) {
                        is FormEvent.FromAmountChanged -> {
                            if (event.amount.isBlank()) {
                                _formDataFlow.update { it.copy(fromAmount = "", toAmount = "") }
                            }
                            event.amount.isNotBlank()
                        }

                        is FormEvent.ToAmountChanged -> {
                            if (event.amount.isBlank()) {
                                _formDataFlow.update { it.copy(toAmount = "", fromAmount = "") }
                            }
                            event.amount.isNotBlank()
                        }

                        else -> true
                    }
                }
                .filter { event ->
                    when (event) {
                        is FormEvent.FromAmountChanged -> event.amount.toDoubleOrNull() != null
                        is FormEvent.ToAmountChanged -> event.amount.toDoubleOrNull() != null
                        is FormEvent.InitialDataLoaded -> event.fromCurrency != Currency.Empty && event.toCurrency != Currency.Empty
                        else -> true
                    }
                }
                .onEach { event ->
                    _formDataFlow.update { data ->
                        when (event) {
                            is FormEvent.InitialDataLoaded -> data.copy(
                                fromCurrency = event.fromCurrency,
                                toCurrency = event.toCurrency
                            )

                            is FormEvent.FromAmountChanged -> data.copy(fromAmount = event.amount)
                            is FormEvent.FromCurrencyChanged -> data.copy(fromCurrency = event.currency)
                            is FormEvent.ToAmountChanged -> data.copy(toAmount = event.amount)
                            is FormEvent.ToCurrencyChanged -> data.copy(toCurrency = event.currency)
                        }
                    }
                }
                .collectLatest { event ->
                    if (event is FormEvent.InitialDataLoaded) return@collectLatest
                    val exchangeRates = when (val state = uiState.value) {
                        is HomeScreenUiState.Success -> state.exchangeRates
                        else -> return@collectLatest
                    }
                    val data = _formDataFlow.value

                    when (event) {
                        is FormEvent.FromAmountChanged,
                        is FormEvent.FromCurrencyChanged,
                        is FormEvent.ToCurrencyChanged -> {
                            val result = calculateResult(
                                fromCurrency = data.fromCurrency,
                                toCurrency = data.toCurrency,
                                amount = data.fromAmount.toDoubleOrNull() ?: 0.0,
                                exchangeRates = exchangeRates
                            ).roundToTwoDecimals()
                            _formDataFlow.update { it.copy(toAmount = result.toString()) }
                        }

                        is FormEvent.ToAmountChanged -> {
                            val result = calculateResult(
                                fromCurrency = data.toCurrency,
                                toCurrency = data.fromCurrency,
                                amount = data.toAmount.toDoubleOrNull() ?: 0.0,
                                exchangeRates = exchangeRates
                            ).roundToTwoDecimals()
                            _formDataFlow.update { it.copy(fromAmount = result.toString()) }
                        }

                        else -> Unit
                    }
                }
        }
    }

    // TODO: Replace this code
    // Save user data in datastore
    // and load data on start
    init {
        viewModelScope.launch {
            _formEventsFlow.emit(
                FormEvent.InitialDataLoaded(
                    fromCurrency = Currency.fromCode("USD").getOrThrow(),
                    toCurrency = Currency.fromCode("PKR").getOrThrow()
                )
            )
        }
    }

    fun handleEvent(event: HomeScreenEvent) {
        viewModelScope.launch {
            when (event) {
                is FavoritesEvent.AddFavorite -> withContext(dispatchers.io) {
                    favoriteCurrenciesDao.insert(event.currency)
                }

                is FavoritesEvent.RemoveFavorite -> withContext(dispatchers.io) {
                    favoriteCurrenciesDao.delete(event.currency)
                }

                is FormEvent -> _formEventsFlow.emit(event)
            }
        }
    }

    private fun convertToBaseCurrency(
        currency: Currency,
        amount: Double,
        exchangeRates: HashMap<String, ExchangeRate>
    ): Double {
        if (currency.code == "USD") return amount
        val usdExchangeRate = exchangeRates[currency.code]?.rate ?: return 0.0
        return amount / usdExchangeRate
    }

    private fun calculateResult(
        fromCurrency: Currency,
        toCurrency: Currency,
        amount: Double,
        exchangeRates: HashMap<String, ExchangeRate>
    ): Double {
        val exchangeRate = exchangeRates[toCurrency.code]?.rate ?: return 0.0
        val baseAmount = convertToBaseCurrency(fromCurrency, amount, exchangeRates)
        val result = baseAmount * exchangeRate
        return result
    }

    private fun Double.roundToTwoDecimals(): Double {
        return floor(this * 100) / 100
    }

}