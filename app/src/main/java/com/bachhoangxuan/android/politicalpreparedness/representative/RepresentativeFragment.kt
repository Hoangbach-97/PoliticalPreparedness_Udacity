package com.bachhoangxuan.android.politicalpreparedness.representative

import android.Manifest
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
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bachhoangxuan.android.politicalpreparedness.R
import com.bachhoangxuan.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.bachhoangxuan.android.politicalpreparedness.network.models.Address
import com.bachhoangxuan.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.bachhoangxuan.android.politicalpreparedness.representative.model.Representative
import com.bachhoangxuan.android.politicalpreparedness.util.Constants
import com.google.android.gms.maps.GoogleMap
import java.util.ArrayList
import java.util.Locale

class RepresentativeFragment : Fragment() {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val motionLayoutStateKey = "motionLayoutState"
        private const val recyclerViewStateKey = "recyclerViewState"
        private const val repsKey = "reps"
        private const val address = "address"
    }

    private val viewModel: RepresentativeViewModel by activityViewModels()
    private var addressUser: Address? = null
    private lateinit var binding: FragmentRepresentativeBinding
    private var recyclerViewState: Parcelable? = null
    private lateinit var map: GoogleMap
    private var repsList: List<Representative>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_representative, container, false
        )

        binding.representativeViewModel = viewModel
        binding.lifecycleOwner = this

        val representativeAdapter = RepresentativeListAdapter()
        binding.representativeRecycler.adapter = representativeAdapter
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.states,
            android.R.layout.simple_spinner_item,
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.state.adapter = adapter
        binding.state.onItemSelectedListener =
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
        if (savedInstanceState != null) {
            Log.d("test_kill_process", "savedInstanceState: true")
            addressUser = savedInstanceState.getParcelable(address)
            repsList = savedInstanceState.getParcelableArrayList(repsKey)

            Log.d("test_kill_process", "savedInstanceState: repsList: ${repsList?.size}")
            viewModel._addressInput.value = addressUser
            viewModel._representatives.value = repsList

            val motionLayoutState = savedInstanceState.getInt(motionLayoutStateKey, -1)
            if (motionLayoutState != -1) {
                binding.representativeMotionLayout.transitionToState(
                    motionLayoutState
                )
            }
            recyclerViewState = savedInstanceState.getParcelable(recyclerViewStateKey)
        }



        viewModel.addressInput.observe(viewLifecycleOwner) { addressUser = it }
        viewModel.representatives.observe(viewLifecycleOwner) {
            it?.let { representativeAdapter.submitList(it) }
        }

        binding.buttonLocation.setOnClickListener {
            it.hideKeyboard()
            enableLocationTracking()
        }

        binding.buttonSearch.setOnClickListener {
            if (addressUser != null) {
                viewModel.fetchRepresentatives(addressUser!!)
            }
        }
        return binding.root

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableLocationTracking()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.enable_location),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun isPermissionLocationGranted(): Boolean =
        isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) && isPermissionGranted(
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    private fun isPermissionGranted(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(
            requireContext(), permission
        ) == PackageManager.PERMISSION_GRANTED

    private fun getLocation() {

        if (isPermissionLocationGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val locationManager =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val lastKnownLocation =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if (lastKnownLocation != null) {
                geoCodeLocation(lastKnownLocation).let { address ->
                    viewModel.setAddress(address)
                    viewModel.fetchRepresentatives(address)
                }
            }
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (address != null) {
            return address.map { add ->
                Address(
                    line1 = add.thoroughfare ?: Constants.EMPTY_STRING,
                    line2 = add.subThoroughfare ?: Constants.EMPTY_STRING,
                    city = add.locality ?: Constants.EMPTY_STRING,
                    state = add.adminArea ?: Constants.EMPTY_STRING,
                    zip = add.postalCode ?: Constants.EMPTY_STRING
                )
            }.first()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.not_exist_add),
                Toast.LENGTH_SHORT,
            ).show()
            return Address()
        }
    }

    private fun enableLocationTracking() {
        if (isPermissionLocationGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            getLocation()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(address, addressUser)
        outState.putInt(motionLayoutStateKey, binding.representativeMotionLayout.currentState)
        outState.putParcelable(
            recyclerViewStateKey,
            binding.representativeRecycler.layoutManager?.onSaveInstanceState()
        )
        outState.putParcelableArrayList(repsKey, viewModel.representatives.value as ArrayList<out Parcelable> )
    }

    override fun onResume() {
        super.onResume()
        recyclerViewState?.let {
            binding.representativeRecycler.layoutManager?.onRestoreInstanceState(
                it
            )
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

}
