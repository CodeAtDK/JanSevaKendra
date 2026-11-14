package dev.dhruva.jansevakendra.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.dhruva.jansevakendra.core.model.AppLanguage
import dev.dhruva.jansevakendra.designsystem.*
import dev.dhruva.jansevakendra.navigation.LocalNavigator
import dev.dhruva.jansevakendra.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LanguageSelectionScreen(
    viewModel: LanguageViewModel = viewModel { LanguageViewModel() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                LanguageNavigationEvent.NavigateToOnboarding -> navigator.navigateTo(Screen.Onboarding)
            }
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
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Spacer(Modifier.height(32.dp))

            // Emblem Accent Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                JskEmblem(size = 42.dp, tint = JskColors.SaffronPrimary)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "अपनी भाषा चुनें",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = JskColors.SaffronPrimary
                        )
                    )
                    Text(
                        text = "Choose Your Language",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Jan Seva Kendra provides citizen services in your preferred regional language. You can change this anytime from settings.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(Modifier.height(28.dp))

            // Staggered 2-column language card grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(uiState.availableLanguages) { index, lang ->
                    StaggeredLanguageCard(
                        language = lang,
                        isSelected = uiState.selectedLanguage == lang,
                        index = index,
                        onClick = { viewModel.onLanguageSelected(lang) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action Button
            JskPrimaryButton(
                text = "${uiState.selectedLanguage.greeting} • Continue (आगे बढ़ें)",
                onClick = viewModel::onConfirm,
                isLoading = uiState.isSaving,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun StaggeredLanguageCard(
    language: AppLanguage,
    isSelected: Boolean,
    index: Int,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 60L)
        isVisible = true
    }

    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = MotionTokens.BouncySpring,
        label = "LanguageCardScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) JskColors.SaffronPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        animationSpec = tween(250),
        label = "BorderColor"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) JskColors.SaffronContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
        animationSpec = tween(250),
        label = "BgColor"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(350)) + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .scale(cardScale)
                .shadow(if (isSelected) 8.dp else 1.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(backgroundColor)
                .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(20.dp))
                .bouncyClickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = language.nativeName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) JskColors.SaffronPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    )

                    // Selection radio badge with pop scale
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) JskColors.SaffronPrimary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(1.5.dp, borderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = language.displayName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "• \"${language.greeting}\"",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) JskColors.SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}
