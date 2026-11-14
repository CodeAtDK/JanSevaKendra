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
    SERVICES("सेवाएं", "Services", "grid"),
    APPLICATIONS("आवेदन", "Tracking", "track"),
    SCHEMES("योजनाएं", "Schemes", "award"),
    PROFILE("प्रोफ़ाइल", "Profile", "user")
}

data class HomeUiState(
    val profile: CitizenProfile? = null,
    val selectedLanguage: AppLanguage = AppLanguage.HINDI,
    val selectedTab: HomeTab = HomeTab.SERVICES,
    val categories: List<ServiceCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val allServices: List<CitizenService> = emptyList(),
    val filteredServices: List<CitizenService> = emptyList(),
    val activeApplications: List<ApplicationStatus> = emptyList(),
    val featuredSchemes: List<WelfareScheme> = emptyList(),
    val searchQuery: String = "",
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
                _uiState.update { it.copy(activeApplications = apps) }
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

            val services = servicesRes.getOrDefault(emptyList())

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    categories = categoriesRes.getOrDefault(emptyList()),
                    allServices = services,
                    filteredServices = filterServices(services, state.selectedCategoryId, state.searchQuery),
                    activeApplications = appsRes.getOrDefault(emptyList()),
                    featuredSchemes = schemesRes.getOrDefault(emptyList())
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

    fun onCategorySelected(categoryId: String?) {
        _uiState.update { state ->
            val newCat = if (state.selectedCategoryId == categoryId) null else categoryId
            state.copy(
                selectedCategoryId = newCat,
                filteredServices = filterServices(state.allServices, newCat, state.searchQuery)
            )
        }
    }

    fun onTabSelected(tab: HomeTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadInitialData()
    }

    fun onServiceClicked(service: CitizenService) {
        viewModelScope.launch {
            _events.emit(HomeNavigationEvent.NavigateToServiceDetail(service))
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
                it.description.lowercase().contains(cleanQuery)
            }
        }
        return result
    }
}
