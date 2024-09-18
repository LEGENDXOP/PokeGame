package com.legendx.pokehexa.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao {

    @Upsert
    suspend fun addUserData(userData: UserTable)

    @Delete
    suspend fun deleteUserData(userData: UserTable)

    @Query("SELECT * FROM userData")
    fun getAllUserData(): Flow<List<UserTable>>
}

@Dao
interface UserStartDao{

    @Upsert
    suspend fun saveStart(userStart: UserStart)

    @Query("SELECT * FROM userStart")
    suspend fun getStart(): UserStart
}