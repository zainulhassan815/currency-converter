package org.dreamerslab.currencyconverter.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import org.dreamerslab.currencyconverter.R
import org.dreamerslab.currencyconverter.data.models.Currency
import org.dreamerslab.currencyconverter.ui.components.CurrencyFlag
import org.dreamerslab.currencyconverter.ui.styles.CurrencyConverterTheme
import org.dreamerslab.currencyconverter.ui.utils.MultiThemePreview

@Composable
fun Home(
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formData by viewModel.formData.collectAsStateWithLifecycle()

    when (val state = uiState) {
        HomeScreenUiState.Loading -> HomeScreenLoading()
        is HomeScreenUiState.Success -> HomeScreenContent(
            state = state,
            formData = formData,
            onEvent = viewModel::handleEvent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenLoading() {
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
        val res = if (isSystemInDarkTheme()) R.raw.loading_indicator_dark else R.raw.loading_indicator_light
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(res))
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentScale = ContentScale.FillBounds,
            speed = 1.5f
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    state: HomeScreenUiState.Success,
    formData: FormData,
    onEvent: (HomeScreenEvent) -> Unit,
) {
    var showFavoriteCurrenciesDialog by remember { mutableStateOf(false) }

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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFavoriteCurrenciesDialog = true },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CurrencyConverterForm(
                data = formData,
                supportedCurrencies = state.currencies,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FavoriteCurrenciesSheet(
                currencies = state.favorites,
                onDelete = { onEvent(FavoritesEvent.RemoveFavorite(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }

    if (showFavoriteCurrenciesDialog) {
        Dialog(
            onDismissRequest = { showFavoriteCurrenciesDialog = false }
        ) {
            CurrencyPickerDialog(
                currencies = state.currencies,
                onSelected = {
                    onEvent(FavoritesEvent.AddFavorite(it))
                    showFavoriteCurrenciesDialog = false
                },
                onDismiss = {
                    showFavoriteCurrenciesDialog = false
                },
                modifier = Modifier.height(320.dp)
            )
        }
    }
}

@Composable
fun CurrencyConverterForm(
    data: FormData,
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
            selectedCurrency = data.fromCurrency,
            modifier = Modifier.height(320.dp)
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
                label = data.fromCurrency.code,
                flagUrl = data.fromCurrency.currencyFlagUrl,
                onClick = { showFromCurrencyPicker = true },
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(R.drawable.convert),
                contentDescription = "Convert Currencies Icon"
            )

            CurrencySelectionButton(
                label = data.toCurrency.code,
                flagUrl = data.toCurrency.currencyFlagUrl,
                onClick = { showToCurrencyPicker = true },
                modifier = Modifier.weight(1f)
            )
        }

        CurrencyInputField(
            value = data.fromAmount,
            onChange = { onEvent(FormEvent.FromAmountChanged(it)) },
            symbol = data.fromCurrency.symbol,
            modifier = Modifier.padding(top = 16.dp)
        )

        CurrencyInputField(
            value = data.toAmount,
            onChange = { onEvent(FormEvent.ToAmountChanged(it)) },
            symbol = data.toCurrency.symbol,
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoriteCurrenciesSheet(
    currencies: List<CurrencyWithExchangeRate>,
    onDelete: (Currency) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BottomSheetDefaults.DragHandle()

            Text(
                text = "Favorites",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                items(
                    items = currencies,
                    key = { it.hashCode() }
                ) {
                    FavoriteCurrencyCard(
                        currency = it,
                        onDeleteClick = { onDelete(it.favoriteCurrency) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItemPlacement()
                    )
                }

                item { Spacer(modifier = Modifier.height(64.dp)) }
            }
        }
    }
}

@Composable
fun FavoriteCurrencyCard(
    currency: CurrencyWithExchangeRate,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SwipeToDeleteWrapper(
        onSwiped = onDeleteClick,
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp),
            shape = RoundedCornerShape(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                CurrencyFlag(
                    flagUrl = currency.favoriteCurrency.currencyFlagUrl,
                    contentDescription = currency.favoriteCurrency.code
                )

                Text(
                    text = currency.favoriteCurrency.code,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = "${currency.favoriteCurrency.symbol} ${currency.resultAmount}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "1 ${currency.baseCurrency.code} = ${currency.exchangeRate} ${currency.favoriteCurrency.code}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteWrapper(
    onSwiped: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd)
                onSwiped()

            true
        },
        positionalThreshold = { distance -> distance * 0.5f }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val alignment = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.Settled -> Alignment.CenterStart
            }

            val scale by animateFloatAsState(
                targetValue = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> 1.2f
                    SwipeToDismissBoxValue.EndToStart -> 1.2f
                    SwipeToDismissBoxValue.Settled -> 0.8f
                },
                label = "Swipe to Dismiss Scale Animation"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.error),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Favorite",
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .align(alignment)
                        .scale(scale),
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        modifier = modifier,
        content = { content() },
    )
}

@MultiThemePreview
@Composable
fun CurrencyConverterFormPreview() {
    CurrencyConverterTheme {
        Surface {
            CurrencyConverterForm(
                data = FormData(
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