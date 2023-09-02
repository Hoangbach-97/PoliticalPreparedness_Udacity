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
import com.bachhoangxuan.android.politicalpreparedness.database.ElectionRepo
import com.bachhoangxuan.android.politicalpreparedness.network.CivicsApi
import com.bachhoangxuan.android.politicalpreparedness.network.jsonadapter.ElectionAdapter
import com.bachhoangxuan.android.politicalpreparedness.network.models.Election
import com.bachhoangxuan.android.politicalpreparedness.network.models.VoterInfoResponse
import com.bachhoangxuan.android.politicalpreparedness.util.BaseSingleLiveEvent
import com.bachhoangxuan.android.politicalpreparedness.util.Constants
import kotlinx.coroutines.launch
import retrofit2.await

class VoterInfoViewModel(private val application: Application) : ViewModel() {
    val showLoading: BaseSingleLiveEvent<Boolean> = BaseSingleLiveEvent()
    private val electionDao = getInstance(application).electionDao
    private val electionRepo = ElectionRepo(electionDao)
    var election: Election? = null

    private val _voterInfoResponse = MutableLiveData<VoterInfoResponse>()
    val voterInfoResponse: LiveData<VoterInfoResponse> get() = _voterInfoResponse

    private val _saveButtonName = MutableLiveData<String>()
    val saveButtonName: LiveData<String> get() = _saveButtonName

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> get() = _address

    private val _electionLiveData = MutableLiveData<Election?>()
    val electionLiveData: LiveData<Election?> get() = _electionLiveData

    fun getVoterInfo(id: String, division: String) {
        val divisionAdapter = ElectionAdapter()
        showLoading.value = true
        viewModelScope.launch {
            try {
                val result =
                    CivicsApi.retrofitService.fetchVoterInfo(
                        electionId = id.toLong(),
                        address = division,
                        productionDataOnly = true,
                        returnAllAvailableData = true,
                    ).await()
                showLoading.postValue(false)

                election = Election(
                    id = result.election.id,
                    name = result.election.name,
                    electionDay = result.election.electionDay,
                    ocdDivisionId = result.election.ocdDivisionId,
                    division = divisionAdapter.divisionFromJson(result.election.ocdDivisionId)
                )

                _voterInfoResponse.value = result
                if (result.state.isNullOrEmpty()) {
                    _address.value = Constants.EMPTY_STRING
                } else {
                    _address.value =
                        result.state.first().electionAdministrationBody.correspondenceAddress?.line1
                            ?: Constants.EMPTY_STRING
                }
            } catch (e: Exception) {
                Log.e("Election error", "${e.message}")
            }
        }
    }

    fun saveElection() {
        viewModelScope.launch {
            if (election != null) {
                electionRepo.insert(election!!)
            }
        }
    }

    fun deleteElection(id: String) {
        viewModelScope.launch {
            electionRepo.deleteById(id)
        }
    }

    /**
     * Hint: The saved state can be accomplished in multiple ways. It is directly related to how elections are saved/removed from the database.
     */

    fun getElection(id: String) {
        viewModelScope.launch {
            when (val result = electionRepo.getById(id)) {
                is BaseResult.Success<Election> -> {
                    val electionData = result.data as Election?
                    if (electionData != null) {
                        _electionLiveData.value = electionData
                        _saveButtonName.value =
                            application.getString(R.string.unfollow)
                    } else {
                        _saveButtonName.value =
                            application.getString(R.string.follow)
                    }
                }

                is BaseResult.Error -> {
                    Log.e("Database Error", "${result.message}")
                    _saveButtonName.value =
                        application.getString(R.string.follow)
                }
            }
        }
    }

}