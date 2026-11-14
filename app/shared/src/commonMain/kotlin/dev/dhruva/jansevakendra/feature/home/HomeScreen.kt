package dev.dhruva.jansevakendra.feature.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
            // Civic App Bar Header with Logo & Language Toggle
            HomeHeaderSection(
                profile = uiState.profile,
                language = uiState.selectedLanguage,
                onLanguageClick = { navigator.navigateTo(Screen.LanguageSelection) }
            )

            // Dynamic Content Area based on Selected Bottom Nav Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    HomeLoadingSkeleton()
                } else {
                    AnimatedContent(
                        targetState = uiState.selectedTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                        },
                        label = "TabContentAnimation"
                    ) { currentTab ->
                        when (currentTab) {
                            HomeTab.HOME -> {
                                HomeDashboardContent(
                                    uiState = uiState,
                                    onSearchChange = viewModel::onSearchQueryChanged,
                                    onOpenServices = { catId -> viewModel.openServicesWithCategory(catId) },
                                    onServiceClick = viewModel::onServiceClicked,
                                    onApplicationClick = viewModel::onApplicationClicked
                                )
                            }
                            HomeTab.SERVICES -> {
                                AllServicesDirectoryContent(
                                    uiState = uiState,
                                    onSearchChange = viewModel::onSearchQueryChanged,
                                    onCategorySelect = viewModel::onCategorySelected,
                                    onToggleCategory = viewModel::toggleCategoryExpanded,
                                    onServiceClick = viewModel::onServiceClicked
                                )
                            }
                            HomeTab.APPLICATIONS -> {
                                HomeApplicationsTabContent(
                                    applications = uiState.filteredApplications,
                                    searchQuery = uiState.applicationSearchQuery,
                                    onSearchChange = viewModel::onApplicationSearchChanged,
                                    onApplicationClick = viewModel::onApplicationClicked
                                )
                            }
                            HomeTab.SCHEMES -> {
                                HomeSchemesTabContent(
                                    schemes = uiState.featuredSchemes,
                                    onApplyScheme = { scheme ->
                                        // Open dialog for scheme application
                                        val mockService = CitizenService(
                                            id = scheme.id,
                                            title = scheme.title,
                                            titleHindi = scheme.titleHindi,
                                            categoryId = "cat_schemes",
                                            description = scheme.benefitDescription,
                                            processingDays = 7,
                                            feeRupees = 0,
                                            requiredDocuments = listOf("Aadhaar Card", "Bank Account Details", "Income Certificate"),
                                            departmentName = scheme.department
                                        )
                                        viewModel.onServiceClicked(mockService)
                                    }
                                )
                            }
                            HomeTab.PROFILE -> {
                                HomeProfileTabContent(
                                    profile = uiState.profile,
                                    language = uiState.selectedLanguage,
                                    onChangeLanguage = { navigator.navigateTo(Screen.LanguageSelection) },
                                    onLogout = { navigator.replaceWith(Screen.Auth) }
                                )
                            }
                        }
                    }
                }
            }

            // Modern Civic 5-Tab Floating Bottom Navigation Bar
            HomeBottomNavigationBar(
                selectedTab = uiState.selectedTab,
                onTabSelect = viewModel::onTabSelected
            )
        }

        // Service Details / Application Bottom Sheet Dialog
        uiState.selectedServiceForApplication?.let { service ->
            ServiceApplicationDialog(
                service = service,
                submittedAppId = uiState.submittedApplicationId,
                onDismiss = viewModel::onDismissServiceDialog,
                onConfirm = { viewModel.onConfirmApplicationSubmission(service) }
            )
        }
    }
}

/**
 * Top App Bar with Citizen Greeting and Official Kendra Branding.
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            JskLogo(size = 46.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${language.greeting}, ${profile?.name?.split(" ")?.firstOrNull() ?: "Dhruva"}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(JskColors.ForestGreen)
                    )
                }
                Text(
                    text = "📍 ${profile?.district ?: "Beed"}, ${profile?.state ?: "Maharashtra"}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Language Toggle Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .bouncyClickable(onClick = onLanguageClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🌐", fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = language.nativeName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = JskColors.SaffronPrimary
                        )
                    )
                }
            }
        }
    }
}

/**
 * TAB 1: Home Dashboard Content (Quick Overview, Tracker, 28-Departments Gateway, Top Services).
 */
@Composable
private fun HomeDashboardContent(
    uiState: HomeUiState,
    onSearchChange: (String) -> Unit,
    onOpenServices: (String?) -> Unit,
    onServiceClick: (CitizenService) -> Unit,
    onApplicationClick: (ApplicationStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Search Bar with Department & Service auto-suggestions
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                JskTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    label = "",
                    placeholder = "Search 28 Departments & 100+ Services...",
                    leadingIcon = {
                        Text(text = "🔍", fontSize = 16.sp)
                    },
                    trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                        {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    .bouncyClickable(onClick = { onSearchChange("") }),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "✕", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    } else null
                )
            }
        }

        // Active Application Tracker (Spotlights Form 3 Learner's Licence or Aadhaar Update)
        if (uiState.activeApplications.isNotEmpty()) {
            item {
                ActiveApplicationTrackerCard(
                    application = uiState.activeApplications.first(),
                    onClick = { onApplicationClick(uiState.activeApplications.first()) }
                )
            }
        }

        // 28 Core Departments Gateway Banner (Prominently directs user to the new Services section!)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .shadow(3.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                JskColors.NavyPrimary,
                                JskColors.NavyLight
                            )
                        )
                    )
                    .border(1.dp, Color(0xFF334E68), RoundedCornerShape(22.dp))
                    .bouncyClickable(onClick = { onOpenServices(null) })
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
                                .background(JskColors.SaffronPrimary)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "28 DEPARTMENTS • सभी २८ विभाग",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        Text(
                            text = "Explore All →",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "नागरिक सेवा केंद्र निर्देशिका",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Transport RTO (Licences), Revenue, Aadhaar, PAN, Agriculture, Ration, Pensions, Electricity & 20+ more departments.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // Quick Category Shortcuts (Top 8 Core Departments)
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Citizen Departments (प्रमुख विभाग)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    TextButton(onClick = { onOpenServices(null) }) {
                        Text(
                            text = "View 28 All →",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = JskColors.SaffronPrimary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // 4x2 Quick Department Badges
                val quickCats = uiState.categories.take(8)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    quickCats.chunked(4).forEach { rowCats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowCats.forEach { cat ->
                                QuickDepartmentBadge(
                                    category = cat,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onOpenServices(cat.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Popular Services
        item {
            Text(
                text = "Popular Citizen Services (लोकप्रिय सेवाएं)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
            )
        }

        // Staggered Service Cards
        itemsIndexed(uiState.filteredServices.take(8)) { index, service ->
            ModernServiceCard(
                service = service,
                index = index,
                onClick = { onServiceClick(service) }
            )
        }
    }
}

/**
 * TAB 2: Full 28 Main Departments + Sub-Services Directory Content.
 */
@Composable
private fun AllServicesDirectoryContent(
    uiState: HomeUiState,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onToggleCategory: (String) -> Unit,
    onServiceClick: (CitizenService) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Sticky Header Title
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "नागरिक सेवाएं (All 28 Departments)",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Complete catalog of 28 Government Departments and sub-services.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Live Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                JskTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    label = "",
                    placeholder = "Search by service, department or form number...",
                    leadingIcon = { Text("🔍", fontSize = 16.sp) },
                    trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                        {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    .bouncyClickable(onClick = { onSearchChange("") }),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✕", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    } else null
                )
            }
        }

        // Horizontal Category Chips for quick jump
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryPill(
                        title = "All 28 Departments",
                        count = uiState.allServices.size,
                        isSelected = uiState.selectedCategoryId == null,
                        onClick = { onCategorySelect(null) }
                    )
                }
                items(uiState.categories) { cat ->
                    CategoryPill(
                        title = "#${cat.departmentNumber.toString().padStart(2, '0')} ${cat.nameHindi}",
                        count = cat.serviceCount,
                        isSelected = uiState.selectedCategoryId == cat.id,
                        onClick = { onCategorySelect(cat.id) }
                    )
                }
            }
        }

        // Department-by-Department Hierarchical Directory
        val displayedCategories = if (uiState.selectedCategoryId != null) {
            uiState.categories.filter { it.id == uiState.selectedCategoryId }
        } else {
            uiState.categories
        }

        items(displayedCategories) { category ->
            val isExpanded = (uiState.expandedCategoryId == category.id) || (uiState.selectedCategoryId != null) || uiState.searchQuery.isNotEmpty()
            val categoryServices = uiState.filteredServices.filter { it.categoryId == category.id }

            DepartmentAccordionCard(
                category = category,
                services = categoryServices,
                isExpanded = isExpanded,
                onToggleExpand = { onToggleCategory(category.id) },
                onServiceClick = onServiceClick
            )
        }
    }
}

/**
 * Department Card with Expandable Sub-Services.
 */
@Composable
private fun DepartmentAccordionCard(
    category: ServiceCategory,
    services: List<CitizenService>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onServiceClick: (CitizenService) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .shadow(2.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.5.dp,
                if (isExpanded) Color(category.highlightColorHex) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(18.dp)
            )
    ) {
        Column {
            // Header Row of Department
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .bouncyClickable(onClick = onToggleExpand)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Department Number Badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(category.highlightColorHex).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.departmentNumber.toString().padStart(2, '0'),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(category.highlightColorHex)
                            )
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = category.nameHindi,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${category.serviceCount} सेवाएं",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = if (isExpanded) "▲" else "▼",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded Sub-Services List
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (services.isEmpty()) {
                        Text(
                            text = "No matching services found in this department for your search.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        services.forEach { service ->
                            SubServiceItemCard(
                                service = service,
                                onClick = { onServiceClick(service) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Sub-Service row inside an expanded Department.
 */
@Composable
private fun SubServiceItemCard(
    service: CitizenService,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .bouncyClickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = service.titleHindi,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = JskColors.SaffronPrimary
                            )
                        )
                        service.formNumber?.let { form ->
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(JskColors.NavyContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = form,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JskColors.OnNavyContainer
                                    )
                                )
                            }
                        }
                    }

                    Text(
                        text = service.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // Fee pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(JskColors.GreenContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (service.feeRupees == 0) "FREE" else "₹${service.feeRupees}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = JskColors.ForestGreen
                        )
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = service.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

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
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "📄 ${service.requiredDocuments.size} Docs",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Text(
                    text = "Apply Now →",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

/**
 * Interactive Service Application Dialog / Sheet.
 */
@Composable
private fun ServiceApplicationDialog(
    service: CitizenService,
    submittedAppId: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Column {
                if (submittedAppId != null) {
                    // Success View
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(JskColors.GreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✓", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = JskColors.ForestGreen)
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "आवेदन सफलतापूर्वक दर्ज हुआ!",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Application Number:",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Text(
                            text = submittedAppId,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = JskColors.SaffronPrimary
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Visit Udyam Jan Sewa Kendra or track status in 'आवेदन' tab.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(Modifier.height(20.dp))

                        JskPrimaryButton(
                            text = "Done (पूर्ण)",
                            onClick = onDismiss
                        )
                    }
                } else {
                    // Application Details & Required Docs View
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(JskColors.SaffronContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = service.departmentName ?: "Jan Seva Kendra",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = JskColors.OnSaffronContainer
                                )
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Text(text = "✕", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = service.titleHindi,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Text(
                        text = service.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // Fee & SLA row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Government Fee", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Text(
                                text = if (service.feeRupees == 0) "FREE" else "₹${service.feeRupees}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = JskColors.ForestGreen)
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Turnaround (SLA)", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Text(
                                text = "${service.processingDays} Working Days",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = JskColors.NavyPrimary)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "आवश्यक दस्तावेज (Required Documents):",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(Modifier.height(6.dp))

                    service.requiredDocuments.forEach { doc ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "✓", color = JskColors.ForestGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = doc,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    JskPrimaryButton(
                        text = "Apply Now (आवेदन करें)",
                        onClick = onConfirm
                    )
                }
            }
        }
    }
}

/**
 * 4-in-a-row Quick Department Badge for Home Dashboard.
 */
@Composable
private fun QuickDepartmentBadge(
    category: ServiceCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .bouncyClickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(category.highlightColorHex).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.departmentCode.take(3),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(category.highlightColorHex)
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = category.nameHindi.split(" ").firstOrNull() ?: category.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
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
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(3.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
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

            Spacer(Modifier.height(10.dp))

            Text(
                text = application.serviceTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "Application No: ${application.applicationNumber} • ${application.applicantName}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(Modifier.height(12.dp))

            // Multi-Stage Visual Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val stages = listOf("Submitted", "Verification", "Field Check", "Approved")
                stages.forEachIndexed { i, title ->
                    val isDone = when (application.currentStage) {
                        ApplicationStage.SUBMITTED -> i == 0
                        ApplicationStage.DOCUMENTS_VERIFYING -> i <= 1
                        ApplicationStage.FIELD_VERIFICATION -> i <= 2
                        ApplicationStage.APPROVED -> true
                        ApplicationStage.REJECTED -> i <= 1
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isDone) JskColors.ForestGreen
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                    text = "Est: ${application.estimatedCompletionDate}",
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
 * Category Pill Filter.
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
 * Modern Service Card.
 */
@Composable
private fun ModernServiceCard(
    service: CitizenService,
    index: Int,
    onClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 25L)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(250)) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = service.titleHindi,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = JskColors.SaffronPrimary
                                )
                            )
                            service.formNumber?.let { form ->
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(JskColors.NavyContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = form,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JskColors.OnNavyContainer
                                        )
                                    )
                                }
                            }
                        }

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
                            .background(JskColors.GreenContainer.copy(alpha = 0.6f))
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
                        text = "Apply Now →",
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
 * TAB 3: Applications Tracking Tab.
 */
@Composable
private fun HomeApplicationsTabContent(
    applications: List<ApplicationStatus>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onApplicationClick: (ApplicationStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "आवेदन स्थिति ट्रैकिंग (Application Status)",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Check the live progress of your government applications.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        item {
            JskTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                label = "",
                placeholder = "Search by Application Number (e.g. 5104819326)...",
                leadingIcon = { Text("🔍", fontSize = 16.sp) }
            )
        }

        if (applications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No applications match your search query.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        } else {
            items(applications) { app ->
                ActiveApplicationTrackerCard(
                    application = app,
                    onClick = { onApplicationClick(app) }
                )
            }
        }
    }
}

/**
 * TAB 4: Welfare Schemes Tab.
 */
@Composable
private fun HomeSchemesTabContent(
    schemes: List<WelfareScheme>,
    onApplyScheme: (WelfareScheme) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "कल्याणकारी योजनाएं (Government Welfare Schemes)",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Direct Benefit Transfer (DBT) and subsidy schemes for citizens.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
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
                    .bouncyClickable(onClick = { onApplyScheme(scheme) })
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
                            text = "Deadline: ${scheme.applicationDeadline}",
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
 * TAB 5: Profile & Kendra Information Tab.
 */
@Composable
private fun HomeProfileTabContent(
    profile: CitizenProfile?,
    language: AppLanguage,
    onChangeLanguage: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Citizen Profile Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(3.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(JskColors.SaffronContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile?.name?.firstOrNull()?.toString() ?: "D",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = JskColors.OnSaffronContainer
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = profile?.name ?: "Dhruva Rajendra Khatavkar",
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

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(JskColors.GreenContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓ Aadhaar e-KYC Verified",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = JskColors.ForestGreen)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Kendra & VLE Operator Information Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    JskLogo(size = 36.dp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Udyam Jan Sewa Kendra (उद्यम जन सेवा केंद्र)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = JskColors.NavyPrimary)
                        )
                        Text(
                            text = "जन कल्याण हमारी पहचान • Center ID: CSC-MH23-8821",
                            style = MaterialTheme.typography.labelSmall.copy(color = JskColors.SaffronPrimary, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Operator / VLE: Dhruva Khatavkar\nAddress: Dhanora Road, Sant Namdev Nagar, Beed, Maharashtra 431122\nCitizen Toll-Free Helpline: 1800-233-0000",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Actions
        JskSecondaryButton(
            text = "Change App Language (भाषा बदलें: ${language.nativeName})",
            onClick = onChangeLanguage
        )

        Spacer(Modifier.height(10.dp))

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
 * Vector Canvas Icons for Civic Bottom Navigation Bar.
 */
@Composable
private fun CivicNavIcon(
    tab: HomeTab,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        when (tab) {
            HomeTab.HOME -> {
                // House with Ashok pillar roofline
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.12f)
                    lineTo(w * 0.9f, h * 0.45f)
                    lineTo(w * 0.78f, h * 0.45f)
                    lineTo(w * 0.78f, h * 0.88f)
                    lineTo(w * 0.22f, h * 0.88f)
                    lineTo(w * 0.22f, h * 0.45f)
                    lineTo(w * 0.1f, h * 0.45f)
                    close()
                }
                drawPath(path, color = tint, style = Stroke(width = w * 0.08f))
                // Door
                drawRect(
                    color = tint,
                    topLeft = Offset(w * 0.4f, h * 0.58f),
                    size = Size(w * 0.2f, h * 0.3f)
                )
            }
            HomeTab.SERVICES -> {
                // 2x2 Department Grid Matrix
                val pad = w * 0.1f
                val boxW = w * 0.32f
                val radius = CornerRadius(w * 0.08f)
                // Top-Left
                drawRoundRect(color = tint, topLeft = Offset(pad, pad), size = Size(boxW, boxW), cornerRadius = radius)
                // Top-Right
                drawRoundRect(color = tint, topLeft = Offset(w - pad - boxW, pad), size = Size(boxW, boxW), cornerRadius = radius)
                // Bottom-Left
                drawRoundRect(color = tint, topLeft = Offset(pad, h - pad - boxW), size = Size(boxW, boxW), cornerRadius = radius)
                // Bottom-Right
                drawRoundRect(color = tint, topLeft = Offset(w - pad - boxW, h - pad - boxW), size = Size(boxW, boxW), cornerRadius = radius)
            }
            HomeTab.APPLICATIONS -> {
                // Clipboard with verification checkmark
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.15f, h * 0.18f),
                    size = Size(w * 0.7f, h * 0.75f),
                    cornerRadius = CornerRadius(w * 0.08f),
                    style = Stroke(width = w * 0.08f)
                )
                // Top clip
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.32f, h * 0.08f),
                    size = Size(w * 0.36f, h * 0.14f),
                    cornerRadius = CornerRadius(w * 0.04f)
                )
                // Checkmark
                val checkPath = Path().apply {
                    moveTo(w * 0.32f, h * 0.54f)
                    lineTo(w * 0.45f, h * 0.68f)
                    lineTo(w * 0.70f, h * 0.42f)
                }
                drawPath(checkPath, color = tint, style = Stroke(width = w * 0.09f))
            }
            HomeTab.SCHEMES -> {
                // Government seal / medal star
                val cx = w * 0.5f
                val cy = h * 0.42f
                val r = w * 0.35f
                drawCircle(color = tint, radius = r, center = Offset(cx, cy), style = Stroke(width = w * 0.08f))
                drawCircle(color = tint, radius = r * 0.45f, center = Offset(cx, cy))
                // Ribbons
                val ribbon = Path().apply {
                    moveTo(w * 0.35f, cy + r * 0.7f)
                    lineTo(w * 0.25f, h * 0.95f)
                    lineTo(w * 0.42f, h * 0.85f)
                    lineTo(w * 0.5f, h * 0.95f)
                    lineTo(w * 0.58f, h * 0.85f)
                    lineTo(w * 0.75f, h * 0.95f)
                    lineTo(w * 0.65f, cy + r * 0.7f)
                }
                drawPath(ribbon, color = tint, style = Stroke(width = w * 0.07f))
            }
            HomeTab.PROFILE -> {
                // Citizen Avatar
                val cx = w * 0.5f
                // Head
                drawCircle(color = tint, radius = w * 0.22f, center = Offset(cx, h * 0.32f))
                // Shoulders arc
                val shoulders = Path().apply {
                    moveTo(w * 0.12f, h * 0.92f)
                    cubicTo(w * 0.18f, h * 0.62f, w * 0.82f, h * 0.62f, w * 0.88f, h * 0.92f)
                }
                drawPath(shoulders, color = tint, style = Stroke(width = w * 0.09f))
            }
        }
    }
}

/**
 * Modern 5-Tab Floating Civic Bottom Navigation Bar.
 */
@Composable
private fun HomeBottomNavigationBar(
    selectedTab: HomeTab,
    onTabSelect: (HomeTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(12.dp, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
                .padding(horizontal = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1f,
                        animationSpec = MotionTokens.BouncySpring,
                        label = "TabScale"
                    )

                    val tabTint = if (isSelected) JskColors.SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

                    Box(
                        modifier = Modifier
                            .scale(tabScale)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSelected) JskColors.SaffronContainer.copy(alpha = 0.6f)
                                else Color.Transparent
                            )
                            .bouncyClickable(onClick = { onTabSelect(tab) })
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CivicNavIcon(
                                tab = tab,
                                tint = tabTint,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = tab.titleHindi,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = tabTint,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
