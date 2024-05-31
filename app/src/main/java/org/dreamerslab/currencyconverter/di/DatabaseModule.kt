package org.dreamerslab.currencyconverter.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.dreamerslab.currencyconverter.CurrencyConverterDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): CurrencyConverterDatabase {
        return Room.databaseBuilder(
            context,
            CurrencyConverterDatabase::class.java,
            "currency_converter_database"
        ).build()
    }

    @Provides
    fun provideFavoriteCurrenciesDao(
        db: CurrencyConverterDatabase
    ) = db.favoriteCurrenciesDao()

}