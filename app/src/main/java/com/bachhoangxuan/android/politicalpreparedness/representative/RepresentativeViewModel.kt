package com.bachhoangxuan.android.politicalpreparedness.representative

import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bachhoangxuan.android.politicalpreparedness.network.CivicsApi
import com.bachhoangxuan.android.politicalpreparedness.network.models.Address
import com.bachhoangxuan.android.politicalpreparedness.representative.model.Representative
import com.bachhoangxuan.android.politicalpreparedness.util.Constants
import kotlinx.coroutines.launch
import retrofit2.await

class RepresentativeViewModel : ViewModel() {

    var _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    var _addressInput = MutableLiveData(
        Address(
            line1 = Constants.EMPTY_STRING,
            city = Constants.EMPTY_STRING,
            state = Constants.EMPTY_STRING,
            zip = Constants.EMPTY_STRING
        )
    )
    val addressInput: LiveData<Address>
        get() = _addressInput

//    private val savedStateHandle = savedStateHandle

    /**
     *  The following code will prove helpful in constructing a representative from the API. This code combines the two nodes of the RepresentativeResponse into a single official
     *  val (offices, officials) = getRepresentativesDeferred.await()
     *     _representatives.value = offices.flatMap { office -> office.getRepresentatives(officials) }
     *     Note: getRepresentatives in the above code represents the method used to fetch data from the API
     *     Note: _representatives in the above code represents the established mutable live data housing representatives
     */
    fun fetchRepresentatives(address: Address){
        viewModelScope.launch {
            try {
                val (offices, officials) =
                    CivicsApi.retrofitService.fetchReps(address = address.toFormattedString())
                        .await()
                _representatives.value =
                    offices.flatMap { office -> office.getRepresentatives(officials) }
            } catch (e: Exception) {
                Log.e("Fetch reps error", "${e.message}")
            }
        }
    }

    fun setState(state: String) {
        _addressInput.value?.state = state
    }

    fun setAddress(address: Address) {
        _addressInput.value = address
    }

}
