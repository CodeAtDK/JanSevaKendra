package dev.dhruva.jansevakendra.core.usecase

import dev.dhruva.jansevakendra.core.model.*
import dev.dhruva.jansevakendra.core.repository.*
import dev.dhruva.jansevakendra.core.result.Resource
import kotlinx.coroutines.flow.Flow

class GetCitizenServicesUseCase(private val repository: CitizenServiceRepository) {
    suspend operator fun invoke(categoryId: String? = null): Resource<List<CitizenService>> =
        repository.getServices(categoryId)
}

class GetCategoriesUseCase(private val repository: CitizenServiceRepository) {
    suspend operator fun invoke(): Resource<List<ServiceCategory>> =
        repository.getCategories()
}

class SearchCitizenServicesUseCase(private val repository: CitizenServiceRepository) {
    suspend operator fun invoke(query: String): Resource<List<CitizenService>> =
        repository.searchServices(query)
}

class GetActiveApplicationsUseCase(private val repository: ApplicationRepository) {
    suspend operator fun invoke(): Resource<List<ApplicationStatus>> =
        repository.getApplications()

    fun observe(): Flow<List<ApplicationStatus>> = repository.observeActiveApplications()
}

class GetFeaturedSchemesUseCase(private val repository: WelfareSchemeRepository) {
    suspend operator fun invoke(): Resource<List<WelfareScheme>> =
        repository.getFeaturedSchemes()
}

class SendOtpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(mobileNumber: String): Resource<String> =
        repository.sendOtp(mobileNumber)
}

class VerifyOtpUseCase(
    private val authRepository: AuthRepository,
    private val sessionRepository: UserSessionRepository
) {
    suspend operator fun invoke(mobileNumber: String, otp: String): Resource<CitizenProfile> {
        val result = authRepository.verifyOtp(mobileNumber, otp)
        result.onSuccess { profile ->
            sessionRepository.saveProfile(profile)
        }
        return result
    }
}

class ManageSessionUseCase(private val sessionRepository: UserSessionRepository) {
    fun observeProfile(): Flow<CitizenProfile?> = sessionRepository.observeCurrentProfile()
    fun observeLanguage(): Flow<AppLanguage> = sessionRepository.observeSelectedLanguage()
    fun observeOnboarding(): Flow<Boolean> = sessionRepository.observeIsOnboardingCompleted()

    suspend fun setLanguage(language: AppLanguage) = sessionRepository.setLanguage(language)
    suspend fun setOnboardingCompleted(completed: Boolean) = sessionRepository.setOnboardingCompleted(completed)
    suspend fun logout() = sessionRepository.clearSession()
}
