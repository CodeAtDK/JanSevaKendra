package dev.dhruva.jansevakendra

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.dhruva.jansevakendra.designsystem.JanSevaTheme
import dev.dhruva.jansevakendra.feature.auth.AuthScreen
import dev.dhruva.jansevakendra.feature.auth.OtpVerificationScreen
import dev.dhruva.jansevakendra.feature.home.HomeScreen
import dev.dhruva.jansevakendra.feature.onboarding.LanguageSelectionScreen
import dev.dhruva.jansevakendra.feature.onboarding.OnboardingScreen
import dev.dhruva.jansevakendra.feature.splash.SplashScreen
import dev.dhruva.jansevakendra.navigation.LocalNavigator
import dev.dhruva.jansevakendra.navigation.Navigator
import dev.dhruva.jansevakendra.navigation.Screen

@Composable
@Preview
fun App() {
    val navigator = remember { Navigator(Screen.Splash) }
    val currentScreen by navigator.currentScreen.collectAsStateWithLifecycle()

    JanSevaTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalNavigator provides navigator) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        val isForward = when {
                            initialState is Screen.Splash -> true
                            initialState is Screen.LanguageSelection && targetState is Screen.Onboarding -> true
                            initialState is Screen.Onboarding && targetState is Screen.Auth -> true
                            initialState is Screen.Auth && targetState is Screen.OtpVerification -> true
                            initialState is Screen.OtpVerification && targetState is Screen.Home -> true
                            else -> false
                        }

                        if (isForward) {
                            (slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(350)
                            ) + fadeIn(animationSpec = tween(300)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> -fullWidth / 3 },
                                        animationSpec = tween(350)
                                    ) + fadeOut(animationSpec = tween(300))
                                )
                        } else {
                            (slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth / 3 },
                                animationSpec = tween(350)
                            ) + fadeIn(animationSpec = tween(300)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> fullWidth },
                                        animationSpec = tween(350)
                                    ) + fadeOut(animationSpec = tween(300))
                                )
                        }
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        Screen.Splash -> SplashScreen()
                        Screen.LanguageSelection -> LanguageSelectionScreen()
                        Screen.Onboarding -> OnboardingScreen()
                        Screen.Auth -> AuthScreen()
                        is Screen.OtpVerification -> OtpVerificationScreen(mobileNumber = screen.mobileNumber)
                        Screen.Home -> HomeScreen()
                    }
                }
            }
        }
    }
}
