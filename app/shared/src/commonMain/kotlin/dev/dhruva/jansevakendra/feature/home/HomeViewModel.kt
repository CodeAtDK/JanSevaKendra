package dev.dhruva.jansevakendra.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.dhruva.jansevakendra.core.di.CoreContainer
import dev.dhruva.jansevakendra.core.model.*
import dev.dhruva.jansevakendra.core.usecase.*
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class HomeTab(val titleHindi: String, val titleEnglish: String, val iconKey: String) {
    HOME("Home", "Home", "home"),
    SERVICES("Services", "Services", "grid"),
    APPLICATIONS("Tracking", "Tracking", "track"),
    SCHEMES("Schemes", "Schemes", "award"),
    PROFILE("Profile", "Profile", "user");

    val title: String get() = titleEnglish
}

data class HomeUiState(
    val profile: CitizenProfile? = null,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val selectedTab: HomeTab = HomeTab.HOME,
    val categories: List<ServiceCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val expandedCategoryId: String? = null,
    val allServices: List<CitizenService> = emptyList(),
    val filteredServices: List<CitizenService> = emptyList(),
    val activeApplications: List<ApplicationStatus> = emptyList(),
    val filteredApplications: List<ApplicationStatus> = emptyList(),
    val featuredSchemes: List<WelfareScheme> = emptyList(),
    val searchQuery: String = "",
    val applicationSearchQuery: String = "",
    val selectedServiceForApplication: CitizenService? = null,
    val submittedApplicationId: String? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

sealed interface HomeNavigationEvent {
    data class ShowToast(val message: String) : HomeNavigationEvent
    data class NavigateToServiceDetail(val service: CitizenService) : HomeNavigationEvent
    data class NavigateToApplicationDetail(val application: ApplicationStatus) : HomeNavigationEvent
}

class HomeViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase = CoreContainer.getCategoriesUseCase,
    private val getCitizenServicesUseCase: GetCitizenServicesUseCase = CoreContainer.getCitizenServicesUseCase,
    private val searchCitizenServicesUseCase: SearchCitizenServicesUseCase = CoreContainer.searchCitizenServicesUseCase,
    private val getActiveApplicationsUseCase: GetActiveApplicationsUseCase = CoreContainer.getActiveApplicationsUseCase,
    private val getFeaturedSchemesUseCase: GetFeaturedSchemesUseCase = CoreContainer.getFeaturedSchemesUseCase,
    private val manageSessionUseCase: ManageSessionUseCase = CoreContainer.manageSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeNavigationEvent>(replay = 0)
    val events: SharedFlow<HomeNavigationEvent> = _events.asSharedFlow()

    init {
        observeSession()
        loadInitialData()
    }

    private fun observeSession() {
        viewModelScope.launch {
            manageSessionUseCase.observeProfile().collect { profile ->
                _uiState.update { it.copy(profile = profile) }
            }
        }
        viewModelScope.launch {
            manageSessionUseCase.observeLanguage().collect { lang ->
                _uiState.update { it.copy(selectedLanguage = lang) }
            }
        }
        viewModelScope.launch {
            getActiveApplicationsUseCase.observe().collect { apps ->
                _uiState.update { state ->
                    state.copy(
                        activeApplications = apps,
                        filteredApplications = filterApplications(apps, state.applicationSearchQuery)
                    )
                }
            }
        }
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Concurrent execution using Coroutine async
            val categoriesDeferred = async { getCategoriesUseCase() }
            val servicesDeferred = async { getCitizenServicesUseCase() }
            val applicationsDeferred = async { getActiveApplicationsUseCase() }
            val schemesDeferred = async { getFeaturedSchemesUseCase() }

            val categoriesRes = categoriesDeferred.await()
            val servicesRes = servicesDeferred.await()
            val appsRes = applicationsDeferred.await()
            val schemesRes = schemesDeferred.await()

            val categories = categoriesRes.getOrDefault(emptyList())
            val services = servicesRes.getOrDefault(emptyList())
            val apps = appsRes.getOrDefault(emptyList())
            val schemes = schemesRes.getOrDefault(emptyList())

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    categories = categories,
                    allServices = services,
                    filteredServices = filterServices(services, state.selectedCategoryId, state.searchQuery),
                    activeApplications = apps,
                    filteredApplications = filterApplications(apps, state.applicationSearchQuery),
                    featuredSchemes = schemes
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredServices = filterServices(state.allServices, state.selectedCategoryId, query)
            )
        }
    }

    fun onApplicationSearchChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                applicationSearchQuery = query,
                filteredApplications = filterApplications(state.activeApplications, query)
            )
        }
    }

    fun onCategorySelected(categoryId: String?) {
        _uiState.update { state ->
            val newCat = if (state.selectedCategoryId == categoryId) null else categoryId
            state.copy(
                selectedCategoryId = newCat,
                filteredServices = filterServices(state.allServices, newCat, state.searchQuery)
            )
        }
    }

    fun toggleCategoryExpanded(categoryId: String) {
        _uiState.update { state ->
            state.copy(
                expandedCategoryId = if (state.expandedCategoryId == categoryId) null else categoryId
            )
        }
    }

    fun onTabSelected(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun openServicesWithCategory(categoryId: String?) {
        _uiState.update { state ->
            state.copy(
                selectedTab = HomeTab.SERVICES,
                selectedCategoryId = categoryId,
                expandedCategoryId = categoryId,
                filteredServices = filterServices(state.allServices, categoryId, state.searchQuery)
            )
        }
    }

    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadInitialData()
    }

    fun onServiceClicked(service: CitizenService) {
        _uiState.update { it.copy(selectedServiceForApplication = service) }
    }

    fun onDismissServiceDialog() {
        _uiState.update { it.copy(selectedServiceForApplication = null, submittedApplicationId = null) }
    }

    fun onConfirmApplicationSubmission(service: CitizenService) {
        viewModelScope.launch {
            val randomNum = (1000000000L..9999999999L).random()
            val newAppId = "JSK-2026-$randomNum"
            _uiState.update { it.copy(submittedApplicationId = newAppId) }
            _events.emit(HomeNavigationEvent.ShowToast("Application submitted successfully! Application No: $newAppId"))
        }
    }

    fun onApplicationClicked(application: ApplicationStatus) {
        viewModelScope.launch {
            _events.emit(HomeNavigationEvent.NavigateToApplicationDetail(application))
        }
    }

    private fun filterServices(
        services: List<CitizenService>,
        categoryId: String?,
        query: String
    ): List<CitizenService> {
        var result = services
        if (categoryId != null) {
            result = result.filter { it.categoryId == categoryId }
        }
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isNotEmpty()) {
            result = result.filter {
                it.title.lowercase().contains(cleanQuery) ||
                it.titleHindi.contains(cleanQuery) ||
                it.description.lowercase().contains(cleanQuery) ||
                (it.departmentName?.lowercase()?.contains(cleanQuery) == true) ||
                (it.formNumber?.lowercase()?.contains(cleanQuery) == true)
            }
        }
        return result
    }

    private fun filterApplications(
        apps: List<ApplicationStatus>,
        query: String
    ): List<ApplicationStatus> {
        val clean = query.trim().lowercase()
        if (clean.isEmpty()) return apps
        return apps.filter {
            it.applicationNumber.lowercase().contains(clean) ||
            it.serviceTitle.lowercase().contains(clean) ||
            it.applicantName.lowercase().contains(clean)
        }
    }
}
