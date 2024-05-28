package org.dreamerslab.currencyconverter.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dreamerslab.currencyconverter.R
import org.dreamerslab.currencyconverter.data.models.Currency
import org.dreamerslab.currencyconverter.ui.components.CurrencyFlag
import org.dreamerslab.currencyconverter.ui.styles.CurrencyConverterTheme
import org.dreamerslab.currencyconverter.ui.utils.MultiThemePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    viewModel: HomeViewModel
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val screenState by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.app_name))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // TODO: Open side menu
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (val state = screenState) {
                HomeScreenState.Loading -> Unit
                is HomeScreenState.Success -> CurrencyConverterForm(
                    state = formState,
                    supportedCurrencies = state.currencies,
                    onEvent = { viewModel.onEvent(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

        }
    }
}

@Composable
fun CurrencyConverterForm(
    state: FormState,
    supportedCurrencies: List<Currency>,
    onEvent: (FormEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFromCurrencyPicker by remember { mutableStateOf(false) }
    var showToCurrencyPicker by remember { mutableStateOf(false) }

    if (showFromCurrencyPicker || showToCurrencyPicker) {
        CurrencyPickerDialog(
            currencies = supportedCurrencies,
            onSelected = {
                if (showFromCurrencyPicker) {
                    onEvent(FormEvent.FromCurrencyChanged(it))
                } else {
                    onEvent(FormEvent.ToCurrencyChanged(it))
                }
                showFromCurrencyPicker = false
                showToCurrencyPicker = false
            },
            onDismiss = {
                showToCurrencyPicker = false
                showFromCurrencyPicker = false
            },
            selectedCurrency = state.fromCurrency,
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CurrencySelectionButton(
                label = state.fromCurrency.code,
                flagUrl = state.fromCurrency.currencyFlagUrl,
                onClick = { showFromCurrencyPicker = true },
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(R.drawable.convert),
                contentDescription = "Convert Currencies Icon"
            )

            CurrencySelectionButton(
                label = state.toCurrency.code,
                flagUrl = state.toCurrency.currencyFlagUrl,
                onClick = { showToCurrencyPicker = true },
                modifier = Modifier.weight(1f)
            )
        }

        CurrencyInputField(
            value = state.fromAmount,
            onChange = { onEvent(FormEvent.FromAmountChanged(it)) },
            symbol = state.fromCurrency.symbol,
            modifier = Modifier.padding(top = 16.dp)
        )

        CurrencyInputField(
            value = state.toAmount,
            onChange = { onEvent(FormEvent.ToAmountChanged(it)) },
            symbol = state.toCurrency.symbol,
            modifier = Modifier.padding(top = 16.dp)
        )
    }


}

@Composable
fun LastUpdatedText(
    dateTimeString: String,
    modifier: Modifier = Modifier,
) {
    Text(
        buildAnnotatedString {
            withStyle(
                style = MaterialTheme.typography.labelLarge.toSpanStyle()
            ) {
                append(stringResource(R.string.last_updated))
            }
            withStyle(
                style = MaterialTheme.typography.labelLarge.toSpanStyle().copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                append(dateTimeString)
            }
        },
        modifier = modifier
    )
}

@Composable
fun CurrencySelectionButton(
    label: String,
    flagUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.semantics { Role.Button },
        onClick = onClick,
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            CurrencyFlag(
                flagUrl = flagUrl,
                contentDescription = label
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }
    }
}

@Composable
fun CurrencyInputField(
    value: String,
    onChange: (value: String) -> Unit,
    symbol: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
    ) {
        TextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            placeholder = {
                Text(
                    text = "0.0",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.width(56.dp)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )
    }
}

@MultiThemePreview
@Composable
fun CurrencyConverterFormPreview() {
    CurrencyConverterTheme {
        Surface {
            CurrencyConverterForm(
                state = FormState(
                    fromCurrency = Currency("USD", "United States Dollar", "$"),
                    toCurrency = Currency("PKR", "Pakistani Rupees", "Rs")
                ),
                supportedCurrencies = emptyList(),
                onEvent = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@MultiThemePreview
@Composable
fun CurrencySelectionButtonPreview() {
    CurrencyConverterTheme {
        CurrencySelectionButton(
            label = "PKR",
            flagUrl = "",
            onClick = {}
        )
    }
}

@MultiThemePreview
@Composable
private fun LastUpdatedTextPreview() {
    CurrencyConverterTheme {
        LastUpdatedText(dateTimeString = "Friday 15 March 2024")
    }
}