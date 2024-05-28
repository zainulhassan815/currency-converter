package org.dreamerslab.currencyconverter.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.dreamerslab.currencyconverter.data.models.Currency
import org.dreamerslab.currencyconverter.data.models.ExchangeRate
import org.dreamerslab.currencyconverter.data.models.SupportedCurrencyCodes
import org.dreamerslab.currencyconverter.util.AppDispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRatesRepository @Inject constructor(
    private val dispatchers: AppDispatchers,
    private val database: FirebaseDatabase
) {

    val supportedCountriesFlow: Flow<List<Currency>> = flow {
        val currencies = withContext(dispatchers.default) {
            SupportedCurrencyCodes.mapNotNull { code ->
                Currency.fromCode(code).getOrNull()
            }
        }
        emit(currencies)
    }

    val exchangeRatesFlow = callbackFlow {
        val baseCurrency = Currency.fromCode("USD").getOrThrow()
        val ref = database.getReference("conversion_rates")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currencies = snapshot.children.mapNotNull {
                    val code = it.key ?: return@mapNotNull null
                    val currency = Currency.fromCode(code).getOrNull() ?: return@mapNotNull null
                    val value = it.getValue<Double>() ?: return@mapNotNull null
                    ExchangeRate(
                        baseCurrency = baseCurrency,
                        targetCurrency = currency,
                        rate = value
                    )
                }
                trySend(currencies)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

}
