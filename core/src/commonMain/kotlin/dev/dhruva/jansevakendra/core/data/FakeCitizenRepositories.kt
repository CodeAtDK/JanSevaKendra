package dev.dhruva.jansevakendra.core.data

import dev.dhruva.jansevakendra.core.model.*
import dev.dhruva.jansevakendra.core.repository.*
import dev.dhruva.jansevakendra.core.result.AppError
import dev.dhruva.jansevakendra.core.result.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCitizenServiceRepository : CitizenServiceRepository {

    private val categories = listOf(
        ServiceCategory("cat_identity", "Identity & Aadhaar", "आधार एवं पहचान", "fingerprint", 8, 0xFFE65100),
        ServiceCategory("cat_certificates", "Certificates & Revenue", "प्रमाण पत्र एवं राजस्व", "document", 12, 0xFF1B3B5A),
        ServiceCategory("cat_schemes", "Welfare & Pensions", "कल्याणकारी योजनाएं", "heart", 15, 0xFF2E7D32),
        ServiceCategory("cat_utilities", "Utility & Bill Payments", "बिल एवं जनोपयोगी सेवाएं", "flash", 9, 0xFFF57C00),
        ServiceCategory("cat_agriculture", "Agriculture & Land (Kisan)", "कृषि एवं भूमि रिकार्ड", "leaf", 7, 0xFF388E3C),
        ServiceCategory("cat_education", "Education & Employment", "शिक्षा एवं रोजगार", "book", 10, 0xFF0288D1)
    )

    private val services = listOf(
        CitizenService(
            id = "srv_aadhaar_update",
            title = "Aadhaar Card Mobile & Address Update",
            titleHindi = "आधार कार्ड मोबाइल व पता संशोधन",
            categoryId = "cat_identity",
            description = "Update your verified mobile number, residential address, or demographic details in UIDAI database.",
            processingDays = 3,
            feeRupees = 50,
            requiredDocuments = listOf("Existing Aadhaar Card", "Address Proof (Electricity Bill/Ration Card)", "Active Mobile Phone"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 42800
        ),
        CitizenService(
            id = "srv_pan_card",
            title = "New PAN Card / Correction (UTI/NSDL)",
            titleHindi = "नया पैन कार्ड / सुधार आवेदन",
            categoryId = "cat_identity",
            description = "Instant e-PAN allotment and physical card delivery to your doorstep within 7-10 working days.",
            processingDays = 7,
            feeRupees = 107,
            requiredDocuments = listOf("Aadhaar Card with linked mobile", "2 Passport Size Photos", "Signature specimen"),
            isPopular = true,
            rating = 4.8f,
            totalProcessedCount = 28900
        ),
        CitizenService(
            id = "srv_income_cert",
            title = "Income Certificate (Aay Praman Patra)",
            titleHindi = "आय प्रमाण पत्र निर्गमन",
            categoryId = "cat_certificates",
            description = "Official revenue authority certificate certifying annual household income for scholarships and subsidies.",
            processingDays = 5,
            feeRupees = 30,
            requiredDocuments = listOf("Ration Card / Parivar Register copy", "Salary slip or Self-declaration", "Aadhaar Card"),
            isPopular = true,
            rating = 4.7f,
            totalProcessedCount = 35400
        ),
        CitizenService(
            id = "srv_caste_cert",
            title = "Caste Certificate (Jati Praman Patra)",
            titleHindi = "जाति प्रमाण पत्र निर्गमन",
            categoryId = "cat_certificates",
            description = "SC/ST/OBC caste verification certificate issued by Sub-Divisional Magistrate (SDM) or Tehsildar.",
            processingDays = 7,
            feeRupees = 30,
            requiredDocuments = listOf("Father's caste certificate or old lineage record", "Aadhaar Card", "Ration Card copy"),
            isPopular = true,
            rating = 4.6f,
            totalProcessedCount = 21000
        ),
        CitizenService(
            id = "srv_domicile_cert",
            title = "Domicile Certificate (Nivas Praman Patra)",
            titleHindi = "निवास प्रमाण पत्र निर्गमन",
            categoryId = "cat_certificates",
            description = "Permanent residence certificate required for admission in government colleges and state employment.",
            processingDays = 5,
            feeRupees = 30,
            requiredDocuments = listOf("10+ years residential proof", "Electricity / Water connection bill", "Aadhaar Card"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 19800
        ),
        CitizenService(
            id = "srv_pm_kisan",
            title = "PM-Kisan Samman Nidhi e-KYC & Status",
            titleHindi = "पीएम-किसान सम्मान निधि ई-केवाईसी",
            categoryId = "cat_agriculture",
            description = "Biometric e-KYC authentication and bank account seeding for financial grant of ₹6,000/year.",
            processingDays = 1,
            feeRupees = 15,
            requiredDocuments = listOf("Aadhaar Card", "Land Khatauni / Revenue Record", "Bank Passbook with IFSC"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 51200
        ),
        CitizenService(
            id = "srv_ration_card",
            title = "Ration Card Member Addition / New Card",
            titleHindi = "राशन कार्ड नया / यूनिट जोड़ना",
            categoryId = "cat_schemes",
            description = "National Food Security Act (NFSA) ration card application and addition of new family members.",
            processingDays = 10,
            feeRupees = 50,
            requiredDocuments = listOf("Family Group Photo", "Aadhaar of all members", "Income certificate"),
            isPopular = false,
            rating = 4.5f,
            totalProcessedCount = 14300
        ),
        CitizenService(
            id = "srv_electricity_bill",
            title = "Electricity & Water Bill Payment",
            titleHindi = "बिजली एवं जल बिल भुगतान",
            categoryId = "cat_utilities",
            description = "Instant online bill clearance with authentic government receipt and cashback reward tokens.",
            processingDays = 0,
            feeRupees = 0,
            requiredDocuments = listOf("Consumer Account Number (CA Number)"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 89000
        )
    )

    override suspend fun getCategories(): Resource<List<ServiceCategory>> {
        delay(120) // Realistic network latency simulation
        return Resource.Success(categories)
    }

    override suspend fun getServices(categoryId: String?): Resource<List<CitizenService>> {
        delay(150)
        return if (categoryId == null) {
            Resource.Success(services)
        } else {
            Resource.Success(services.filter { it.categoryId == categoryId })
        }
    }

    override suspend fun getPopularServices(): Resource<List<CitizenService>> {
        delay(100)
        return Resource.Success(services.filter { it.isPopular })
    }

    override suspend fun searchServices(query: String): Resource<List<CitizenService>> {
        delay(80)
        val clean = query.trim().lowercase()
        if (clean.isEmpty()) return Resource.Success(services)
        val filtered = services.filter {
            it.title.lowercase().contains(clean) ||
            it.titleHindi.contains(clean) ||
            it.description.lowercase().contains(clean)
        }
        return Resource.Success(filtered)
    }

    override suspend fun getServiceById(serviceId: String): Resource<CitizenService> {
        delay(100)
        val match = services.firstOrNull { it.id == serviceId }
        return if (match != null) {
            Resource.Success(match)
        } else {
            Resource.Error(AppError.NotFound("Service with ID $serviceId not found."))
        }
    }
}

class FakeApplicationRepository : ApplicationRepository {

    private val activeApplications = MutableStateFlow(
        listOf(
            ApplicationStatus(
                id = "app_101",
                applicationNumber = "JSK-2026-98124",
                serviceId = "srv_aadhaar_update",
                serviceTitle = "Aadhaar Card Mobile & Address Update",
                applicantName = "Rajesh Kumar Sharma",
                currentStage = ApplicationStage.DOCUMENTS_VERIFYING,
                submissionDate = "12 Nov 2026",
                estimatedCompletionDate = "16 Nov 2026",
                remark = "Document verification under progress by Tehsildar desk."
            ),
            ApplicationStatus(
                id = "app_102",
                applicationNumber = "JSK-2026-77319",
                serviceId = "srv_income_cert",
                serviceTitle = "Income Certificate (Aay Praman Patra)",
                applicantName = "Rajesh Kumar Sharma",
                currentStage = ApplicationStage.APPROVED,
                submissionDate = "05 Nov 2026",
                estimatedCompletionDate = "10 Nov 2026",
                remark = "Digitally signed certificate issued. Ready to download."
            )
        )
    )

    override fun observeActiveApplications(): Flow<List<ApplicationStatus>> = activeApplications.asStateFlow()

    override suspend fun getApplications(): Resource<List<ApplicationStatus>> {
        delay(120)
        return Resource.Success(activeApplications.value)
    }

    override suspend fun getApplicationById(id: String): Resource<ApplicationStatus> {
        delay(80)
        val app = activeApplications.value.firstOrNull { it.id == id }
        return if (app != null) Resource.Success(app) else Resource.Error(AppError.NotFound("Application not found."))
    }
}

class FakeWelfareSchemeRepository : WelfareSchemeRepository {
    private val schemes = listOf(
        WelfareScheme(
            id = "sch_pm_kisan",
            title = "PM Kisan Samman Nidhi 18th Installment",
            titleHindi = "प्रधानमंत्री किसान सम्मान निधि १८वीं किस्त",
            department = "Ministry of Agriculture & Farmers Welfare",
            benefitDescription = "Direct Benefit Transfer (DBT) of ₹2,000 credited to eligible farmer accounts.",
            eligibilitySummary = "All landholding farmer families with seeded Aadhaar Bank Account.",
            applicationDeadline = "30 Nov 2026",
            tag = "Direct Transfer",
            bannerGradientHexStart = 0xFF1B5E20,
            bannerGradientHexEnd = 0xFF43A047
        ),
        WelfareScheme(
            id = "sch_ayushman",
            title = "Ayushman Bharat PM-JAY Golden Card",
            titleHindi = "आयुष्मान भारत ₹५ लाख तक मुफ्त इलाज",
            department = "National Health Authority",
            benefitDescription = "Free secondary and tertiary healthcare coverage up to ₹5 Lakhs per family per year.",
            eligibilitySummary = "Families identified under SECC 2011 & Antyodaya Anna Yojana.",
            applicationDeadline = "Ongoing",
            tag = "Health Guarantee",
            bannerGradientHexStart = 0xFF0D47A1,
            bannerGradientHexEnd = 0xFF1976D2
        ),
        WelfareScheme(
            id = "sch_up_scholarship",
            title = "Post-Matric Scholarship Scheme 2026",
            titleHindi = "दशमोत्तर छात्रवृत्ति एवं शुल्क प्रतिपूर्ति",
            department = "Social Welfare Department",
            benefitDescription = "100% Tuition fee reimbursement and monthly stipend for higher education.",
            eligibilitySummary = "Students with household income under ₹2.5 Lakhs/year.",
            applicationDeadline = "15 Dec 2026",
            tag = "Education Subsidy",
            bannerGradientHexStart = 0xFFB71C1C,
            bannerGradientHexEnd = 0xFFE53935
        )
    )

    override suspend fun getFeaturedSchemes(): Resource<List<WelfareScheme>> {
        delay(100)
        return Resource.Success(schemes)
    }
}

class FakeAuthRepository : AuthRepository {
    override suspend fun sendOtp(mobileNumber: String): Resource<String> {
        delay(600)
        if (mobileNumber.length != 10 || !mobileNumber.all { it.isDigit() }) {
            return Resource.Error(AppError.Validation("Please enter a valid 10-digit Indian mobile number."))
        }
        // In prototype, OTP is always 123456
        return Resource.Success("OTP sent successfully to +91 $mobileNumber")
    }

    override suspend fun verifyOtp(mobileNumber: String, otp: String): Resource<CitizenProfile> {
        delay(800)
        if (otp != "123456" && otp != "999999") {
            return Resource.Error(AppError.Validation("Incorrect OTP entered. For test please use 123456."))
        }
        val profile = CitizenProfile(
            id = "cit_987654",
            name = "Rajesh Kumar Sharma",
            mobileNumber = mobileNumber,
            email = "rajesh.sharma@example.com",
            language = AppLanguage.HINDI,
            district = "Lucknow",
            state = "Uttar Pradesh",
            isVerified = true,
            activeApplicationsCount = 2
        )
        return Resource.Success(profile)
    }

    override suspend fun logout(): Resource<Unit> {
        delay(200)
        return Resource.Success(Unit)
    }
}

class InMemoryUserSessionRepository : UserSessionRepository {
    private val profileState = MutableStateFlow<CitizenProfile?>(null)
    private val languageState = MutableStateFlow(AppLanguage.HINDI)
    private val onboardingCompletedState = MutableStateFlow(false)

    override fun observeCurrentProfile(): Flow<CitizenProfile?> = profileState.asStateFlow()

    override suspend fun getCurrentProfile(): CitizenProfile? = profileState.value

    override suspend fun saveProfile(profile: CitizenProfile) {
        profileState.value = profile
    }

    override suspend fun clearSession() {
        profileState.value = null
    }

    override fun observeSelectedLanguage(): Flow<AppLanguage> = languageState.asStateFlow()

    override suspend fun setLanguage(language: AppLanguage) {
        languageState.value = language
    }

    override fun observeIsOnboardingCompleted(): Flow<Boolean> = onboardingCompletedState.asStateFlow()

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        onboardingCompletedState.value = completed
    }
}
