package dev.dhruva.jansevakendra.feature.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.dhruva.jansevakendra.core.model.*
import dev.dhruva.jansevakendra.designsystem.*
import dev.dhruva.jansevakendra.navigation.LocalNavigator
import dev.dhruva.jansevakendra.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel { HomeViewModel() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Animated Header Banner
            HomeHeaderSection(
                profile = uiState.profile,
                language = uiState.selectedLanguage,
                onLanguageClick = { navigator.navigateTo(Screen.LanguageSelection) }
            )

            // Search Bar
            HomeSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                placeholder = "Search 500+ Services (Aadhaar, PAN, प्रमाण पत्र...)"
            )

            // Main Content Area based on Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    HomeLoadingSkeleton()
                } else {
                    when (uiState.selectedTab) {
                        HomeTab.SERVICES -> {
                            HomeServicesTabContent(
                                uiState = uiState,
                                onCategorySelect = viewModel::onCategorySelected,
                                onServiceClick = viewModel::onServiceClicked,
                                onApplicationClick = viewModel::onApplicationClicked
                            )
                        }
                        HomeTab.APPLICATIONS -> {
                            HomeApplicationsTabContent(
                                applications = uiState.activeApplications,
                                onApplicationClick = viewModel::onApplicationClicked
                            )
                        }
                        HomeTab.SCHEMES -> {
                            HomeSchemesTabContent(
                                schemes = uiState.featuredSchemes
                            )
                        }
                        HomeTab.PROFILE -> {
                            HomeProfileTabContent(
                                profile = uiState.profile,
                                language = uiState.selectedLanguage,
                                onLogout = { navigator.replaceWith(Screen.Auth) }
                            )
                        }
                    }
                }
            }

            // Floating Civic Navigation Bar
            HomeBottomNavigationBar(
                selectedTab = uiState.selectedTab,
                onTabSelect = viewModel::onTabSelected
            )
        }
    }
}

/**
 * Top App Bar with Citizen Greeting and Live Notification indicator.
 */
@Composable
private fun HomeHeaderSection(
    profile: CitizenProfile?,
    language: AppLanguage,
    onLanguageClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            JskEmblem(size = 44.dp, tint = JskColors.SaffronPrimary)
            Spacer(Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${language.greeting}, ${profile?.name?.split(" ")?.firstOrNull() ?: "Citizen"}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(JskColors.ForestGreen)
                    )
                }
                Text(
                    text = "📍 ${profile?.district ?: "Lucknow"}, ${profile?.state ?: "UP"}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Language Pill Toggle
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .bouncyClickable(onClick = onLanguageClick)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "🌐 ${language.nativeName}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = JskColors.SaffronPrimary
                )
            )
        }
    }
}

/**
 * Modern Search bar with morphing query and icon.
 */
@Composable
private fun HomeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        JskTextField(
            value = query,
            onValueChange = onQueryChange,
            label = "",
            placeholder = placeholder,
            leadingIcon = {
                Text(text = "🔍", fontSize = 16.sp)
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            .bouncyClickable(onClick = { onQueryChange("") }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✕", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            } else null
        )
    }
}

/**
 * Main Services Tab Content with Application Tracker, Categories, and Popular Services.
 */
@Composable
private fun HomeServicesTabContent(
    uiState: HomeUiState,
    onCategorySelect: (String?) -> Unit,
    onServiceClick: (CitizenService) -> Unit,
    onApplicationClick: (ApplicationStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        // High-Priority Application Tracker (if active applications exist)
        if (uiState.activeApplications.isNotEmpty()) {
            item {
                ActiveApplicationTrackerCard(
                    application = uiState.activeApplications.first(),
                    onClick = { onApplicationClick(uiState.activeApplications.first()) }
                )
            }
        }

        // Section Title: Categories
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Citizen Service Hub (सेवाएं)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                if (uiState.selectedCategoryId != null) {
                    TextButton(onClick = { onCategorySelect(null) }) {
                        Text(
                            text = "Show All (सभी)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = JskColors.SaffronPrimary
                            )
                        )
                    }
                }
            }
        }

        // Horizontal Category Filter Pills
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    CategoryPill(
                        title = "All (सभी)",
                        count = uiState.allServices.size,
                        isSelected = uiState.selectedCategoryId == null,
                        onClick = { onCategorySelect(null) }
                    )
                }
                items(uiState.categories) { cat ->
                    CategoryPill(
                        title = "${cat.nameHindi} • ${cat.name}",
                        count = cat.serviceCount,
                        isSelected = uiState.selectedCategoryId == cat.id,
                        onClick = { onCategorySelect(cat.id) }
                    )
                }
            }
        }

        // Section Title: Services
        item {
            Text(
                text = if (uiState.selectedCategoryId != null) "Filtered Services" else "Popular Citizen Services (लोकप्रिय सेवाएं)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)
            )
        }

        // Staggered Service Cards
        itemsIndexed(uiState.filteredServices) { index, service ->
            StaggeredServiceCard(
                service = service,
                index = index,
                onClick = { onServiceClick(service) }
            )
        }
    }
}

/**
 * Animated Active Application Tracker Card.
 */
@Composable
private fun ActiveApplicationTrackerCard(
    application: ApplicationStatus,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TrackerPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TrackerDot"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .shadow(6.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            )
            .border(1.2.dp, JskColors.SaffronLight.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
            .bouncyClickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(JskColors.ForestGreen.copy(alpha = pulseAlpha))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "ACTIVE TRACKING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = JskColors.SaffronPrimary,
                            letterSpacing = 1.sp
                        )
                    )
                }

                JskStatusChip(stage = application.currentStage)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = application.serviceTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Application No: ${application.applicationNumber}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(Modifier.height(14.dp))

            // Multi-Stage Progress Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val stages = listOf("Submitted", "Verification", "Field Check", "Approved")
                stages.forEachIndexed { i, title ->
                    val isDone = i <= 1 // In this mock 2 steps are done
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isDone) JskColors.ForestGreen
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                    )
                    if (i < stages.lastIndex) {
                        Spacer(Modifier.width(4.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Submitted: ${application.submissionDate}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = "Est. Ready: ${application.estimatedCompletionDate}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = JskColors.ForestGreen
                    )
                )
            }
        }
    }
}

/**
 * Interactive filter pill.
 */
@Composable
private fun CategoryPill(
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) JskColors.NavyPrimary else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "PillBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(200),
        label = "PillText"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isSelected) JskColors.NavyPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                RoundedCornerShape(14.dp)
            )
            .bouncyClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            )
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                )
            }
        }
    }
}

/**
 * Service card with staggered entrance.
 */
@Composable
private fun StaggeredServiceCard(
    service: CitizenService,
    index: Int,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 35L)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(300)) + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .shadow(2.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .bouncyClickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = service.titleHindi,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = JskColors.SaffronPrimary
                            )
                        )
                        Text(
                            text = service.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    // Fee badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(JskColors.GreenContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (service.feeRupees == 0) "FREE" else "₹${service.feeRupees}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = JskColors.ForestGreen
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(12.dp))

                // Bottom feature badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⏱ ${if (service.processingDays == 0) "Instant" else "${service.processingDays} Days"}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "📄 ${service.requiredDocuments.size} Docs required",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Text(
                        text = "Apply →",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

/**
 * Applications Tracking Tab.
 */
@Composable
private fun HomeApplicationsTabContent(
    applications: List<ApplicationStatus>,
    onApplicationClick: (ApplicationStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Your Submitted Applications (आपके आवेदन)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        items(applications) { app ->
            ActiveApplicationTrackerCard(
                application = app,
                onClick = { onApplicationClick(app) }
            )
        }
    }
}

/**
 * Welfare Schemes Tab.
 */
@Composable
private fun HomeSchemesTabContent(
    schemes: List<WelfareScheme>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Central & State Welfare Schemes (सरकारी योजनाएं)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        items(schemes) { scheme ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(scheme.bannerGradientHexStart),
                                Color(scheme.bannerGradientHexEnd)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = scheme.tag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Text(
                            text = "Ends: ${scheme.applicationDeadline}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = scheme.titleHindi,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = scheme.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Benefit: ${scheme.benefitDescription}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFD54F)
                        )
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Eligibility: ${scheme.eligibilitySummary}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Profile Tab with citizen details and logout.
 */
@Composable
private fun HomeProfileTabContent(
    profile: CitizenProfile?,
    language: AppLanguage,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(JskColors.SaffronContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile?.name?.firstOrNull()?.toString() ?: "U",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = JskColors.OnSaffronContainer
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = profile?.name ?: "Rajesh Kumar Sharma",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Text(
                    text = "🇮🇳 +91 ${profile?.mobileNumber ?: "9876543210"}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(JskColors.GreenContainer.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "✓ Aadhaar e-KYC Verified", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = JskColors.ForestGreen))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        JskSecondaryButton(
            text = "Log Out (लॉग आउट)",
            onClick = onLogout
        )
    }
}

/**
 * Shimmer Loading Skeleton.
 */
@Composable
private fun HomeLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        JskShimmerCard(height = 140.dp)
        JskShimmerCard(height = 50.dp)
        JskShimmerCard(height = 90.dp)
        JskShimmerCard(height = 90.dp)
        JskShimmerCard(height = 90.dp)
    }
}

/**
 * Floating Civic Bottom Navigation Bar.
 */
@Composable
private fun HomeBottomNavigationBar(
    selectedTab: HomeTab,
    onTabSelect: (HomeTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 1f,
                        animationSpec = MotionTokens.BouncySpring,
                        label = "TabScale"
                    )

                    Box(
                        modifier = Modifier
                            .scale(tabScale)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) JskColors.SaffronContainer.copy(alpha = 0.5f)
                                else Color.Transparent
                            )
                            .bouncyClickable(onClick = { onTabSelect(tab) })
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = when (tab) {
                                    HomeTab.SERVICES -> "🏛️"
                                    HomeTab.APPLICATIONS -> "📋"
                                    HomeTab.SCHEMES -> "📜"
                                    HomeTab.PROFILE -> "👤"
                                },
                                fontSize = 18.sp
                            )
                            Text(
                                text = tab.titleHindi,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) JskColors.SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
