package com.lekan.bodyfattracker.ui.history

import androidx.lifecycle.viewModelScope
import com.lekan.bodyfattracker.domain.IBodyFatInfoRepository
import com.lekan.bodyfattracker.model.BodyFatInfo
import com.lekan.bodyfattracker.ui.core.CoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryState(
    val isLoading: Boolean = true,
    val recordedMeasurements: List<BodyFatInfo> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: IBodyFatInfoRepository
) : CoreViewModel<HistoryState>() {
    override fun initialize(): HistoryState = HistoryState()

    fun start() {
        viewModelScope.launch {
            updateState { copy(recordedMeasurements = repository.getBodyFatInfoList(), isLoading = false) }
        }
    }
}