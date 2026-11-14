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
