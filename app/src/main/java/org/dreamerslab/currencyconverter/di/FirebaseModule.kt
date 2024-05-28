package org.dreamerslab.currencyconverter.di

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.dreamerslab.currencyconverter.BuildConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return Firebase.database.apply {
            if (BuildConfig.USE_EMULATOR) useEmulator("10.0.2.2", 9000)
        }
    }

}