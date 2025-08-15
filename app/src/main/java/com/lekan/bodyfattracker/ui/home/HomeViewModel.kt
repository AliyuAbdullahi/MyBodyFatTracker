package com.lekan.bodyfattracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.domain.IBodyFatInfoRepository
import com.lekan.bodyfattracker.domain.IProfileRepository
import com.lekan.bodyfattracker.model.BodyFatInfo
import com.lekan.bodyfattracker.ui.core.CoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: IBodyFatInfoRepository,
    private val profileRepository: IProfileRepository
) : CoreViewModel<HomeViewModel.State>() {

    override fun initialize(): State = State()

    init {
        viewModelScope.launch {
            updateState { copy(userName = profileRepository.getProfile()?.name) }
        }
    }

    data class State(
        val bodyFatInfo: BodyFatInfo? = null,
        val info: String? = null,
        val isTakingGuestMeasurement: Boolean = false,
        val userName: String? = null
    )

    fun update() {
        val bodyFatInfo = repository.getLastBodyFatInfo()
        viewModelScope.launch {
            updateState { copy(bodyFatInfo = bodyFatInfo) }
        }
    }

    fun onMoreInfoClicked(info: String) {
        viewModelScope.launch {
            updateState { copy(info = info) }
        }
    }

    fun takeGuestMeasurement() {
        viewModelScope.launch {
            updateState { copy(isTakingGuestMeasurement = true) }
        }
    }

    fun clearGuestMeasurement() {
        viewModelScope.launch {
            updateState { copy(isTakingGuestMeasurement = false) }
        }
    }

    fun onClearInfoClicked() {
        viewModelScope.launch {
            updateState {
                copy(
                    info = null
                )
            }
        }
    }
}