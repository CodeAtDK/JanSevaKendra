package dev.dhruva.jansevakendra.core.repository

import dev.dhruva.jansevakendra.core.model.*
import dev.dhruva.jansevakendra.core.result.Resource
import kotlinx.coroutines.flow.Flow

interface CitizenServiceRepository {
    suspend fun getCategories(): Resource<List<ServiceCategory>>
    suspend fun getServices(categoryId: String? = null): Resource<List<CitizenService>>
    suspend fun getPopularServices(): Resource<List<CitizenService>>
    suspend fun searchServices(query: String): Resource<List<CitizenService>>
    suspend fun getServiceById(serviceId: String): Resource<CitizenService>
}

interface ApplicationRepository {
    fun observeActiveApplications(): Flow<List<ApplicationStatus>>
    suspend fun getApplications(): Resource<List<ApplicationStatus>>
    suspend fun getApplicationById(id: String): Resource<ApplicationStatus>
}

interface WelfareSchemeRepository {
    suspend fun getFeaturedSchemes(): Resource<List<WelfareScheme>>
}

interface AuthRepository {
    suspend fun sendOtp(mobileNumber: String): Resource<String>
    suspend fun verifyOtp(mobileNumber: String, otp: String): Resource<CitizenProfile>
    suspend fun logout(): Resource<Unit>
}

interface UserSessionRepository {
    fun observeCurrentProfile(): Flow<CitizenProfile?>
    suspend fun getCurrentProfile(): CitizenProfile?
    suspend fun saveProfile(profile: CitizenProfile)
    suspend fun clearSession()
    fun observeSelectedLanguage(): Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)
    fun observeIsOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
}
