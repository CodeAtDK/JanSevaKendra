package dev.dhruva.jansevakendra.core.di

import dev.dhruva.jansevakendra.core.data.*
import dev.dhruva.jansevakendra.core.repository.*
import dev.dhruva.jansevakendra.core.usecase.*

/**
 * Dependency container providing singletons and use cases for the Jan Seva Kendra application.
 */
object CoreContainer {

    // Repositories
    val citizenServiceRepository: CitizenServiceRepository by lazy {
        FakeCitizenServiceRepository()
    }

    val applicationRepository: ApplicationRepository by lazy {
        FakeApplicationRepository()
    }

    val welfareSchemeRepository: WelfareSchemeRepository by lazy {
        FakeWelfareSchemeRepository()
    }

    val authRepository: AuthRepository by lazy {
        FakeAuthRepository()
    }

    val userSessionRepository: UserSessionRepository by lazy {
        InMemoryUserSessionRepository()
    }

    // Use cases
    val getCitizenServicesUseCase: GetCitizenServicesUseCase by lazy {
        GetCitizenServicesUseCase(citizenServiceRepository)
    }

    val getCategoriesUseCase: GetCategoriesUseCase by lazy {
        GetCategoriesUseCase(citizenServiceRepository)
    }

    val searchCitizenServicesUseCase: SearchCitizenServicesUseCase by lazy {
        SearchCitizenServicesUseCase(citizenServiceRepository)
    }

    val getActiveApplicationsUseCase: GetActiveApplicationsUseCase by lazy {
        GetActiveApplicationsUseCase(applicationRepository)
    }

    val getFeaturedSchemesUseCase: GetFeaturedSchemesUseCase by lazy {
        GetFeaturedSchemesUseCase(welfareSchemeRepository)
    }

    val sendOtpUseCase: SendOtpUseCase by lazy {
        SendOtpUseCase(authRepository)
    }

    val verifyOtpUseCase: VerifyOtpUseCase by lazy {
        VerifyOtpUseCase(authRepository, userSessionRepository)
    }

    val manageSessionUseCase: ManageSessionUseCase by lazy {
        ManageSessionUseCase(userSessionRepository)
    }
}
