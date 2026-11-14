package dev.dhruva.jansevakendra.feature.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.dhruva.jansevakendra.designsystem.*
import dev.dhruva.jansevakendra.navigation.LocalNavigator
import dev.dhruva.jansevakendra.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel { AuthViewModel() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AuthNavigationEvent.NavigateToOtp -> navigator.navigateTo(Screen.OtpVerification(event.mobileNumber))
                AuthNavigationEvent.NavigateToHome -> navigator.replaceWith(Screen.Home)
                is AuthNavigationEvent.ShowToast -> {}
            }
        }
    }

    // Shake animation on error
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(uiState.errorShakeCounter) {
        if (uiState.errorShakeCounter > 0) {
            for (i in 0..2) {
                shakeOffset.animateTo(12f, tween(50, easing = LinearEasing))
                shakeOffset.animateTo(-12f, tween(50, easing = LinearEasing))
            }
            shakeOffset.animateTo(0f, spring(stiffness = Spring.StiffnessHigh))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(32.dp))

            // Official Logo & Civic Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                JskLogo(size = 48.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Citizen Login",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = JskColors.SaffronPrimary
                        )
                    )
                    Text(
                        text = "Mobile Authentication",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Enter your 10-digit mobile number linked with your Aadhaar or Jan Seva Kendra ID. We'll send an authentication OTP.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(Modifier.height(36.dp))

            // Input card with shake modifier
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = shakeOffset.value.dp)
            ) {
                JskTextField(
                    value = uiState.mobileNumber,
                    onValueChange = viewModel::onMobileNumberChanged,
                    label = "Mobile Number",
                    placeholder = "98765 43210",
                    isError = uiState.errorMessage != null,
                    errorMessage = uiState.errorMessage,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🇮🇳 +91",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            // Consent Notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "🔒",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "By continuing, you agree to receive official service updates and consent to Digital India authentication protocols.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(Modifier.weight(1f))

            // Action Button
            JskPrimaryButton(
                text = "Get OTP",
                onClick = viewModel::requestOtp,
                isLoading = uiState.isLoading,
                enabled = uiState.mobileNumber.length == 10,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
