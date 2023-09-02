package com.bachhoangxuan.android.politicalpreparedness.representative

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bachhoangxuan.android.politicalpreparedness.network.CivicsApi
import com.bachhoangxuan.android.politicalpreparedness.network.models.Address
import com.bachhoangxuan.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch
import retrofit2.await

class RepresentativeViewModel : ViewModel() {

    //TODO: Establish live data for representatives and address
    private val _representativesMutableLiveData = MutableLiveData<List<Representative>>()
    val representativesLiveData: LiveData<List<Representative>>
        get() = _representativesMutableLiveData

    private val _addressInputMutableLiveData = MutableLiveData(
        Address(line1 = "", city = "", state = "", zip = "")
    )
    val addressInputLiveData: LiveData<Address>
        get() = _addressInputMutableLiveData

    //TODO: Create function to fetch representatives from API from a provided address
    /**
     *  The following code will prove helpful in constructing a representative from the API. This code combines the two nodes of the RepresentativeResponse into a single official
     *  val (offices, officials) = getRepresentativesDeferred.await()
     *     _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }
     *     Note: getRepresentatives in the above code represents the method used to fetch data from the API
     *     Note: _representatives in the above code represents the established mutable live data housing representatives
     */
    //TODO: Create function get address from geo location
    fun setState(state: String) {
        _addressInputMutableLiveData.value?.state = state
    }

    //TODO: Create function to get address from individual fields
    fun setAddress(address: Address) {
        _addressInputMutableLiveData.value = address
    }

    fun fetchRepresentatives(address: Address) {
        viewModelScope.launch {
            try {
                val (offices, officials) =
                    CivicsApi.retrofitService.fetchRepresentatives(address = address.toFormattedString())
                        .await()
                Log.e("ELECTIONS SUCCESS fetchRepresentatives:", "$offices")
                _representativesMutableLiveData.value =
                    offices.flatMap { office -> office.getRepresentatives(officials) }
            } catch (e: Exception) {
                Log.e("ELECTIONS ERROR fetchRepresentatives:", "Failure: ${e.message}")
            }
        }
    }

}
