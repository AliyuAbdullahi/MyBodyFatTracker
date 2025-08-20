package com.lekan.bodyfattracker.ui.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class CoreViewModel<STATE> : ViewModel() {

    abstract fun initialize(): STATE

    private val _state = MutableStateFlow(initialize())
    val state: StateFlow<STATE> = _state.asStateFlow()

    val currentState
        get() = state.value

    suspend fun updateState(newState: STATE.() -> STATE) {
        val update = _state.value.newState()
        _state.emit(update)
    }

    fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }

    suspend fun applyState(state: STATE) {
        _state.emit(state)
    }
}