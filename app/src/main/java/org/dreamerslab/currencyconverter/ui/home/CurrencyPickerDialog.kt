package org.dreamerslab.currencyconverter.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import org.dreamerslab.currencyconverter.data.models.Currency
import org.dreamerslab.currencyconverter.ui.components.CurrencyFlag
import org.dreamerslab.currencyconverter.ui.styles.CurrencyConverterTheme
import org.dreamerslab.currencyconverter.ui.utils.MultiThemePreview

@Composable
fun CurrencyPickerDialog(
    currencies: List<Currency>,
    onSelected: (currency: Currency) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    selectedCurrency: Currency? = null,
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        CurrencyPickerContent(
            currencies = currencies,
            onSelected = onSelected,
            modifier = modifier,
            selectedCurrency = selectedCurrency
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
private fun CurrencyPickerContent(
    currencies: List<Currency>,
    onSelected: (currency: Currency) -> Unit,
    modifier: Modifier = Modifier,
    selectedCurrency: Currency? = null,
) {
    var query by remember { mutableStateOf("") }
    var filteredCurrencies by remember { mutableStateOf(currencies) }

    LaunchedEffect(currencies) {
        snapshotFlow { query }
            .debounce(300)
            .mapLatest { it.lowercase().trim() }
            .distinctUntilChanged()
            .mapLatest {
                currencies.filter { currency ->
                    currency.name.contains(it, ignoreCase = true) ||
                            currency.code.contains(it, ignoreCase = true)
                }
            }
            .flowOn(Dispatchers.IO)
            .collectLatest { filteredCurrencies = it }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    ) {
        LazyColumn(
            modifier = modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        shape = RoundedCornerShape(4.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                        ),
                        placeholder = {
                            Text(
                                text = "Search",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            items(
                items = filteredCurrencies,
                key = { it.code.hashCode() }
            ) {
                CurrencyCard(
                    currency = it,
                    onClick = { onSelected(it) },
                    modifier = Modifier.fillMaxWidth(),
                    selected = selectedCurrency?.code == it.code
                )
            }
        }
    }
}

@Composable
private fun CurrencyCard(
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            CurrencyFlag(
                flagUrl = currency.currencyFlagUrl,
                contentDescription = currency.code
            )

            Text(
                text = currency.name,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@MultiThemePreview
@Composable
private fun CurrencyCardPreview() {
    CurrencyConverterTheme {
        CurrencyCard(
            currency = Currency(name = "Pakistani Rupee", code = "PKR", symbol = "Rs"),
            onClick = {}
        )
    }
}

@MultiThemePreview
@Composable
private fun CurrencyCardSelectedPreview() {
    CurrencyConverterTheme {
        CurrencyCard(
            currency = Currency(name = "Pakistani Rupee", code = "PKR", symbol = "Rs"),
            onClick = {},
            selected = true
        )
    }
}