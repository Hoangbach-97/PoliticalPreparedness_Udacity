package com.bachhoangxuan.android.politicalpreparedness.network.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "election_table")
data class Election(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "electionDay") val electionDay: String,
    @ColumnInfo(name = "ocdDivisionId") val ocdDivisionId: String,
    @Embedded(prefix = "division_") @Json(name = "ocdDivisionId") val division: Division,
)