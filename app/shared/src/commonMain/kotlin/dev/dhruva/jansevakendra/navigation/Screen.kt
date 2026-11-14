package dev.dhruva.jansevakendra.navigation

/**
 * Screen destinations for the Jan Seva Kendra application.
 */
sealed class Screen {
    data object Splash : Screen()
    data object LanguageSelection : Screen()
    data object Onboarding : Screen()
    data object Auth : Screen()
    data class OtpVerification(val mobileNumber: String) : Screen()
    data object Home : Screen()
}
