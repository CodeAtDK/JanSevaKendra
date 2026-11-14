package dev.dhruva.jansevakendra.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.dhruva.jansevakendra.core.di.CoreContainer
import dev.dhruva.jansevakendra.core.model.CitizenProfile
import dev.dhruva.jansevakendra.core.usecase.SendOtpUseCase
import dev.dhruva.jansevakendra.core.usecase.VerifyOtpUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val mobileNumber: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val isOtpSent: Boolean = false,
    val resendCountdownSeconds: Int = 30,
    val canResendOtp: Boolean = false,
    val errorMessage: String? = null,
    val errorShakeCounter: Int = 0,
    val authenticatedProfile: CitizenProfile? = null
)

sealed interface AuthNavigationEvent {
    data class NavigateToOtp(val mobileNumber: String) : AuthNavigationEvent
    data object NavigateToHome : AuthNavigationEvent
    data class ShowToast(val message: String) : AuthNavigationEvent
}

class AuthViewModel(
    private val sendOtpUseCase: SendOtpUseCase = CoreContainer.sendOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase = CoreContainer.verifyOtpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthNavigationEvent>(replay = 0)
    val events: SharedFlow<AuthNavigationEvent> = _events.asSharedFlow()

    private var countdownJob: Job? = null

    fun onMobileNumberChanged(number: String) {
        val filtered = number.filter { it.isDigit() }.take(10)
        _uiState.update { it.copy(mobileNumber = filtered, errorMessage = null) }
    }

    fun onOtpChanged(otpValue: String) {
        val filtered = otpValue.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(otp = filtered, errorMessage = null) }
        if (filtered.length == 6) {
            verifyOtp()
        }
    }

    fun requestOtp() {
        val mobile = _uiState.value.mobileNumber
        if (mobile.length != 10) {
            _uiState.update {
                it.copy(
                    errorMessage = "Please enter a valid 10-digit mobile number.",
                    errorShakeCounter = it.errorShakeCounter + 1
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = sendOtpUseCase(mobile)
            result.onSuccess {
                _uiState.update { state ->
                    state.copy(isLoading = false, isOtpSent = true)
                }
                startResendTimer()
                _events.emit(AuthNavigationEvent.NavigateToOtp(mobile))
            }.onError { error ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = error.toUserMessage(),
                        errorShakeCounter = state.errorShakeCounter + 1
                    )
                }
            }
        }
    }

    fun verifyOtp() {
        val mobile = _uiState.value.mobileNumber
        val otp = _uiState.value.otp
        if (otp.length != 6) {
            _uiState.update {
                it.copy(
                    errorMessage = "Please enter complete 6-digit OTP.",
                    errorShakeCounter = it.errorShakeCounter + 1
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = verifyOtpUseCase(mobile, otp)
            result.onSuccess { profile ->
                _uiState.update { it.copy(isLoading = false, authenticatedProfile = profile) }
                _events.emit(AuthNavigationEvent.NavigateToHome)
            }.onError { error ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = error.toUserMessage(),
                        errorShakeCounter = state.errorShakeCounter + 1
                    )
                }
            }
        }
    }

    fun resendOtp() {
        if (!_uiState.value.canResendOtp) return
        requestOtp()
    }

    private fun startResendTimer() {
        countdownJob?.cancel()
        _uiState.update { it.copy(resendCountdownSeconds = 30, canResendOtp = false) }
        countdownJob = viewModelScope.launch {
            for (sec in 29 downTo 0) {
                delay(1000)
                _uiState.update {
                    it.copy(
                        resendCountdownSeconds = sec,
                        canResendOtp = sec == 0
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
