package org.dreamerslab.currencyconverter

import androidx.room.Database
import androidx.room.RoomDatabase
import org.dreamerslab.currencyconverter.data.models.Currency
import org.dreamerslab.currencyconverter.data.dao.FavoriteCurrenciesDao

@Database(
    entities = [Currency::class],
    version = 1
)
abstract class CurrencyConverterDatabase : RoomDatabase() {
    abstract fun favoriteCurrenciesDao(): FavoriteCurrenciesDao
}