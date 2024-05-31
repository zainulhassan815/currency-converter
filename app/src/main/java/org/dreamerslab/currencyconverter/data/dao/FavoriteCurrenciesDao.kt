package org.dreamerslab.currencyconverter.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.dreamerslab.currencyconverter.data.models.Currency

@Dao
interface FavoriteCurrenciesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(currency: Currency)

    @Delete
    fun delete(currency: Currency)

    @Query("SELECT * FROM favorite_currencies")
    fun getAll(): Flow<List<Currency>>

}