package com.bachhoangxuan.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bachhoangxuan.android.politicalpreparedness.database.BaseResult
import com.bachhoangxuan.android.politicalpreparedness.database.ElectionDatabase
import com.bachhoangxuan.android.politicalpreparedness.database.ElectionRepositories
import com.bachhoangxuan.android.politicalpreparedness.network.CivicsApi
import com.bachhoangxuan.android.politicalpreparedness.network.jsonadapter.ElectionAdapter
import com.bachhoangxuan.android.politicalpreparedness.network.models.Election
import com.bachhoangxuan.android.politicalpreparedness.utilities.BaseSingleLiveEvent
import kotlinx.coroutines.launch
import retrofit2.await

//TODO: Construct ViewModel and provide election datasource
class ElectionsViewModel(application: Application) : ViewModel() {
    val showEventLoading: BaseSingleLiveEvent<Boolean> = BaseSingleLiveEvent()
    private val electionDao = ElectionDatabase.getInstance(application).electionDao
    private val electionRepositories = ElectionRepositories(electionDao)
    private val _navigateVoterInfoElection = MutableLiveData<Election?>()
    val navigateVoterInfoElection: LiveData<Election?>
        get() = _navigateVoterInfoElection

    //TODO: Create live data val for upcoming elections
    private val _upcomingElectionList = MutableLiveData<List<Election>>()
    val upcomingElectionList: LiveData<List<Election>>
        get() = _upcomingElectionList

    //TODO: Create live data val for saved elections
    private val _savedElectionList = MutableLiveData<List<Election>>()
    val savedElectionList: LiveData<List<Election>>
        get() = _savedElectionList

    init {
        getUpcomingElections()
    }

    //TODO: Create val and functions to populate live data for upcoming elections from the API and saved elections from local database
    fun onSavedElectionS() {
        viewModelScope.launch {
            when (val result = electionRepositories.getElections()) {
                is BaseResult.Success<List<Election>> -> {
                    val elections = result.data
                    _savedElectionList.value = elections
                }

                is BaseResult.Error -> Log.e("Database Error Message: ", "${result.message}")
            }
        }
    }

    private fun getUpcomingElections() {
        val divisionAdapter = ElectionAdapter()
        showEventLoading.value = true
        viewModelScope.launch {
            try {
                val result = CivicsApi.retrofitService.fetchElections().await()
                Log.i("ELECTION LIST SUCCESS RESULT: ", "$result")
                showEventLoading.postValue(false)

                _upcomingElectionList.value = result.elections.map { election ->
                    Election(
                        id = election.id,
                        division = divisionAdapter.divisionFromJson(election.ocdDivisionId),
                        electionDay = election.electionDay,
                        ocdDivisionId = election.ocdDivisionId,
                        name = election.name,
                    )
                }
            } catch (e: Exception) {
                Log.e("ELECTION LIST ERROR MESSAGE:", "${e.message}")
            }
        }
    }

    //TODO: Create functions to navigate to saved or upcoming election voter info
    fun navigateToSaved() {
        _navigateVoterInfoElection.value = null
    }

    fun onClickUpcomingElectionVoterInfo(election: Election) {
        _navigateVoterInfoElection.value = election
    }

}