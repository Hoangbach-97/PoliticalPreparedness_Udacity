package com.bachhoangxuan.android.politicalpreparedness.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bachhoangxuan.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    //TODO: Add insert query
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElection(vararg election: Election)

    //TODO: Add select all election query
    @Query("SELECT * FROM election_table ORDER BY electionDay DESC")
    fun getElections(): List<Election>

    //TODO: Add select single election query
    @Query("SELECT * FROM election_table WHERE id = :id")
    fun getElectionItemById(id: String): Election

    //TODO: Add delete query
    @Query("DELETE FROM election_table WHERE id = :id")
    fun deleteElectionItemById(id: String)

    //TODO: Add clear query
    @Query("DELETE FROM election_table")
    suspend fun clearElections()
}