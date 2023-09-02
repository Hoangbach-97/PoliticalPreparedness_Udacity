package com.bachhoangxuan.android.politicalpreparedness.election

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bachhoangxuan.android.politicalpreparedness.R
import com.bachhoangxuan.android.politicalpreparedness.database.BaseResult
import com.bachhoangxuan.android.politicalpreparedness.database.ElectionDatabase.Companion.getInstance
import com.bachhoangxuan.android.politicalpreparedness.database.ElectionRepositories
import com.bachhoangxuan.android.politicalpreparedness.network.CivicsApi
import com.bachhoangxuan.android.politicalpreparedness.network.jsonadapter.ElectionAdapter
import com.bachhoangxuan.android.politicalpreparedness.network.models.Election
import com.bachhoangxuan.android.politicalpreparedness.network.models.VoterInfoResponse
import com.bachhoangxuan.android.politicalpreparedness.utilities.BaseSingleLiveEvent
import kotlinx.coroutines.launch
import retrofit2.await

class VoterInfoViewModel(private val application: Application) : ViewModel() {
    val showLoadingEvent: BaseSingleLiveEvent<Boolean> = BaseSingleLiveEvent()
    private val electionDao = getInstance(application).electionDao
    private val electionRepositories = ElectionRepositories(electionDao)
    var election: Election? = null

    //TODO: Add live data to hold voter info
    private val _voterInfoResponseMutableLiveData = MutableLiveData<VoterInfoResponse>()
    val voterInfoResponseLiveData: LiveData<VoterInfoResponse> get() = _voterInfoResponseMutableLiveData

    private val _saveButtonNameMutableLiveData = MutableLiveData<String>()
    val saveButtonNameLiveData: LiveData<String> get() = _saveButtonNameMutableLiveData

    private val _addressMutableLiveData = MutableLiveData<String>()
    val addressLiveData: LiveData<String> get() = _addressMutableLiveData

    private val _electionMutableLiveData = MutableLiveData<Election?>()
    val electionLiveData: LiveData<Election?> get() = _electionMutableLiveData

    //TODO: Add var and methods to populate voter info
    fun getPopulateVoterInfo(id: String, division: String) {
        val divisionAdapter = ElectionAdapter()
        showLoadingEvent.value = true
        viewModelScope.launch {
            try {
                val result =
                    CivicsApi.retrofitService.fetchVoterInfo(
                        electionId = id.toLong(),
                        address = division,
                        productionDataOnly = true,
                        returnAllAvailableData = true,
                    ).await()
                showLoadingEvent.postValue(false)
                election = Election(
                    id = result.election.id,
                    name = result.election.name,
                    electionDay = result.election.electionDay,
                    ocdDivisionId = result.election.ocdDivisionId,
                    division = divisionAdapter.divisionFromJson(result.election.ocdDivisionId)
                )
                _voterInfoResponseMutableLiveData.value = result
                if (result.state.isNullOrEmpty()) {
                    _addressMutableLiveData.value = ""
                } else {
                    _addressMutableLiveData.value =
                        result.state.first().electionAdministrationBody.correspondenceAddress?.line1
                            ?: ""
                }
                Log.i("ELECTIONS SUCCESS DATA:", "${_voterInfoResponseMutableLiveData.value}")
            } catch (e: Exception) {
                Log.e("ELECTIONS ERROR FAILURE MESSAGE:", "${e.message}")
            }
        }
    }

    //TODO: Add var and methods to support loading URLs

    //TODO: Add var and methods to save and remove elections to local database
    //TODO: cont'd -- Populate initial state of save button to reflect proper action based on election saved status
    fun saveElectionToDatabase() {
        viewModelScope.launch {
            if (election != null) {
                electionRepositories.insertElection(election!!)
            }
        }
    }

    fun removeElectionById(id: String) {
        viewModelScope.launch {
            electionRepositories.deleteElectionItemById(id)
        }
    }

    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */

    fun getElectionById(id: String) {
        viewModelScope.launch {
            when (val result = electionRepositories.getElectionItemById(id)) {
                is BaseResult.Success<Election> -> {
                    val electionData = result.data as Election?
                    if (electionData != null) {
                        _electionMutableLiveData.value = electionData
                        _saveButtonNameMutableLiveData.value =
                            application.getString(R.string.unfollow)
                    } else {
                        _saveButtonNameMutableLiveData.value =
                            application.getString(R.string.follow)
                    }
                }

                is BaseResult.Error -> {
                    Log.e("Database Error Message:", "${result.message}")
                    _saveButtonNameMutableLiveData.value =
                        application.getString(R.string.follow)
                }
            }
        }
    }

}