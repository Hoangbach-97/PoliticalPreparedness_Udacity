package com.bachhoangxuan.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bachhoangxuan.android.politicalpreparedness.database.BaseResult
import com.bachhoangxuan.android.politicalpreparedness.database.ElectionDatabase
import com.bachhoangxuan.android.politicalpreparedness.database.ElectionRepo
import com.bachhoangxuan.android.politicalpreparedness.network.CivicsApi
import com.bachhoangxuan.android.politicalpreparedness.network.jsonadapter.ElectionAdapter
import com.bachhoangxuan.android.politicalpreparedness.network.models.Election
import com.bachhoangxuan.android.politicalpreparedness.util.BaseSingleLiveEvent
import kotlinx.coroutines.launch
import retrofit2.await

class ElectionsViewModel(application: Application) : ViewModel() {

    private val _navigateVoterInfo = MutableLiveData<Election?>()
    val navigateVoterInfo: LiveData<Election?>
        get() = _navigateVoterInfo

    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>>
        get() = _upcomingElections

    private val _savedElections = MutableLiveData<List<Election>>()
    val savedElections: LiveData<List<Election>>
        get() = _savedElections

    val showLoading: BaseSingleLiveEvent<Boolean> = BaseSingleLiveEvent()
    private val electionDao = ElectionDatabase.getInstance(application).electionDao
    private val electionRepo = ElectionRepo(electionDao)

    init {
        getUpcomingElections()
    }

    fun onSavedElections() {
        viewModelScope.launch {
            when (val result = electionRepo.getAll()) {
                is BaseResult.Success<List<Election>> -> {
                    val elections = result.data
                    _savedElections.value = elections
                }

                is BaseResult.Error -> Log.e("Database Error", "${result.message}")
            }
        }
    }

    private fun getUpcomingElections() {
        val adapter = ElectionAdapter()
        showLoading.value = true
        viewModelScope.launch {
            try {
                val result = CivicsApi.retrofitService.fetchAllElections().await()
                showLoading.postValue(false)

                _upcomingElections.value = result.elections.map { election ->
                    Election(
                        id = election.id,
                        division = adapter.divisionFromJson(election.ocdDivisionId),
                        electionDay = election.electionDay,
                        ocdDivisionId = election.ocdDivisionId,
                        name = election.name,
                    )
                }
            } catch (e: Exception) {
                Log.e("Elections result error", "${e.message}")
            }
        }
    }

    fun navigateToSavedElections() {
        _navigateVoterInfo.value = null
    }

    fun onClickUpcomingElectionVoterInfo(election: Election) {
        _navigateVoterInfo.value = election
    }

}