package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.*
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.repository.ElectionsRepository
import com.example.android.politicalpreparedness.representative.model.Representative
import com.example.android.politicalpreparedness.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class RepresentativeViewModel(private val repository: ElectionsRepository) : ViewModel() {

    private val _line1 = MutableLiveData("")
    val line1: MutableLiveData<String> = _line1
    private val _line2 = MutableLiveData("")
    val line2: MutableLiveData<String> = _line2
    private val _city = MutableLiveData("")
    val city: MutableLiveData<String> = _city
    private val _state = MutableLiveData("")
    val state: MutableLiveData<String> = _state
    private val _zip = MutableLiveData("")
    val zip: MutableLiveData<String> = _zip

    val dataLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val message: SingleLiveEvent<Int> = SingleLiveEvent()
    val messageString = SingleLiveEvent<String>()

    private val _representatives: MutableLiveData<Result<List<Representative>>> = MutableLiveData()
/*    val representatives: LiveData<List<Representative>> = Transformations.map(_representatives) {
        when (it) {
            is com.example.android.politicalpreparedness.repository.Result.Failure -> emptyList()
            is com.example.android.politicalpreparedness.repository.Result.Success -> it.data
            is com.example.android.politicalpreparedness.repository.Result.Loading -> emptyList()
        }
    }*/

    fun searchForRepresentatives(address: Address?) {
        if(address == null) {
            message.value = R.string.representative_failed_parse_location
            return
        }
        _line1.value = address.line1
        _line2.value = address.line2.orEmpty()
        _city.value = address.city
        _state.value = address.state
        _zip.value = address.zip
        searchRepresentativesIfFormValid(address)
    }

    fun searchForMyRepresentatives() {
        searchRepresentativesIfFormValid(
            Address(
                requireNotNull(line1.value),
                line2.value,
                requireNotNull(city.value),
                requireNotNull(state.value),
                requireNotNull(zip.value)
            )
        )
    }

    private fun searchRepresentativesIfFormValid(address: Address) {
        if (address.line1.isBlank()) {
            message.value = R.string.error_missing_first_line_address
            return
        }
        if (address.city.isBlank()) {
            message.value = R.string.error_missing_city
            return
        }
        if (address.state.isBlank()) {
            message.value = R.string.error_missing_state
            return
        }
        if (address.zip.isBlank()) {
            message.value = R.string.error_missing_zip
            return
        }
        search(address)
    }

    private fun search(address: Address) {
        dataLoading.value = true
        viewModelScope.launch {
            _representatives.value = repository.searchRepresentatives(address)
            when (val result = _representatives.value) {
                is com.example.android.politicalpreparedness.repository.Result.Failure<*>
                -> messageString.value = result.exception.message
                is com.example.android.politicalpreparedness.repository.Result.Success<*>,
                is com.example.android.politicalpreparedness.repository.Result.Loading<*>,
                null -> {
                }
            }
            dataLoading.value = false
        }
    }

    fun setState(state: String?) {
        _state.value = state.orEmpty()
    }
}
