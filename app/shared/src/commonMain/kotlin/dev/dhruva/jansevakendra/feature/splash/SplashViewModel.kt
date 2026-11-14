package dev.dhruva.jansevakendra.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.dhruva.jansevakendra.core.di.CoreContainer
import dev.dhruva.jansevakendra.core.usecase.ManageSessionUseCase
import dev.dhruva.jansevakendra.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface SplashNavigationEvent {
    data class NavigateTo(val screen: Screen) : SplashNavigationEvent
}

class SplashViewModel(
    private val manageSessionUseCase: ManageSessionUseCase = CoreContainer.manageSessionUseCase
) : ViewModel() {

    private val _events = MutableSharedFlow<SplashNavigationEvent>(replay = 0)
    val events: SharedFlow<SplashNavigationEvent> = _events.asSharedFlow()

    init {
        resolveStartingScreen()
    }

    private fun resolveStartingScreen() {
        viewModelScope.launch {
            // Keep splash animation visible for smooth brand perception
            delay(1600)

            val profile = manageSessionUseCase.observeProfile().first()
            val isOnboardingDone = manageSessionUseCase.observeOnboarding().first()

            val targetScreen = when {
                profile != null -> Screen.Home
                isOnboardingDone -> Screen.Auth
                else -> Screen.LanguageSelection
            }

            _events.emit(SplashNavigationEvent.NavigateTo(targetScreen))
        }
    }
}
