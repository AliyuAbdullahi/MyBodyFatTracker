package com.lekan.bodyfattracker.ui.theme

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class NavViewModel : ViewModel() {

    val backStack = mutableStateListOf<Screen>(Screen.Home)

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