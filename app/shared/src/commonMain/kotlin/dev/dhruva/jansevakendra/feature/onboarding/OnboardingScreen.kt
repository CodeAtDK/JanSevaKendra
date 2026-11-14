package dev.dhruva.jansevakendra.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dhruva.jansevakendra.core.di.CoreContainer
import dev.dhruva.jansevakendra.designsystem.*
import dev.dhruva.jansevakendra.navigation.LocalNavigator
import dev.dhruva.jansevakendra.navigation.Screen
import kotlinx.coroutines.launch

data class OnboardingStep(
    val titleHindi: String,
    val titleEnglish: String,
    val description: String,
    val badgeLabel: String,
    val accentColor: Color,
    val secondaryColor: Color
)

@Composable
fun OnboardingScreen() {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    var currentStepIndex by remember { mutableStateOf(0) }

    val steps = remember {
        listOf(
            OnboardingStep(
                titleHindi = "डिजिटल सरकारी सेवाएं",
                titleEnglish = "Direct Citizen Public Services",
                description = "Apply for Aadhaar updates, Income, Caste, and Domicile certificates directly without long queues or agent fees.",
                badgeLabel = "500+ Official Services",
                accentColor = JskColors.SaffronPrimary,
                secondaryColor = JskColors.SaffronContainer
            ),
            OnboardingStep(
                titleHindi = "पारदर्शी आवेदन ट्रैकिंग",
                titleEnglish = "Real-Time Tracking & Verification",
                description = "Track your submitted applications at every administrative stage from Tehsildar desk to digital signature allotment.",
                badgeLabel = "100% Transparent",
                accentColor = JskColors.NavyPrimary,
                secondaryColor = JskColors.NavyContainer
            ),
            OnboardingStep(
                titleHindi = "सरकारी योजनाएं एवं डीबीटी",
                titleEnglish = "Welfare Schemes & Direct Benefits",
                description = "Instant eligibility matching for PM-Kisan, Ayushman Bharat, Pensions, and state scholarships with direct bank credit.",
                badgeLabel = "Direct Benefit Transfer",
                accentColor = JskColors.ForestGreen,
                secondaryColor = JskColors.GreenContainer
            )
        )
    }

    val step = steps[currentStepIndex]

    // Floating ornament animation
    val infiniteTransition = rememberInfiniteTransition(label = "OnboardingFloating")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BadgeFloat"
    )

    fun completeOnboarding() {
        coroutineScope.launch {
            CoreContainer.manageSessionUseCase.setOnboardingCompleted(true)
            navigator.replaceWith(Screen.Auth)
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                JskLogo(size = 40.dp)

                TextButton(
                    onClick = { completeOnboarding() }
                ) {
                    Text(
                        text = "Skip (छोड़ें)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(Modifier.height(30.dp))

            // Main Graphic Container with animated badge
            AnimatedContent(
                targetState = currentStepIndex,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(400)) + slideInHorizontally { width -> width / 2 })
                        .togetherWith(fadeOut(animationSpec = tween(400)) + slideOutHorizontally { width -> -width / 2 })
                },
                label = "OnboardingContent"
            ) { targetIndex ->
                val current = steps[targetIndex]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Illustration Card with gradient and floating badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .shadow(4.dp, RoundedCornerShape(28.dp))
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        current.secondaryColor,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Central decorative Logo
                        JskLogo(
                            size = 100.dp,
                            shape = RoundedCornerShape(22.dp)
                        )

                        // Floating Badge
                        Box(
                            modifier = Modifier
                                .offset(y = floatOffset.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(current.accentColor)
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = current.badgeLabel,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Text(
                        text = current.titleHindi,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = current.accentColor
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = current.titleEnglish,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = current.description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Expanding Page Indicator dots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                steps.indices.forEach { index ->
                    val isSelected = index == currentStepIndex
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 32.dp else 10.dp,
                        animationSpec = MotionTokens.DpSnappySpring,
                        label = "IndicatorWidth"
                    )
                    val color by animateColorAsState(
                        targetValue = if (isSelected) step.accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        animationSpec = tween(300),
                        label = "IndicatorColor"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(10.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Next / Get Started Action Button
            JskPrimaryButton(
                text = if (currentStepIndex == steps.lastIndex) "Get Started (शुरू करें)" else "Next (आगे)",
                onClick = {
                    if (currentStepIndex < steps.lastIndex) {
                        currentStepIndex++
                    } else {
                        completeOnboarding()
                    }
                },
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}
