package org.dreamerslab.currencyconverter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.dreamerslab.currencyconverter.util.AppDispatchers
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

    @Provides
    @Singleton
    fun provideCoroutineDispatcher(): AppDispatchers = AppDispatchers(
        io = Dispatchers.IO,
        main = Dispatchers.Main,
        default = Dispatchers.Default
    )

}