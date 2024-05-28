package org.dreamerslab.currencyconverter.util

import kotlinx.coroutines.CoroutineDispatcher

data class AppDispatchers(
    val io: CoroutineDispatcher,
    val main: CoroutineDispatcher,
    val default: CoroutineDispatcher,
)
