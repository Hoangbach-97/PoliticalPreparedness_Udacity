package com.bachhoangxuan.android.politicalpreparedness.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bachhoangxuan.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg election: Election)

    @Query("select * from election_table where id = :id")
    fun getById(id: String): Election

    @Query("select * from election_table order by electionDay desc")
    fun getAll(): List<Election>

    @Query("delete from election_table where id = :id")
    fun deleteById(id: String)

    @Query("delete from election_table")
    suspend fun deleteAll()
}