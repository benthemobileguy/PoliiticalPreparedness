package com.example.android.politicalpreparedness.election

import androidx.lifecycle.*
import androidx.navigation.NavDirections
import com.example.android.politicalpreparedness.network.models.toDomainModel
import com.example.android.politicalpreparedness.repository.ElectionsRepository
import com.github.mjaremczuk.politicalpreparedness.election.model.ElectionModel
import kotlinx.coroutines.launch


class ElectionsViewModel(private val repository: ElectionsRepository) : ViewModel() {

    private val _dataLoading = MutableLiveData(false)
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _navigateTo = MutableLiveData<NavDirections?>()
    val navigateTo: LiveData<NavDirections?> = _navigateTo

    private val elections: LiveData<List<ElectionModel>> =
        Transformations.map(repository.observeElections()) {
        when (it) {
            is com.example.android.politicalpreparedness.repository.Result.Failure -> {
                emptyList()
            }
            is  com.example.android.politicalpreparedness.repository.Result.Success  -> {
                it.data.toDomainModel()
            }
            is com.example.android.politicalpreparedness.repository.Result.Loading -> {
                upcomingElections.value
            }
        }
    }

    val upcomingElections: LiveData<List<ElectionModel>> = elections

    val savedElections: LiveData<List<ElectionModel>> = Transformations.map(elections) {
        it.filter { it.saved }
    }

    fun refresh() {
        _dataLoading.value = true
        viewModelScope.launch {
            refreshElections()
            _dataLoading.value = false
        }
    }

    private suspend fun refreshElections() {
        repository.refreshElections()
    }

    fun onUpcomingClicked(electionModel: ElectionModel) {
        _navigateTo.value = ElectionsFragmentDirections
            .actionElectionsFragmentToVoterInfoFragment(electionModel)
    }

    fun onSavedClicked(electionModel: ElectionModel) {
        _navigateTo.value = ElectionsFragmentDirections
            .actionElectionsFragmentToVoterInfoFragment(electionModel)
    }

    fun navigateCompleted() {
        _navigateTo.value = null
    }
}
