package com.bachhoangxuan.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
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
import com.bachhoangxuan.android.politicalpreparedness.util.Constants
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import java.util.Locale

@Suppress("DEPRECATION")
class RepresentativeFragment : Fragment() {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val motionLayoutStateKey = "motionLayoutState"
        private const val recyclerViewStateKey = "recyclerViewState"
        private const val line1Key = "line1"
        private const val line2Key = "line2"
        private const val cityKey = "city"
        private const val stateKey = "state"
        private const val zipKey = "zip"
    }

    private val viewModel: RepresentativeViewModel by activityViewModels()
    private var addressUser: Address? = null
    private lateinit var binding: FragmentRepresentativeBinding
    private var recyclerViewState: Parcelable? = null
    private lateinit var map: GoogleMap

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
        savedInstanceState?.let {
            try {
                addressUser = Address(
                    line1 = it.getString(line1Key) ?: Constants.EMPTY_STRING,
                    line2 = it.getString(line2Key) ?: Constants.EMPTY_STRING,
                    city = it.getString(cityKey) ?: Constants.EMPTY_STRING,
                    state = it.getString(stateKey) ?: Constants.EMPTY_STRING,
                    zip = it.getString(zipKey) ?: Constants.EMPTY_STRING,
                )
                val motionLayoutState = it.getInt(motionLayoutStateKey, -1)
                if (motionLayoutState != -1) {
                    binding.representativeMotionLayout.transitionToState(
                        motionLayoutState
                    )
                }
                recyclerViewState = it.getParcelable(recyclerViewStateKey)
            } catch (e: Exception) {
                Log.e("savedInstanceState ERROR:", "${e.message}")
            }
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

    @SuppressLint("MissingPermission")
    private fun getLocation() {

        if (isPermissionLocationGranted()) {
            val fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    geoCodeLocation(location).let { address ->
                        viewModel.setAddress(address)
                        viewModel.fetchRepresentatives(address)
                    }
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
        return address?.map { address1 ->
            Address(
                line1 = address1.thoroughfare ?: Constants.EMPTY_STRING,
                line2 = address1.subThoroughfare ?: Constants.EMPTY_STRING,
                city = address1.locality ?: Constants.EMPTY_STRING,
                state = address1.adminArea ?: Constants.EMPTY_STRING,
                zip = address1.postalCode ?: Constants.EMPTY_STRING
            )
        }!!.first()
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
        addressUser?.let {
            with(outState) {
                putString(line1Key, it.line1)
                putString(line2Key, it.line2)
                putString(cityKey, it.city)
                putString(stateKey, it.state)
                putString(zipKey, it.zip)
            }

        }
        val currentState = binding.representativeMotionLayout.currentState
        with(outState) {
            putInt(motionLayoutStateKey, currentState)
            putParcelable(
                recyclerViewStateKey,
                binding.representativeRecycler.layoutManager?.onSaveInstanceState(),
            )
        }
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
