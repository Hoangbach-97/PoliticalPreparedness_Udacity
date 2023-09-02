package com.bachhoangxuan.android.politicalpreparedness.network

import com.bachhoangxuan.android.politicalpreparedness.network.jsonadapter.ElectionAdapter
import com.bachhoangxuan.android.politicalpreparedness.network.models.ElectionResponse
import com.bachhoangxuan.android.politicalpreparedness.network.models.RepresentativeResponse
import com.bachhoangxuan.android.politicalpreparedness.network.models.VoterInfoResponse
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://www.googleapis.com/civicinfo/v2/"

// TODO: Add adapters for Java Date and custom adapter ElectionAdapter (included in project)
private val moshi = Moshi.Builder()
    .add(ElectionAdapter())
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .client(CivicsHttpClient.getClient())
    .baseUrl(BASE_URL)
    .build()

/**
 *  Documentation for the Google Civics API Service can be found at https://developers.google.com/civic-information/docs/v2
 */

interface CivicsApiService {
    @GET("elections")
    fun fetchAllElections(): Call<ElectionResponse>

    @GET("voterinfo")
    fun fetchVoterInfo(
        @Query("address") address: String,
        @Query("electionId") electionId: Long,
        @Query("returnAllAvailableData") returnAllAvailableData: Boolean,
        @Query("productionDataOnly") productionDataOnly: Boolean,
    ): Call<VoterInfoResponse>

    @GET("representatives")
    fun fetchReps(@Query("address") address: String): Call<RepresentativeResponse>
}

object CivicsApi {
    val retrofitService: CivicsApiService by lazy {
        retrofit.create(CivicsApiService::class.java)
    }
}
