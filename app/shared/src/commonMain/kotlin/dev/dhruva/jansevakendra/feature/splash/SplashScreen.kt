package dev.dhruva.jansevakendra.feature.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.dhruva.jansevakendra.designsystem.JskColors
import dev.dhruva.jansevakendra.designsystem.JskEmblem
import dev.dhruva.jansevakendra.navigation.LocalNavigator
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = viewModel { SplashViewModel() }
) {
    val navigator = LocalNavigator.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SplashNavigationEvent.NavigateTo -> navigator.replaceWith(event.screen)
            }
        }
    }

    // Breathing pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "SplashBreathing")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "EmblemPulse"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    // Staggered entrance animations
    val contentAlpha = remember { Animatable(0f) }
    val contentSlide = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(Unit) {
        contentSlide.animateTo(
            targetValue = 0f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Radiant Glow Circle
        Box(
            modifier = Modifier
                .size(260.dp)
                .scale(scalePulse * 1.1f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            JskColors.SaffronLight.copy(alpha = glowAlpha * 0.45f),
                            JskColors.NavyLight.copy(alpha = glowAlpha * 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = contentSlide.value.dp)
                .alpha(contentAlpha.value)
        ) {
            // Emblem with pulse scale
            Box(
                modifier = Modifier
                    .scale(scalePulse)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                JskEmblem(
                    size = 100.dp,
                    tint = JskColors.SaffronPrimary
                )
            }

            Spacer(Modifier.height(20.dp))

            // Hindi branding
            Text(
                text = "जन सेवा केंद्र",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = JskColors.SaffronPrimary
                )
            )

            Spacer(Modifier.height(6.dp))

            // English branding
            Text(
                text = "JAN SEVA KENDRA",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.5.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            )

            Spacer(Modifier.height(10.dp))

            // Civic Tagline
            Text(
                text = "Government to Citizen Digital Portal",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Bottom Republic Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(contentAlpha.value)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(JskColors.ForestGreen)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Digital India Initiative",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
