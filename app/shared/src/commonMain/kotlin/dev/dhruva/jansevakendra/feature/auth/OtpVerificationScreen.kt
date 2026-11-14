package dev.dhruva.jansevakendra.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.dhruva.jansevakendra.designsystem.*
import dev.dhruva.jansevakendra.navigation.LocalNavigator
import dev.dhruva.jansevakendra.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OtpVerificationScreen(
    mobileNumber: String,
    viewModel: AuthViewModel = viewModel { AuthViewModel() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current

    LaunchedEffect(mobileNumber) {
        viewModel.onMobileNumberChanged(mobileNumber)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                AuthNavigationEvent.NavigateToHome -> navigator.popToRootAndNavigate(Screen.Home)
                else -> {}
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
            // Back button
            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .bouncyClickable(onClick = { navigator.pop() }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "←",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Verification (सत्यापन)",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Enter 6-digit OTP sent to",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "+91 $mobileNumber",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = JskColors.SaffronPrimary
                )
            )

            Spacer(Modifier.height(8.dp))

            // Test hint pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(JskColors.SaffronContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "💡 Test Demo OTP: 123456",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = JskColors.OnSaffronContainer
                    )
                )
            }

            Spacer(Modifier.height(36.dp))

            // 6-digit OTP Box Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = shakeOffset.value.dp),
                contentAlignment = Alignment.Center
            ) {
                // Invisible real text field on top
                BasicTextField(
                    value = uiState.otp,
                    onValueChange = viewModel::onOtpChanged,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    decorationBox = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (i in 0 until 6) {
                                val digit = uiState.otp.getOrNull(i)?.toString() ?: ""
                                val isFocused = uiState.otp.length == i || (i == 5 && uiState.otp.length == 6)

                                val borderColor by animateColorAsState(
                                    targetValue = when {
                                        uiState.errorMessage != null -> MaterialTheme.colorScheme.error
                                        isFocused -> JskColors.SaffronPrimary
                                        digit.isNotEmpty() -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.outline
                                    },
                                    animationSpec = tween(200),
                                    label = "OtpBorderColor"
                                )

                                val boxScale by animateFloatAsState(
                                    targetValue = if (isFocused) 1.05f else 1f,
                                    animationSpec = MotionTokens.BouncySpring,
                                    label = "OtpBoxScale"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp)
                                        .scale(boxScale)
                                        .shadow(if (isFocused) 4.dp else 0.dp, RoundedCornerShape(14.dp))
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(
                                            width = if (isFocused) 2.dp else 1.2.dp,
                                            color = borderColor,
                                            shape = RoundedCornerShape(14.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = digit,
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                            }
                        }
                    }
                )
            }

            AnimatedVisibility(visible = uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                    modifier = Modifier.padding(top = 12.dp, start = 4.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            // Resend timer row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.canResendOtp) {
                    TextButton(onClick = viewModel::resendOtp) {
                        Text(
                            text = "Resend OTP (ओटीपी पुनः भेजें)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = JskColors.SaffronPrimary
                            )
                        )
                    }
                } else {
                    Text(
                        text = "Resend OTP in 00:${uiState.resendCountdownSeconds.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Verify button
            JskPrimaryButton(
                text = "Verify & Proceed (पुष्टि करें)",
                onClick = viewModel::verifyOtp,
                isLoading = uiState.isLoading,
                enabled = uiState.otp.length == 6,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
