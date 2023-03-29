package com.example.android.politicalpreparedness.representative

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import com.example.android.politicalpreparedness.DataBindFragment
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.example.android.politicalpreparedness.utils.LocationPermissionsUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.snackbar.Snackbar
import org.koin.java.KoinJavaComponent.inject
import java.util.Locale

class RepresentativeFragment : DataBindFragment<FragmentRepresentativeBinding>(),
    LocationPermissionsUtil.PermissionListener {

    private val permissionUtil = LocationPermissionsUtil(this)
    lateinit var fusedLocationClient: FusedLocationProviderClient

    val viewModel: RepresentativeViewModel by viewModel()
    val geocoderHelperFactory: GeocoderHelper.Factory by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRepresentativeBinding.inflate(layoutInflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.representativeContainer.setTransition(R.id.start, R.id.start)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.representativeRecycler.adapter =
            RepresentativeListAdapter(RepresentativeListAdapter.RepresentativeListener {})

        viewModel.representatives.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                binding.representativeContainer.setTransition(R.id.start, R.id.start)
            } else {
                binding.representativeContainer.setTransition(R.id.start, R.id.end)
            }
        }
        viewModel.message.observe(viewLifecycleOwner) {
            it?.let {
                showSnackbar(getString(it))
            }
        }

        viewModel.messageString.observe(viewLifecycleOwner) {
            it?.let {
                showSnackbar(it)
            }
        }
        viewModel.dataLoading.observe(viewLifecycleOwner) {
            if (it) {
                hideKeyboard()
            }
        }

        binding.state.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                viewModel.setState(requireContext().resources.getStringArray(R.array.states)[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.buttonLocation.setOnClickListener {
            permissionUtil.requestPermissions()
        }
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionUtil.registerForPermissionResults(this)
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    private fun getLocation() {
        binding.representativesLoading.fadeIn()
        fusedLocationClient.getCurrentLocation(100, object : CancellationToken() {
            override fun isCancellationRequested(): Boolean {
                return false
            }

            override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                return this
            }

        }).addOnSuccessListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.searchForRepresentatives(geoCodeLocation(it))
            }

        }.addOnCompleteListener {
            binding.representativesLoading.fadeOut()
        }.addOnFailureListener {
            binding.representativesLoading.fadeOut()
        }
    }

    private suspend fun geoCodeLocation(location: Location): Address? {
        if (Geocoder.isPresent().not()) return null

        val helper = geocoderHelperFactory.create(requireContext())
        val address = helper.getAddressFromLocation(location)
        return address?.let {
            Address(
                it.thoroughfare.orEmpty(),
                it.subThoroughfare.orEmpty(),
                it.locality.orEmpty(),
                it.adminArea.orEmpty(),
                it.postalCode.orEmpty()
            )
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onGranted() {
        getLocation()
    }

    override fun onDenied() {
        showSnackbar(getString(R.string.error_location_permission_denied))
    }

    private fun showSnackbar(text: String) {
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
    }
}