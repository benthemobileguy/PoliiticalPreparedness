package com.example.android.politicalpreparedness.election
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.android.politicalpreparedness.DataBindFragment
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.utils.LocationPermissionsUtil
import org.koin.android.ext.android.inject
import java.text.DateFormat

class ElectionsFragment : DataBindFragment<FragmentElectionBinding>(), LocationPermissionsUtil.PermissionListener {

    private val viewModel: ElectionsViewModel by viewModel()
    val dateFormatter: DateFormat by inject()

    private val permissionUtil = LocationPermissionsUtil(this)

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentElectionBinding.inflate(layoutInflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.upcomingElectionsRecycler.adapter =
            ElectionListAdapter(dateFormatter, ElectionListAdapter.ElectionListener {
                viewModel.onUpcomingClicked(it)
            })

        binding.savedElectionsRecycler.adapter =
            ElectionListAdapter(dateFormatter, ElectionListAdapter.ElectionListener {
                viewModel.onSavedClicked(it)
            })
        binding.upcomingRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.navigateTo.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.navigateCompleted()
                findNavController().navigate(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.refresh()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionUtil.registerForResultAndRequestPermissions(this)
    }

    override fun onDestroyView() {
        permissionUtil.unregister()
        super.onDestroyView()
    }

    override fun onGranted() {
    }

    override fun onDenied() {
        showToast(getString(R.string.error_location_permission_denied))
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}
