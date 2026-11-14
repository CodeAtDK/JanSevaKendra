package dev.dhruva.jansevakendra.navigation

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Navigator(initialScreen: Screen = Screen.Splash) {
    private val backStack = mutableStateListOf<Screen>(initialScreen)

    private val _currentScreen = MutableStateFlow<Screen>(initialScreen)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        backStack.add(screen)
        _currentScreen.value = screen
    }

    fun replaceWith(screen: Screen) {
        if (backStack.isNotEmpty()) {
            backStack.removeAt(backStack.lastIndex)
        }
        backStack.add(screen)
        _currentScreen.value = screen
    }

    fun pop(): Boolean {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
            _currentScreen.value = backStack.last()
            return true
        }
        return false
    }

    fun popToRootAndNavigate(screen: Screen) {
        backStack.clear()
        backStack.add(screen)
        _currentScreen.value = screen
    }
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("LocalNavigator not provided! Wrap with CompositionLocalProvider(LocalNavigator provides navigator)")
}
