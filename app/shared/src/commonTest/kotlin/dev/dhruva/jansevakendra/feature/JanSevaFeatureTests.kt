package dev.dhruva.jansevakendra.feature

import dev.dhruva.jansevakendra.core.data.*
import dev.dhruva.jansevakendra.core.model.AppLanguage
import dev.dhruva.jansevakendra.core.usecase.*
import dev.dhruva.jansevakendra.feature.auth.AuthNavigationEvent
import dev.dhruva.jansevakendra.feature.auth.AuthViewModel
import dev.dhruva.jansevakendra.feature.home.HomeNavigationEvent
import dev.dhruva.jansevakendra.feature.home.HomeTab
import dev.dhruva.jansevakendra.feature.home.HomeViewModel
import dev.dhruva.jansevakendra.feature.onboarding.LanguageNavigationEvent
import dev.dhruva.jansevakendra.feature.onboarding.LanguageViewModel
import dev.dhruva.jansevakendra.feature.splash.SplashNavigationEvent
import dev.dhruva.jansevakendra.feature.splash.SplashViewModel
import dev.dhruva.jansevakendra.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class JanSevaFeatureTests {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test HomeViewModel loads categories and services successfully`() = runTest(testDispatcher) {
        val serviceRepo = FakeCitizenServiceRepository()
        val appRepo = FakeApplicationRepository()
        val schemeRepo = FakeWelfareSchemeRepository()
        val sessionRepo = InMemoryUserSessionRepository()

        val viewModel = HomeViewModel(
            getCategoriesUseCase = GetCategoriesUseCase(serviceRepo),
            getCitizenServicesUseCase = GetCitizenServicesUseCase(serviceRepo),
            searchCitizenServicesUseCase = SearchCitizenServicesUseCase(serviceRepo),
            getActiveApplicationsUseCase = GetActiveApplicationsUseCase(appRepo),
            getFeaturedSchemesUseCase = GetFeaturedSchemesUseCase(schemeRepo),
            manageSessionUseCase = ManageSessionUseCase(sessionRepo)
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.categories.isNotEmpty())
        assertTrue(state.allServices.isNotEmpty())
        assertTrue(state.activeApplications.isNotEmpty())
        assertTrue(state.featuredSchemes.isNotEmpty())
    }

    @Test
    fun `test HomeViewModel category selection filters services correctly`() = runTest(testDispatcher) {
        val serviceRepo = FakeCitizenServiceRepository()
        val appRepo = FakeApplicationRepository()
        val schemeRepo = FakeWelfareSchemeRepository()
        val sessionRepo = InMemoryUserSessionRepository()

        val viewModel = HomeViewModel(
            getCategoriesUseCase = GetCategoriesUseCase(serviceRepo),
            getCitizenServicesUseCase = GetCitizenServicesUseCase(serviceRepo),
            searchCitizenServicesUseCase = SearchCitizenServicesUseCase(serviceRepo),
            getActiveApplicationsUseCase = GetActiveApplicationsUseCase(appRepo),
            getFeaturedSchemesUseCase = GetFeaturedSchemesUseCase(schemeRepo),
            manageSessionUseCase = ManageSessionUseCase(sessionRepo)
        )

        advanceUntilIdle()

        // Select 'Identity & Aadhaar' category
        viewModel.onCategorySelected("cat_identity")
        val state = viewModel.uiState.value
        assertEquals("cat_identity", state.selectedCategoryId)
        assertTrue(state.filteredServices.all { it.categoryId == "cat_identity" })
    }

    @Test
    fun `test HomeViewModel search filtering matches services`() = runTest(testDispatcher) {
        val serviceRepo = FakeCitizenServiceRepository()
        val appRepo = FakeApplicationRepository()
        val schemeRepo = FakeWelfareSchemeRepository()
        val sessionRepo = InMemoryUserSessionRepository()

        val viewModel = HomeViewModel(
            getCategoriesUseCase = GetCategoriesUseCase(serviceRepo),
            getCitizenServicesUseCase = GetCitizenServicesUseCase(serviceRepo),
            searchCitizenServicesUseCase = SearchCitizenServicesUseCase(serviceRepo),
            getActiveApplicationsUseCase = GetActiveApplicationsUseCase(appRepo),
            getFeaturedSchemesUseCase = GetFeaturedSchemesUseCase(schemeRepo),
            manageSessionUseCase = ManageSessionUseCase(sessionRepo)
        )

        advanceUntilIdle()

        viewModel.onSearchQueryChanged("PAN")
        val state = viewModel.uiState.value
        assertTrue(state.filteredServices.any { it.title.contains("PAN") })
    }

    @Test
    fun `test HomeViewModel loads all 28 citizen departments`() = runTest(testDispatcher) {
        val serviceRepo = FakeCitizenServiceRepository()
        val appRepo = FakeApplicationRepository()
        val schemeRepo = FakeWelfareSchemeRepository()
        val sessionRepo = InMemoryUserSessionRepository()

        val viewModel = HomeViewModel(
            getCategoriesUseCase = GetCategoriesUseCase(serviceRepo),
            getCitizenServicesUseCase = GetCitizenServicesUseCase(serviceRepo),
            searchCitizenServicesUseCase = SearchCitizenServicesUseCase(serviceRepo),
            getActiveApplicationsUseCase = GetActiveApplicationsUseCase(appRepo),
            getFeaturedSchemesUseCase = GetFeaturedSchemesUseCase(schemeRepo),
            manageSessionUseCase = ManageSessionUseCase(sessionRepo)
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(28, state.categories.size, "Must have exactly 28 government departments")
        assertEquals(1, state.categories.first().departmentNumber)
        assertEquals("cat_transport", state.categories.first().id)
        assertEquals(28, state.categories.last().departmentNumber)
        assertEquals("cat_banking_dbt", state.categories.last().id)
    }

    @Test
    fun `test Transport department contains Learner Licence Form 3 from uploaded document`() = runTest(testDispatcher) {
        val serviceRepo = FakeCitizenServiceRepository()
        val appRepo = FakeApplicationRepository()
        val schemeRepo = FakeWelfareSchemeRepository()
        val sessionRepo = InMemoryUserSessionRepository()

        val viewModel = HomeViewModel(
            getCategoriesUseCase = GetCategoriesUseCase(serviceRepo),
            getCitizenServicesUseCase = GetCitizenServicesUseCase(serviceRepo),
            searchCitizenServicesUseCase = SearchCitizenServicesUseCase(serviceRepo),
            getActiveApplicationsUseCase = GetActiveApplicationsUseCase(appRepo),
            getFeaturedSchemesUseCase = GetFeaturedSchemesUseCase(schemeRepo),
            manageSessionUseCase = ManageSessionUseCase(sessionRepo)
        )

        advanceUntilIdle()

        // Filter by Transport category
        viewModel.onCategorySelected("cat_transport")
        val transportServices = viewModel.uiState.value.filteredServices
        assertTrue(transportServices.isNotEmpty())
        val learnerLicence = transportServices.firstOrNull { it.id == "srv_dl_learner" }
        assertNotNull(learnerLicence)
        assertEquals("Form 3", learnerLicence.formNumber)
        assertTrue(learnerLicence.title.contains("Learner's Driving Licence"))

        // Search for application number 5104819326 from uploaded PDF
        viewModel.onApplicationSearchChanged("5104819326")
        val matchingApps = viewModel.uiState.value.filteredApplications
        assertEquals(1, matchingApps.size)
        assertEquals("5104819326", matchingApps.first().applicationNumber)
        assertTrue(matchingApps.first().applicantName.contains("Dhruva"))
    }

    @Test
    fun `test HomeViewModel 5-tab navigation and direct category exploration`() = runTest(testDispatcher) {
        val serviceRepo = FakeCitizenServiceRepository()
        val appRepo = FakeApplicationRepository()
        val schemeRepo = FakeWelfareSchemeRepository()
        val sessionRepo = InMemoryUserSessionRepository()

        val viewModel = HomeViewModel(
            getCategoriesUseCase = GetCategoriesUseCase(serviceRepo),
            getCitizenServicesUseCase = GetCitizenServicesUseCase(serviceRepo),
            searchCitizenServicesUseCase = SearchCitizenServicesUseCase(serviceRepo),
            getActiveApplicationsUseCase = GetActiveApplicationsUseCase(appRepo),
            getFeaturedSchemesUseCase = GetFeaturedSchemesUseCase(schemeRepo),
            manageSessionUseCase = ManageSessionUseCase(sessionRepo)
        )

        advanceUntilIdle()

        // Initial tab is HOME
        assertEquals(HomeTab.HOME, viewModel.uiState.value.selectedTab)

        // Switch to SERVICES
        viewModel.onTabSelected(HomeTab.SERVICES)
        assertEquals(HomeTab.SERVICES, viewModel.uiState.value.selectedTab)

        // Open specific department directly
        viewModel.openServicesWithCategory("cat_revenue")
        assertEquals(HomeTab.SERVICES, viewModel.uiState.value.selectedTab)
        assertEquals("cat_revenue", viewModel.uiState.value.selectedCategoryId)
        assertEquals("cat_revenue", viewModel.uiState.value.expandedCategoryId)

        // Switch to APPLICATIONS
        viewModel.onTabSelected(HomeTab.APPLICATIONS)
        assertEquals(HomeTab.APPLICATIONS, viewModel.uiState.value.selectedTab)

        // Switch to SCHEMES
        viewModel.onTabSelected(HomeTab.SCHEMES)
        assertEquals(HomeTab.SCHEMES, viewModel.uiState.value.selectedTab)

        // Switch to PROFILE
        viewModel.onTabSelected(HomeTab.PROFILE)
        assertEquals(HomeTab.PROFILE, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `test AuthViewModel valid mobile requests OTP and sends event`() = runTest(testDispatcher) {
        val authRepo = FakeAuthRepository()
        val sessionRepo = InMemoryUserSessionRepository()

        val viewModel = AuthViewModel(
            sendOtpUseCase = SendOtpUseCase(authRepo),
            verifyOtpUseCase = VerifyOtpUseCase(authRepo, sessionRepo)
        )

        viewModel.onMobileNumberChanged("9876543210")
        assertEquals("9876543210", viewModel.uiState.value.mobileNumber)

        var capturedEvent: AuthNavigationEvent? = null
        val job = launch {
            capturedEvent = viewModel.events.first()
        }

        viewModel.requestOtp()
        advanceUntilIdle()

        assertIs<AuthNavigationEvent.NavigateToOtp>(capturedEvent)
        assertEquals("9876543210", (capturedEvent as AuthNavigationEvent.NavigateToOtp).mobileNumber)
        job.cancel()
    }

    @Test
    fun `test AuthViewModel invalid mobile displays error with shake`() = runTest(testDispatcher) {
        val authRepo = FakeAuthRepository()
        val sessionRepo = InMemoryUserSessionRepository()

        val viewModel = AuthViewModel(
            sendOtpUseCase = SendOtpUseCase(authRepo),
            verifyOtpUseCase = VerifyOtpUseCase(authRepo, sessionRepo)
        )

        viewModel.onMobileNumberChanged("12345") // Too short
        viewModel.requestOtp()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.errorShakeCounter > 0)
    }

    @Test
    fun `test AuthViewModel verify OTP success navigates to Home`() = runTest(testDispatcher) {
        val authRepo = FakeAuthRepository()
        val sessionRepo = InMemoryUserSessionRepository()

        val viewModel = AuthViewModel(
            sendOtpUseCase = SendOtpUseCase(authRepo),
            verifyOtpUseCase = VerifyOtpUseCase(authRepo, sessionRepo)
        )

        viewModel.onMobileNumberChanged("9876543210")
        viewModel.onOtpChanged("123456")

        var capturedEvent: AuthNavigationEvent? = null
        val job = launch {
            capturedEvent = viewModel.events.first()
        }

        advanceUntilIdle()

        assertEquals(AuthNavigationEvent.NavigateToHome, capturedEvent)
        assertNotNull(sessionRepo.getCurrentProfile())
        job.cancel()
    }

    @Test
    fun `test LanguageViewModel saves language and navigates`() = runTest(testDispatcher) {
        val sessionRepo = InMemoryUserSessionRepository()
        val viewModel = LanguageViewModel(manageSessionUseCase = ManageSessionUseCase(sessionRepo))

        advanceUntilIdle()
        viewModel.onLanguageSelected(AppLanguage.MARATHI)
        assertEquals(AppLanguage.MARATHI, viewModel.uiState.value.selectedLanguage)

        var capturedEvent: LanguageNavigationEvent? = null
        val job = launch {
            capturedEvent = viewModel.events.first()
        }

        viewModel.onConfirm()
        advanceUntilIdle()

        assertEquals(LanguageNavigationEvent.NavigateToOnboarding, capturedEvent)
        assertEquals(AppLanguage.MARATHI, sessionRepo.observeSelectedLanguage().first())
        job.cancel()
    }

    @Test
    fun `test SplashViewModel navigates to LanguageSelection for first-time launch`() = runTest(testDispatcher) {
        val sessionRepo = InMemoryUserSessionRepository()
        val viewModel = SplashViewModel(manageSessionUseCase = ManageSessionUseCase(sessionRepo))

        var capturedEvent: SplashNavigationEvent? = null
        val job = launch {
            capturedEvent = viewModel.events.first()
        }

        advanceUntilIdle()

        assertIs<SplashNavigationEvent.NavigateTo>(capturedEvent)
        assertEquals(Screen.LanguageSelection, (capturedEvent as SplashNavigationEvent.NavigateTo).screen)
        job.cancel()
    }
}
