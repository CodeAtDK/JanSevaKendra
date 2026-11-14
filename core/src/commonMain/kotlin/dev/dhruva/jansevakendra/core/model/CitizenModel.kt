package dev.dhruva.jansevakendra.core.model

/**
 * Supported app languages for Jan Seva Kendra
 */
enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val greeting: String) {
    HINDI("hi", "Hindi", "हिंदी", "नमस्ते"),
    ENGLISH("en", "English", "English", "Welcome"),
    MARATHI("mr", "Marathi", "मराठी", "नमस्कार"),
    GUJARATI("gu", "Gujarati", "ગુજરાતી", "નમસ્તે"),
    BENGALI("bn", "Bengali", "বাংলা", "নমস্কার"),
    TELUGU("te", "Telugu", "తెలుగు", "నమస్కారం");

    companion object {
        fun fromCode(code: String): AppLanguage = entries.firstOrNull { it.code == code } ?: HINDI
    }
}

/**
 * Citizen Service Category (e.g. 28 Official Departments of Jan Seva Kendra)
 */
data class ServiceCategory(
    val id: String,
    val name: String,
    val nameHindi: String,
    val iconKey: String,
    val serviceCount: Int,
    val highlightColorHex: Long = 0xFF1B3B5A,
    val departmentNumber: Int = 1,
    val departmentCode: String = "",
    val description: String = ""
)

/**
 * Individual Citizen Service offered by Jan Seva Kendra
 */
data class CitizenService(
    val id: String,
    val title: String,
    val titleHindi: String,
    val categoryId: String,
    val description: String,
    val processingDays: Int,
    val feeRupees: Int,
    val requiredDocuments: List<String>,
    val isPopular: Boolean = false,
    val rating: Float = 4.8f,
    val totalProcessedCount: Int = 12500,
    val formNumber: String? = null,
    val subCategory: String? = null,
    val departmentName: String? = null
)

/**
 * Status of an active or past application submitted by citizen
 */
enum class ApplicationStage {
    SUBMITTED,
    DOCUMENTS_VERIFYING,
    FIELD_VERIFICATION,
    APPROVED,
    REJECTED
}

data class ApplicationStatus(
    val id: String,
    val applicationNumber: String,
    val serviceId: String,
    val serviceTitle: String,
    val applicantName: String,
    val currentStage: ApplicationStage,
    val submissionDate: String,
    val estimatedCompletionDate: String,
    val remark: String? = null
)

/**
 * Government Welfare Scheme advertised on Home Screen
 */
data class WelfareScheme(
    val id: String,
    val title: String,
    val titleHindi: String,
    val department: String,
    val benefitDescription: String,
    val eligibilitySummary: String,
    val applicationDeadline: String,
    val tag: String,
    val bannerGradientHexStart: Long,
    val bannerGradientHexEnd: Long
)

/**
 * Citizen User Profile
 */
data class CitizenProfile(
    val id: String,
    val name: String,
    val mobileNumber: String,
    val email: String? = null,
    val language: AppLanguage = AppLanguage.HINDI,
    val district: String = "Lucknow",
    val state: String = "Uttar Pradesh",
    val isVerified: Boolean = true,
    val activeApplicationsCount: Int = 2
)
