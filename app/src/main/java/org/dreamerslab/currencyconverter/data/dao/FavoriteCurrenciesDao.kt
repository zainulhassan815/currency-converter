package org.dreamerslab.currencyconverter.data.models

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavoriteCurrenciesDao {

    @Insert
    fun insert(currency: Currency)

    @Delete
    fun delete(currency: Currency)

    @Query("SELECT * FROM favorite_currencies")
    fun getAll(): List<Currency>

}