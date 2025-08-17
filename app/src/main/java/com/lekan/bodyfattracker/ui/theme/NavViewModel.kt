package com.lekan.bodyfattracker.ui.theme

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.lekan.bodyfattracker.domain.IProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    val profileRepository: IProfileRepository
) : ViewModel() {

    val backStack = mutableStateListOf<Screen>(Screen.Home)

    val user = profileRepository.getProfile()

    fun push(screen: Screen) {
        backStack.add(screen)
    }

    fun pop(screen: Screen) {
        backStack.remove(screen)
    }

    fun popLast() {
        backStack.removeLastOrNull()
    }
}