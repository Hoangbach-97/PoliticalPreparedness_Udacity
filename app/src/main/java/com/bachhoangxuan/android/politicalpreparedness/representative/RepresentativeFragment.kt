package com.bachhoangxuan.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bachhoangxuan.android.politicalpreparedness.R
import com.bachhoangxuan.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.bachhoangxuan.android.politicalpreparedness.network.models.Address
import com.bachhoangxuan.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import java.util.Locale

@Suppress("DEPRECATION")
class RepresentativeFragment : Fragment() {

    companion object {
        //TODO: Add Constant for Location request
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val motionLayoutStateKey = "motionLayoutState"
        private const val recyclerViewStateKey = "recyclerViewState"
        private const val addressLine1Key = "line1"
        private const val addressLine2Key = "line2"
        private const val addressCityKey = "city"
        private const val addressStateKey = "state"
        private const val addressZipKey = "zip"
    }

    //TODO: Declare ViewModel
    private val viewModel: RepresentativeViewModel by activityViewModels()
    private var addressUser: Address? = null
    private lateinit var fragmentRepresentativeBinding: FragmentRepresentativeBinding
    private var recyclerViewState: Parcelable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        //TODO: Establish bindings
        fragmentRepresentativeBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_representative, container, false
        )

        //TODO: Define and assign Representative adapter
        fragmentRepresentativeBinding.representativeViewModel = viewModel
        fragmentRepresentativeBinding.lifecycleOwner = this

        //TODO: Populate Representative adapter
        val representativeAdapter = RepresentativeListAdapter()
        fragmentRepresentativeBinding.representativeRecycler.adapter = representativeAdapter
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.states,
            android.R.layout.simple_spinner_item,
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fragmentRepresentativeBinding.state.adapter = adapter
        fragmentRepresentativeBinding.state.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    viewModel.setState(parent?.getItemAtPosition(position)?.toString() ?: "")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        // Restore data from onSaveInstanceState
        savedInstanceState?.let {
            try {
                addressUser = Address(
                    line1 = it.getString(addressLine1Key) ?: "",
                    line2 = it.getString(addressLine2Key) ?: "",
                    city = it.getString(addressCityKey) ?: "",
                    state = it.getString(addressStateKey) ?: "",
                    zip = it.getString(addressZipKey) ?: "",
                )
                val motionLayoutState = it.getInt(motionLayoutStateKey, -1)
                if (motionLayoutState != -1) {
                    fragmentRepresentativeBinding.representativeMotionLayout.transitionToState(
                        motionLayoutState
                    )
                }
                recyclerViewState = it.getParcelable(recyclerViewStateKey)
            } catch (e: Exception) {
                Log.e("savedInstanceState ERROR:", "${e.message}")
            }
        }
        viewModel.addressInputLiveData.observe(viewLifecycleOwner) { addressUser = it }
        viewModel.representativesLiveData.observe(viewLifecycleOwner) {
            it?.let { representativeAdapter.submitList(it) }
        }

        //TODO: Establish button listeners for field and location search
        fragmentRepresentativeBinding.buttonLocation.setOnClickListener {
            hideKeyboard()
            checkLocationPermissions()
        }
        fragmentRepresentativeBinding.buttonSearch.setOnClickListener {
            if (addressUser != null) {
                viewModel.fetchRepresentatives(addressUser!!)
            }
        }
        return fragmentRepresentativeBinding.root

    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //TODO: Handle location permission result to get location on permission granted
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.enable_location),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun checkLocationPermissions() {
        if (isPermissionGranted()) {
            getLocation()
        } else {
            //TODO: Request Location permissions
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION,
            )
        }
    }

    private fun isPermissionGranted(): Boolean {
        //TODO: Check if permission is already granted and return (true = granted, false = denied/other)
        val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        return ContextCompat.checkSelfPermission(
            requireContext(), locationPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        //TODO: Get location from LocationServices
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        //TODO: The geoCodeLocation method is a helper function to change the lat/long location to a human readable street address
        if (lastKnownLocation != null) {
            geoCodeLocation(lastKnownLocation).let { address ->
                viewModel.setAddress(address)
                viewModel.fetchRepresentatives(address)
            }
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.enable_location),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        Log.i("geoCodeLocation address: ", "$address")
        return address?.map { address1 ->
            Address(
                line1 = address1.thoroughfare ?: "",
                line2 = address1.subThoroughfare ?: "",
                city = address1.locality ?: "",
                state = address1.adminArea ?: "",
                zip = address1.postalCode ?: ""
            )
        }!!.first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save data to the addressUser model
        addressUser?.let {
            outState.putString(addressLine1Key, it.line1)
            outState.putString(addressLine2Key, it.line2)
            outState.putString(addressCityKey, it.city)
            outState.putString(addressStateKey, it.state)
            outState.putString(addressZipKey, it.zip)
        }
        val mlCurrentState = fragmentRepresentativeBinding.representativeMotionLayout.currentState
        outState.putInt(motionLayoutStateKey, mlCurrentState)
        outState.putParcelable(
            recyclerViewStateKey,
            fragmentRepresentativeBinding.representativeRecycler.layoutManager?.onSaveInstanceState(),
        )
    }

    override fun onResume() {
        super.onResume()
        recyclerViewState?.let {
            fragmentRepresentativeBinding.representativeRecycler.layoutManager?.onRestoreInstanceState(
                it
            )
        }
    }

}