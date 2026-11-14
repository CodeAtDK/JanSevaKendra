package dev.dhruva.jansevakendra.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.dhruva.jansevakendra.core.di.CoreContainer
import dev.dhruva.jansevakendra.core.model.AppLanguage
import dev.dhruva.jansevakendra.core.usecase.ManageSessionUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LanguageUiState(
    val selectedLanguage: AppLanguage = AppLanguage.HINDI,
    val availableLanguages: List<AppLanguage> = AppLanguage.entries,
    val isSaving: Boolean = false
)

sealed interface LanguageNavigationEvent {
    data object NavigateToOnboarding : LanguageNavigationEvent
}

class LanguageViewModel(
    private val manageSessionUseCase: ManageSessionUseCase = CoreContainer.manageSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageUiState())
    val uiState: StateFlow<LanguageUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LanguageNavigationEvent>(replay = 0)
    val events: SharedFlow<LanguageNavigationEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            manageSessionUseCase.observeLanguage().collect { lang ->
                _uiState.update { it.copy(selectedLanguage = lang) }
            }
        }
    }

    fun onLanguageSelected(language: AppLanguage) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun onConfirm() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            manageSessionUseCase.setLanguage(_uiState.value.selectedLanguage)
            _events.emit(LanguageNavigationEvent.NavigateToOnboarding)
        }
    }
}
