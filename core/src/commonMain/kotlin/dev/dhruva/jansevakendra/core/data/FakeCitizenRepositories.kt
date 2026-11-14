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

    // 28 Official Government Departments of Jan Seva Kendra / e-Seva Kendra
    private val categories = listOf(
        ServiceCategory(
            id = "cat_transport",
            name = "Transport & Motor Vehicles (RTO)",
            nameHindi = "परिवहन विभाग (आरटीओ)",
            iconKey = "car",
            serviceCount = 6,
            highlightColorHex = 0xFF0288D1,
            departmentNumber = 1,
            departmentCode = "RTO",
            description = "Driving Licences, Learner Licences (Form 3), Vehicle RC, Challans & Permits"
        ),
        ServiceCategory(
            id = "cat_revenue",
            name = "Revenue & Land Records (Tehsil)",
            nameHindi = "राजस्व एवं भूमि अभिलेख",
            iconKey = "document",
            serviceCount = 6,
            highlightColorHex = 0xFF8D6E63,
            departmentNumber = 2,
            departmentCode = "REV",
            description = "Income, Caste, Domicile Certificates, 7/12 Nakal & Mutation"
        ),
        ServiceCategory(
            id = "cat_identity",
            name = "Identity & Aadhaar (UIDAI)",
            nameHindi = "आधार एवं पहचान सेवाएं",
            iconKey = "fingerprint",
            serviceCount = 5,
            highlightColorHex = 0xFFE65100,
            departmentNumber = 3,
            departmentCode = "UIDAI",
            description = "Aadhaar Demographic Update, Biometric Update, PVC Card & Mobile Link"
        ),
        ServiceCategory(
            id = "cat_pan",
            name = "Income Tax & PAN Card (UTI/NSDL)",
            nameHindi = "आयकर एवं पैन सेवाएं",
            iconKey = "credit_card",
            serviceCount = 4,
            highlightColorHex = 0xFF1565C0,
            departmentNumber = 4,
            departmentCode = "ITD",
            description = "New PAN Card (Form 49A), Correction, e-PAN Instant Download & Aadhaar Link"
        ),
        ServiceCategory(
            id = "cat_food_ration",
            name = "Food, Civil Supplies & Consumer Affairs",
            nameHindi = "खाद्य एवं नागरिक आपूर्ति (राशन)",
            iconKey = "shopping_bag",
            serviceCount = 4,
            highlightColorHex = 0xFFEF6C00,
            departmentNumber = 5,
            departmentCode = "FCS",
            description = "NFSA Ration Card, Unit Addition, Family Member Transfer & e-Ration"
        ),
        ServiceCategory(
            id = "cat_agriculture",
            name = "Agriculture & Farmers Welfare (Kisan)",
            nameHindi = "कृषि एवं किसान कल्याण",
            iconKey = "leaf",
            serviceCount = 5,
            highlightColorHex = 0xFF2E7D32,
            departmentNumber = 6,
            departmentCode = "AGRI",
            description = "PM-Kisan Samman Nidhi e-KYC, Fasal Bima, KCC Loan & Farm Subsidies"
        ),
        ServiceCategory(
            id = "cat_social_welfare",
            name = "Social Justice, Welfare & Pensions",
            nameHindi = "सामाजिक न्याय एवं पेंशन",
            iconKey = "heart",
            serviceCount = 4,
            highlightColorHex = 0xFFAD1457,
            departmentNumber = 7,
            departmentCode = "SJW",
            description = "Old Age Pension, Widow Pension, Divyang Pension & UDID Card"
        ),
        ServiceCategory(
            id = "cat_women_child",
            name = "Women & Child Development",
            nameHindi = "महिला एवं बाल विकास",
            iconKey = "shield",
            serviceCount = 4,
            highlightColorHex = 0xFFC2185B,
            departmentNumber = 8,
            departmentCode = "WCD",
            description = "Majhi Ladki Bahin Yojana, PMMVY Matru Vandana, Sukanya Samriddhi"
        ),
        ServiceCategory(
            id = "cat_labour",
            name = "Labour & Employment (e-Shram)",
            nameHindi = "श्रम एवं रोजगार विभाग",
            iconKey = "briefcase",
            serviceCount = 4,
            highlightColorHex = 0xFF00838F,
            departmentNumber = 9,
            departmentCode = "LEP",
            description = "e-Shram Registration, BOCW Construction Worker Card & Rojgar Sangam"
        ),
        ServiceCategory(
            id = "cat_health",
            name = "Health & Family Welfare (Ayushman)",
            nameHindi = "स्वास्थ्य एवं परिवार कल्याण",
            iconKey = "local_hospital",
            serviceCount = 3,
            highlightColorHex = 0xFF00695C,
            departmentNumber = 10,
            departmentCode = "HFW",
            description = "Ayushman Bharat Golden Card (₹5 Lakh Free Treatment) & ABHA Health ID"
        ),
        ServiceCategory(
            id = "cat_electricity",
            name = "Energy & Electricity Discom",
            nameHindi = "ऊर्जा एवं विद्युत सेवाएं",
            iconKey = "flash",
            serviceCount = 3,
            highlightColorHex = 0xFFF57F17,
            departmentNumber = 11,
            departmentCode = "DISCOM",
            description = "Electricity Bill Payment, New Connection & PM Surya Ghar Solar"
        ),
        ServiceCategory(
            id = "cat_panchayat",
            name = "Panchayati Raj & Rural Development",
            nameHindi = "पंचायती राज एवं ग्रामीण विकास",
            iconKey = "home",
            serviceCount = 4,
            highlightColorHex = 0xFF558B2F,
            departmentNumber = 12,
            departmentCode = "PRD",
            description = "Parivar Register Nakal, MGNREGA Job Card & Rural Birth/Death Certificate"
        ),
        ServiceCategory(
            id = "cat_municipal",
            name = "Urban Local Bodies & Municipal Corporation",
            nameHindi = "नगर निगम एवं शहरी निकाय",
            iconKey = "apartment",
            serviceCount = 4,
            highlightColorHex = 0xFF37474F,
            departmentNumber = 13,
            departmentCode = "ULB",
            description = "House Tax, Water Supply, Trade Licence & Urban Birth/Death Registration"
        ),
        ServiceCategory(
            id = "cat_police",
            name = "Home & Police Citizen Services",
            nameHindi = "गृह एवं पुलिस सेवाएं",
            iconKey = "security",
            serviceCount = 4,
            highlightColorHex = 0xFF283593,
            departmentNumber = 14,
            departmentCode = "POLICE",
            description = "Police Clearance Certificate (PCC), Tenant Verification & e-Lost Report"
        ),
        ServiceCategory(
            id = "cat_education",
            name = "School & Higher Education / Scholarships",
            nameHindi = "शिक्षा एवं छात्रवृत्ति सेवाएं",
            iconKey = "school",
            serviceCount = 4,
            highlightColorHex = 0xFF6A1B9A,
            departmentNumber = 15,
            departmentCode = "EDU",
            description = "State Scholarship, National Scholarship Portal (NSP) & RTE Admission"
        ),
        ServiceCategory(
            id = "cat_passport",
            name = "Passport Seva Kendra (MEA)",
            nameHindi = "विदेश मंत्रालय एवं पासपोर्ट",
            iconKey = "flight",
            serviceCount = 3,
            highlightColorHex = 0xFF1565C0,
            departmentNumber = 16,
            departmentCode = "MEA",
            description = "Fresh Passport Application, Renewal/Re-issue & Tatkaal Passport Booking"
        ),
        ServiceCategory(
            id = "cat_election",
            name = "Election Commission (Voter ID / NVSP)",
            nameHindi = "निर्वाचन एवं मतदाता पहचान",
            iconKey = "how_to_vote",
            serviceCount = 3,
            highlightColorHex = 0xFF4527A0,
            departmentNumber = 17,
            departmentCode = "ECI",
            description = "New Voter Registration (Form 6), Voter Card Correction (Form 8) & e-EPIC"
        ),
        ServiceCategory(
            id = "cat_judiciary",
            name = "Courts & Legal Services (e-Courts)",
            nameHindi = "न्यायालय एवं विधिक सेवाएं",
            iconKey = "gavel",
            serviceCount = 3,
            highlightColorHex = 0xFF4E342E,
            departmentNumber = 18,
            departmentCode = "JUD",
            description = "Court Case Status, e-Stamping & Free Legal Aid Advocate"
        ),
        ServiceCategory(
            id = "cat_skill",
            name = "Skill Development & Entrepreneurship",
            nameHindi = "कौशल विकास एवं विश्वकर्मा",
            iconKey = "build",
            serviceCount = 3,
            highlightColorHex = 0xFFD84315,
            departmentNumber = 19,
            departmentCode = "MSDE",
            description = "PM Vishwakarma Toolkit (₹15,000 grant) & PMKVY Training Admission"
        ),
        ServiceCategory(
            id = "cat_msme",
            name = "Micro, Small & Medium Enterprises (MSME)",
            nameHindi = "सूक्ष्म, लघु एवं मध्यम उद्यम (उद्यम)",
            iconKey = "store",
            serviceCount = 3,
            highlightColorHex = 0xFF00695C,
            departmentNumber = 20,
            departmentCode = "MSME",
            description = "Udyam Registration Certificate, PMEGP Subsidy Loan & Mudra Assistance"
        ),
        ServiceCategory(
            id = "cat_housing",
            name = "Housing & Urban Affairs (PMAY)",
            nameHindi = "आवास एवं शहरी कार्य (पीएम आवास)",
            iconKey = "deck",
            serviceCount = 3,
            highlightColorHex = 0xFF0277BD,
            departmentNumber = 21,
            departmentCode = "PMAY",
            description = "Pradhan Mantri Awas Yojana ₹2.5 Lakh Housing Subsidy & Rural Awas"
        ),
        ServiceCategory(
            id = "cat_cooperation",
            name = "Cooperation & Sahara Refund (CRCS)",
            nameHindi = "सहकारिता एवं सहारा रिफंड",
            iconKey = "account_balance",
            serviceCount = 2,
            highlightColorHex = 0xFF3E2723,
            departmentNumber = 22,
            departmentCode = "CRCS",
            description = "CRCS Sahara Depositor Refund Portal Filing & PACS Cooperative"
        ),
        ServiceCategory(
            id = "cat_animal_husbandry",
            name = "Animal Husbandry, Dairying & Fisheries",
            nameHindi = "पशुपालन, डेयरी एवं मत्स्य",
            iconKey = "pets",
            serviceCount = 3,
            highlightColorHex = 0xFF5D4037,
            departmentNumber = 23,
            departmentCode = "AHDF",
            description = "Pashupalan Kisan Credit Card, Dairy Subsidy & PM Matsya Sampada"
        ),
        ServiceCategory(
            id = "cat_minority",
            name = "Minority Welfare & Waqf",
            nameHindi = "अल्पसंख्यक कल्याण एवं वक्फ",
            iconKey = "groups",
            serviceCount = 3,
            highlightColorHex = 0xFF004D40,
            departmentNumber = 24,
            departmentCode = "MOWA",
            description = "Begum Hazrat Mahal Scholarship, Minority Schemes & Haj Registration"
        ),
        ServiceCategory(
            id = "cat_jal_shakti",
            name = "Jal Shakti & Ground Water Resources",
            nameHindi = "जल शक्ति एवं भूगर्भ जल",
            iconKey = "water_drop",
            serviceCount = 2,
            highlightColorHex = 0xFF00838F,
            departmentNumber = 25,
            departmentCode = "JJM",
            description = "Har Ghar Nal Se Jal Tap Water & Tube-well Boring Subsidy"
        ),
        ServiceCategory(
            id = "cat_environment",
            name = "Forest, Environment & Climate Change",
            nameHindi = "वन, पर्यावरण एवं जलवायु",
            iconKey = "nature",
            serviceCount = 2,
            highlightColorHex = 0xFF1B5E20,
            departmentNumber = 26,
            departmentCode = "FECC",
            description = "Tree Felling Permission & Free Nursery Plant Sapling Booking"
        ),
        ServiceCategory(
            id = "cat_commerce",
            name = "Commerce, Industry & GST Services",
            nameHindi = "उद्योग, व्यापार एवं जीएसटी सेवाएं",
            iconKey = "business_center",
            serviceCount = 3,
            highlightColorHex = 0xFF424242,
            departmentNumber = 27,
            departmentCode = "GST",
            description = "New GST Registration, GST Return Filing & Shop Act Gumasta"
        ),
        ServiceCategory(
            id = "cat_banking_dbt",
            name = "Banking, Insurance & DBT Inclusion",
            nameHindi = "बैंकिंग, बीमा एवं डीबीटी समावेशन",
            iconKey = "payments",
            serviceCount = 4,
            highlightColorHex = 0xFF283593,
            departmentNumber = 28,
            departmentCode = "DBT",
            description = "PM Jan Dhan Yojana, Atal Pension, PMJJBY, PMSBY & Aadhaar NPCI Seeding"
        )
    )

    // Detailed Citizen Services Catalog organized under the 28 Departments
    private val services = listOf(
        // Department 1: Transport & Motor Vehicles / RTO (Features the User's Learner's Licence!)
        CitizenService(
            id = "srv_dl_learner",
            title = "Learner's Driving Licence (Form 3 - LMV & MCWG)",
            titleHindi = "लर्नर ड्राइविंग लाइसेंस आवेदन (Form 3)",
            categoryId = "cat_transport",
            description = "Application for Learner's Licence for Light Motor Vehicle (Car) & Motorcycle with Gear under CMV Rules 1989. Issued by Regional Transport Office (RTO).",
            processingDays = 7,
            feeRupees = 150,
            requiredDocuments = listOf(
                "Aadhaar Card (linked to Mobile)",
                "Age Proof (10th Marksheet or Birth Certificate)",
                "Address Proof",
                "Self-declaration Form 1 / 1A Medical fitness"
            ),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 68400,
            formNumber = "Form 3",
            departmentName = "Transport & RTO"
        ),
        CitizenService(
            id = "srv_dl_permanent",
            title = "Permanent Driving Licence (Form 7 - Smart Card)",
            titleHindi = "स्थाई ड्राइविंग लाइसेंस (स्मार्ट कार्ड)",
            categoryId = "cat_transport",
            description = "Upgrade from Learner's Licence to Permanent Smart Card Driving Licence after clearing practical driving test at RTO track.",
            processingDays = 15,
            feeRupees = 700,
            requiredDocuments = listOf(
                "Valid Learner's Licence",
                "Driving School Certificate (Form 5) if commercial",
                "Biometric photo and signature at RTO"
            ),
            isPopular = true,
            rating = 4.8f,
            totalProcessedCount = 45200,
            formNumber = "Form 7",
            departmentName = "Transport & RTO"
        ),
        CitizenService(
            id = "srv_dl_renewal",
            title = "Renewal / Address Change in Driving Licence",
            titleHindi = "ड्राइविंग लाइसेंस नवीनीकरण / पता परिवर्तन",
            categoryId = "cat_transport",
            description = "Renew expired driving licence or update residential address without re-taking the driving test.",
            processingDays = 10,
            feeRupees = 400,
            requiredDocuments = listOf("Original Driving Licence", "Medical Certificate Form 1A (if > 40 yrs)", "Address Proof"),
            isPopular = false,
            rating = 4.7f,
            totalProcessedCount = 22100,
            departmentName = "Transport & RTO"
        ),
        CitizenService(
            id = "srv_rc_transfer",
            title = "Vehicle Registration & Ownership Transfer (RC)",
            titleHindi = "वाहन पंजीकरण व स्वामित्व हस्तांतरण (RC)",
            categoryId = "cat_transport",
            description = "Transfer ownership of two-wheeler or four-wheeler (Form 29 & 30) or obtain duplicate RC.",
            processingDays = 14,
            feeRupees = 530,
            requiredDocuments = listOf("Original RC Book", "Form 29 & Form 30", "Valid Insurance & PUC Certificate", "Buyer & Seller Aadhaar"),
            isPopular = false,
            rating = 4.6f,
            totalProcessedCount = 18400,
            formNumber = "Form 29/30",
            departmentName = "Transport & RTO"
        ),
        CitizenService(
            id = "srv_echallan",
            title = "e-Challan Traffic Fine Settlement",
            titleHindi = "ई-चालान ऑनलाइन निस्तारण व भुगतान",
            categoryId = "cat_transport",
            description = "Check and pay pending traffic e-challans issued by traffic police or high-speed cameras.",
            processingDays = 0,
            feeRupees = 0,
            requiredDocuments = listOf("Vehicle Registration Number", "Challan Number"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 98000,
            departmentName = "Transport & RTO"
        ),

        // Department 2: Revenue & Land Records
        CitizenService(
            id = "srv_income_cert",
            title = "Income Certificate (Aay Praman Patra)",
            titleHindi = "आय प्रमाण पत्र निर्गमन",
            categoryId = "cat_revenue",
            description = "Official revenue authority certificate certifying annual household income for scholarships, subsidies, and schemes.",
            processingDays = 5,
            feeRupees = 30,
            requiredDocuments = listOf("Ration Card / Parivar Register copy", "Salary slip or Self-declaration", "Aadhaar Card"),
            isPopular = true,
            rating = 4.8f,
            totalProcessedCount = 75400,
            departmentName = "Revenue & Tehsil"
        ),
        CitizenService(
            id = "srv_caste_cert",
            title = "Caste Certificate (Jati Praman Patra)",
            titleHindi = "जाति प्रमाण पत्र निर्गमन",
            categoryId = "cat_revenue",
            description = "SC/ST/OBC caste verification certificate issued by Sub-Divisional Magistrate (SDM) or Tehsildar.",
            processingDays = 7,
            feeRupees = 30,
            requiredDocuments = listOf("Father's caste certificate or old lineage record", "Aadhaar Card", "Ration Card copy"),
            isPopular = true,
            rating = 4.7f,
            totalProcessedCount = 52000,
            departmentName = "Revenue & Tehsil"
        ),
        CitizenService(
            id = "srv_domicile_cert",
            title = "Domicile Certificate (Nivas Praman Patra)",
            titleHindi = "मूल निवास प्रमाण पत्र निर्गमन",
            categoryId = "cat_revenue",
            description = "Permanent residence certificate required for admission in government colleges and state employment.",
            processingDays = 5,
            feeRupees = 30,
            requiredDocuments = listOf("10+ years residential proof", "Electricity / Water connection bill", "Aadhaar Card"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 61800,
            departmentName = "Revenue & Tehsil"
        ),
        CitizenService(
            id = "srv_non_creamy",
            title = "Non-Creamy Layer Certificate (OBC/EWS)",
            titleHindi = "नॉन-क्रीमी लेयर प्रमाण पत्र",
            categoryId = "cat_revenue",
            description = "Income eligibility certificate for OBC reservation benefits in Central/State jobs and colleges.",
            processingDays = 7,
            feeRupees = 50,
            requiredDocuments = listOf("Income Certificate of last 3 years", "Caste Certificate", "Aadhaar Card"),
            isPopular = false,
            rating = 4.7f,
            totalProcessedCount = 19400,
            departmentName = "Revenue & Tehsil"
        ),
        CitizenService(
            id = "srv_land_extract",
            title = "7/12 & 8A Land Extract (सातबारा व खतौनी)",
            titleHindi = "भूमि खतौनी एवं 7/12 नकल",
            categoryId = "cat_revenue",
            description = "Digitally signed land record extract showing survey number, owner name, land area, and crop entries.",
            processingDays = 1,
            feeRupees = 20,
            requiredDocuments = listOf("District, Tehsil, Village Name", "Gata / Khasra / Survey Number"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 112000,
            departmentName = "Revenue & Tehsil"
        ),

        // Department 3: Identity & Aadhaar (UIDAI)
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
            totalProcessedCount = 142800,
            departmentName = "UIDAI Aadhaar"
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
            totalProcessedCount = 88900,
            formNumber = "Form 49A",
            departmentName = "Income Tax / NSDL"
        ),
        CitizenService(
            id = "srv_aadhaar_pvc",
            title = "Official UIDAI PVC Plastic Aadhaar Card",
            titleHindi = "पीवीसी प्लास्टिक आधार कार्ड ऑर्डर",
            categoryId = "cat_identity",
            description = "Durable, waterproof, pocket-friendly PVC card with hologram, micro-text, ghost image, and secure QR code delivered by Speed Post.",
            processingDays = 7,
            feeRupees = 50,
            requiredDocuments = listOf("Aadhaar Number", "Mobile number for OTP verification"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 94000,
            departmentName = "UIDAI Aadhaar"
        ),
        CitizenService(
            id = "srv_aadhaar_biometric",
            title = "Mandatory Biometric Update (Iris & Fingerprint)",
            titleHindi = "अनिवार्य बायोमेट्रिक अपडेट (5 व 15 वर्ष)",
            categoryId = "cat_identity",
            description = "Mandatory biometric capture for children reaching ages 5 and 15 years, or adult biometric re-verification.",
            processingDays = 5,
            feeRupees = 100,
            requiredDocuments = listOf("Child's Aadhaar Card", "Parent / Guardian Aadhaar Card", "School ID Card"),
            isPopular = false,
            rating = 4.8f,
            totalProcessedCount = 31000,
            departmentName = "UIDAI Aadhaar"
        ),

        // Department 4: Income Tax & PAN Services
        CitizenService(
            id = "srv_pan_instant",
            title = "Instant e-PAN Allotment (Digital in 10 mins)",
            titleHindi = "तत्काल ई-पैन कार्ड (10 मिनट में)",
            categoryId = "cat_pan",
            description = "Zero wait time paperless PAN generation through Aadhaar-based e-KYC directly from Income Tax Department.",
            processingDays = 0,
            feeRupees = 50,
            requiredDocuments = listOf("Aadhaar Card linked with active mobile number"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 76000,
            departmentName = "Income Tax Department"
        ),
        CitizenService(
            id = "srv_pan_link",
            title = "Link PAN Card with Aadhaar Card",
            titleHindi = "पैन कार्ड को आधार कार्ड से लिंक करना",
            categoryId = "cat_pan",
            description = "Comply with CBDT mandate to keep PAN operative by linking with 12-digit Aadhaar number.",
            processingDays = 1,
            feeRupees = 50,
            requiredDocuments = listOf("PAN Card", "Aadhaar Card"),
            isPopular = true,
            rating = 4.8f,
            totalProcessedCount = 135000,
            departmentName = "Income Tax Department"
        ),

        // Department 5: Food, Civil Supplies & Ration
        CitizenService(
            id = "srv_ration_card",
            title = "Ration Card Member Addition / New Card",
            titleHindi = "राशन कार्ड नया / यूनिट जोड़ना",
            categoryId = "cat_food_ration",
            description = "National Food Security Act (NFSA) ration card application and addition of new family members.",
            processingDays = 10,
            feeRupees = 50,
            requiredDocuments = listOf("Family Group Photo", "Aadhaar of all members", "Income certificate"),
            isPopular = true,
            rating = 4.6f,
            totalProcessedCount = 44300,
            departmentName = "Food & Civil Supplies"
        ),
        CitizenService(
            id = "srv_ration_transfer",
            title = "Ration Card Surrender / Inter-State Transfer",
            titleHindi = "राशन कार्ड स्थानांतरण / समर्पण",
            categoryId = "cat_food_ration",
            description = "One Nation One Ration Card (ONORC) transfer or surrender certificate for inter-district migration.",
            processingDays = 7,
            feeRupees = 30,
            requiredDocuments = listOf("Existing Ration Card", "New Address Proof", "Aadhaar Card"),
            isPopular = false,
            rating = 4.5f,
            totalProcessedCount = 11200,
            departmentName = "Food & Civil Supplies"
        ),

        // Department 6: Agriculture & PM-Kisan
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
            totalProcessedCount = 185000,
            departmentName = "Ministry of Agriculture"
        ),
        CitizenService(
            id = "srv_fasal_bima",
            title = "PM Fasal Bima Yojana (Crop Insurance)",
            titleHindi = "प्रधानमंत्री फसल बीमा योजना पंजीयन",
            categoryId = "cat_agriculture",
            description = "Comprehensive crop insurance coverage against natural calamities, pests, and drought for Kharif & Rabi seasons.",
            processingDays = 3,
            feeRupees = 40,
            requiredDocuments = listOf("Land Record (7/12 or Khatauni)", "Crop Sowing Certificate", "Bank Passbook"),
            isPopular = false,
            rating = 4.7f,
            totalProcessedCount = 28000,
            departmentName = "Ministry of Agriculture"
        ),

        // Department 7: Social Justice & Pensions
        CitizenService(
            id = "srv_pension_old",
            title = "Old Age Pension (Vridhavastha Pension)",
            titleHindi = "वृद्धावस्था पेंशन योजना (₹1000/माह)",
            categoryId = "cat_social_welfare",
            description = "Monthly financial assistance for citizens aged 60 years and above living below poverty line (BPL).",
            processingDays = 15,
            feeRupees = 50,
            requiredDocuments = listOf("Age Proof (60+ years)", "Income Certificate (under ₹46,080 rural)", "Bank Account", "Aadhaar Card"),
            isPopular = true,
            rating = 4.8f,
            totalProcessedCount = 47000,
            departmentName = "Social Welfare Dept"
        ),
        CitizenService(
            id = "srv_pension_widow",
            title = "Widow Pension Scheme (Nirashrit Mahila)",
            titleHindi = "निराश्रित महिला (विधवा) पेंशन",
            categoryId = "cat_social_welfare",
            description = "Monthly pension grant for destitute widows to ensure dignity and livelihood support.",
            processingDays = 15,
            feeRupees = 50,
            requiredDocuments = listOf("Husband's Death Certificate", "Income Certificate", "Bank Passbook", "Aadhaar Card"),
            isPopular = false,
            rating = 4.7f,
            totalProcessedCount = 33000,
            departmentName = "Social Welfare Dept"
        ),

        // Department 8: Women & Child Development
        CitizenService(
            id = "srv_ladki_bahin",
            title = "Majhi Ladki Bahin Yojana (₹1500/Month)",
            titleHindi = "माझी लाड़की बहिन योजना (₹1500 प्रतिमाह)",
            categoryId = "cat_women_child",
            description = "Maharashtra government financial empowerment scheme providing direct cash assistance of ₹1,500 every month to eligible women.",
            processingDays = 7,
            feeRupees = 25,
            requiredDocuments = listOf("Aadhaar Card", "Ration Card (Yellow/Orange)", "Income Certificate (< ₹2.5 Lakh)", "Bank Passbook seeded with Aadhaar"),
            isPopular = true,
            rating = 5.0f,
            totalProcessedCount = 210000,
            departmentName = "Women & Child Development"
        ),
        CitizenService(
            id = "srv_sukanya",
            title = "Sukanya Samriddhi Yojana (SSY Account)",
            titleHindi = "सुकन्या समृद्धि योजना खाता",
            categoryId = "cat_women_child",
            description = "High interest (8.2%) tax-free small savings scheme for girl child education and marriage.",
            processingDays = 2,
            feeRupees = 0,
            requiredDocuments = listOf("Girl Child Birth Certificate", "Parent / Guardian Aadhaar & PAN", "Photos"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 41000,
            departmentName = "Post Office / Banking"
        ),

        // Department 9: Labour & Employment
        CitizenService(
            id = "srv_eshram_reg",
            title = "e-Shram Card Registration (Unorganized Workers)",
            titleHindi = "ई-श्रम कार्ड पंजीयन व नवीनीकरण",
            categoryId = "cat_labour",
            description = "National database registration for unorganized workers with ₹2 Lakh accidental insurance cover and social security grants.",
            processingDays = 1,
            feeRupees = 20,
            requiredDocuments = listOf("Aadhaar linked Mobile Number", "Bank Account Details", "Occupation details"),
            isPopular = true,
            rating = 4.8f,
            totalProcessedCount = 168000,
            departmentName = "Ministry of Labour"
        ),

        // Department 10: Health & Family Welfare
        CitizenService(
            id = "srv_ayushman_card",
            title = "Ayushman Bharat PM-JAY Golden Card (₹5 Lakh)",
            titleHindi = "आयुष्मान भारत ₹5 लाख तक मुफ्त इलाज कार्ड",
            categoryId = "cat_health",
            description = "Cashless health insurance card for free medical treatment up to ₹5,00,000 per family per year in empanelled hospitals.",
            processingDays = 2,
            feeRupees = 30,
            requiredDocuments = listOf("Ration Card / SECC Name", "Aadhaar Card", "Active Mobile Number"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 192000,
            departmentName = "National Health Authority"
        ),
        CitizenService(
            id = "srv_abha_id",
            title = "ABHA Digital Health Account (Health ID)",
            titleHindi = "आभा डिजिटल हेल्थ आईडी निर्माण",
            categoryId = "cat_health",
            description = "Create your 14-digit Ayushman Bharat Health Account to securely store and share digital medical records.",
            processingDays = 0,
            feeRupees = 0,
            requiredDocuments = listOf("Aadhaar Card with mobile OTP"),
            isPopular = false,
            rating = 4.7f,
            totalProcessedCount = 58000,
            departmentName = "National Health Authority"
        ),

        // Department 11: Electricity & Utilities
        CitizenService(
            id = "srv_electricity_bill",
            title = "Electricity & Water Bill Payment",
            titleHindi = "बिजली एवं जल बिल भुगतान",
            categoryId = "cat_electricity",
            description = "Instant online bill clearance with authentic government receipt and discom confirmation.",
            processingDays = 0,
            feeRupees = 0,
            requiredDocuments = listOf("Consumer Account Number (CA / IVRS Number)"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 210000,
            departmentName = "Electricity Discom"
        ),
        CitizenService(
            id = "srv_pm_surya",
            title = "PM Surya Ghar Muft Bijli Yojana (Rooftop Solar)",
            titleHindi = "पीएम सूर्य घर मुफ्त बिजली योजना (सोलर)",
            categoryId = "cat_electricity",
            description = "Up to ₹78,000 direct subsidy for installing rooftop solar panels to get up to 300 units free power every month.",
            processingDays = 7,
            feeRupees = 50,
            requiredDocuments = listOf("Recent Electricity Bill", "House Ownership Proof / Roof Rights", "Bank Passbook", "Aadhaar Card"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 37000,
            departmentName = "Ministry of New & Renewable Energy"
        ),

        // Department 12: Panchayati Raj & Rural Development
        CitizenService(
            id = "srv_parivar_nakal",
            title = "Parivar Register Nakal (Kutumb Register)",
            titleHindi = "परिवार रजिस्टर नकल निर्गमन",
            categoryId = "cat_panchayat",
            description = "Official certified family lineage record issued by Gram Panchayat Secretary / Gram Vikas Adhikari.",
            processingDays = 4,
            feeRupees = 25,
            requiredDocuments = listOf("Gram Panchayat Name", "Family Head Name", "Aadhaar Card"),
            isPopular = false,
            rating = 4.6f,
            totalProcessedCount = 38000,
            departmentName = "Panchayati Raj"
        ),

        // Department 13: Urban Local Bodies & Municipal
        CitizenService(
            id = "srv_property_tax",
            title = "Municipal Property / House Tax Payment",
            titleHindi = "नगर निगम गृहकर व जलकर भुगतान",
            categoryId = "cat_municipal",
            description = "Online assessment and tax payment receipt for municipal corporation and Nagar Palika.",
            processingDays = 0,
            feeRupees = 0,
            requiredDocuments = listOf("House ID / Property PIN Number", "Previous Year Tax Receipt"),
            isPopular = false,
            rating = 4.7f,
            totalProcessedCount = 49000,
            departmentName = "Urban Local Bodies"
        ),

        // Department 14: Police & Character Verification
        CitizenService(
            id = "srv_pcc",
            title = "Police Clearance Certificate (PCC) / Character",
            titleHindi = "पुलिस चरित्र प्रमाण पत्र (PCC)",
            categoryId = "cat_police",
            description = "Official police background verification required for government jobs, private employment, bank Mitra, and passport.",
            processingDays = 14,
            feeRupees = 50,
            requiredDocuments = listOf("Aadhaar Card", "Local Police Station Address Proof", "Passport Size Photo", "Self-declaration"),
            isPopular = true,
            rating = 4.8f,
            totalProcessedCount = 39000,
            departmentName = "Police Citizen Portal"
        ),

        // Department 15: Education & Scholarships
        CitizenService(
            id = "srv_state_scholarship",
            title = "Post-Matric & Pre-Matric State Scholarship",
            titleHindi = "छात्रवृत्ति एवं शुल्क प्रतिपूर्ति आवेदन",
            categoryId = "cat_education",
            description = "Fee reimbursement and monthly allowance for 9th to Post-Graduate students studying in recognized institutions.",
            processingDays = 20,
            feeRupees = 50,
            requiredDocuments = listOf("Last Year Marksheet", "Income & Caste Certificate", "College Fee Receipt", "Bank Passbook with Aadhaar Seeding"),
            isPopular = true,
            rating = 4.8f,
            totalProcessedCount = 65000,
            departmentName = "Social Welfare & Education"
        ),

        // Department 16: Passport Seva Kendra (MEA)
        CitizenService(
            id = "srv_passport_new",
            title = "Fresh Indian Passport Application & Appointment",
            titleHindi = "नया पासपोर्ट आवेदन व अप्वाइंटमेंट",
            categoryId = "cat_passport",
            description = "Online application submission, government fee payment, and appointment slot booking at nearest PSK/POPSK.",
            processingDays = 21,
            feeRupees = 1500,
            requiredDocuments = listOf("Aadhaar Card", "10th School Passing Certificate (Non-ECR)", "Bank Passbook / Voter ID for address proof"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 28000,
            departmentName = "Ministry of External Affairs"
        ),

        // Department 17: Election Commission (Voter ID)
        CitizenService(
            id = "srv_voter_form6",
            title = "New Voter ID Registration (Form 6 / NVSP)",
            titleHindi = "नया मतदाता पहचान पत्र (फॉर्म 6)",
            categoryId = "cat_election",
            description = "Enroll as a voter on turning 18 years old and receive free plastic EPIC card at your home address.",
            processingDays = 15,
            feeRupees = 0,
            requiredDocuments = listOf("Passport Size Photo", "Age Proof (18+ yrs)", "Address Proof"),
            isPopular = false,
            rating = 4.7f,
            totalProcessedCount = 42000,
            formNumber = "Form 6",
            departmentName = "Election Commission"
        ),

        // Department 18: Courts & Legal
        CitizenService(
            id = "srv_estamp",
            title = "e-Stamping & Non-Judicial Stamp Paper",
            titleHindi = "ई-स्टाम्प पेपर निर्गमन (₹10 से ₹500+)",
            categoryId = "cat_judiciary",
            description = "Immediate generation of authentic government e-Stamp papers for affidavits, rent agreements, and contracts.",
            processingDays = 0,
            feeRupees = 20,
            requiredDocuments = listOf("First Party & Second Party Names", "Purpose of Stamp Paper", "Aadhaar Card"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 82000,
            departmentName = "Registration & Stamp Dept"
        ),

        // Department 19: Skill Development
        CitizenService(
            id = "srv_pm_vishwakarma",
            title = "PM Vishwakarma Yojana (₹15,000 Tool Kit)",
            titleHindi = "पीएम विश्वकर्मा योजना (टूलकिट व ₹3 लाख ऋण)",
            categoryId = "cat_skill",
            description = "Free skill training, ₹15,000 toolkit voucher, and collateral-free enterprise loan at 5% interest for 18 traditional artisan trades.",
            processingDays = 10,
            feeRupees = 25,
            requiredDocuments = listOf("Aadhaar Card", "Active Mobile Number", "Ration Card", "Bank Account Details"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 71000,
            departmentName = "Ministry of MSME & Skill"
        ),

        // Department 20: MSME & Udyam
        CitizenService(
            id = "srv_udyam_reg",
            title = "Udyam MSME Registration Certificate",
            titleHindi = "उद्यम पंजीयन प्रमाण पत्र (MSME)",
            categoryId = "cat_msme",
            description = "Government recognition certificate for micro, small, and medium businesses to access priority bank credit and subsidies.",
            processingDays = 2,
            feeRupees = 0,
            requiredDocuments = listOf("Aadhaar Card of Business Owner", "PAN Card", "Business Bank Account Details"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 54000,
            departmentName = "Ministry of MSME"
        ),

        // Department 21: Housing & Urban Affairs
        CitizenService(
            id = "srv_pmay_urban",
            title = "Pradhan Mantri Awas Yojana (PMAY ₹2.5 Lakh)",
            titleHindi = "प्रधानमंत्री आवास योजना (₹2.50 लाख अनुदान)",
            categoryId = "cat_housing",
            description = "Financial assistance of ₹2.5 Lakh for constructing a pucca house for homeless and kutcha house families.",
            processingDays = 30,
            feeRupees = 50,
            requiredDocuments = listOf("Land Plot Registry / Hakka Patra", "Family Income Certificate", "Aadhaar of all family members", "Bank Passbook"),
            isPopular = true,
            rating = 4.8f,
            totalProcessedCount = 63000,
            departmentName = "Ministry of Housing"
        ),

        // Department 22: Cooperation & CRCS
        CitizenService(
            id = "srv_crcs_sahara",
            title = "CRCS Sahara Depositor Refund Portal",
            titleHindi = "सहारा रिफंड पोर्टल दावा आवेदन",
            categoryId = "cat_cooperation",
            description = "Online claim submission for depositors of 4 Sahara cooperative societies under Supreme Court directive.",
            processingDays = 45,
            feeRupees = 50,
            requiredDocuments = listOf("Deposit Certificate / Passbook", "Aadhaar linked Mobile Number", "PAN Card (if claim > ₹50,000)"),
            isPopular = false,
            rating = 4.4f,
            totalProcessedCount = 38000,
            departmentName = "Ministry of Cooperation"
        ),

        // Department 23: Animal Husbandry
        CitizenService(
            id = "srv_pashu_kcc",
            title = "Pashupalan Kisan Credit Card (₹2 Lakh Loan)",
            titleHindi = "पशुपालक किसान क्रेडिट कार्ड (KCC)",
            categoryId = "cat_animal_husbandry",
            description = "Concessional credit facility for cow, buffalo, goat rearing, poultry, and fish farming with 3% prompt repayment subsidy.",
            processingDays = 15,
            feeRupees = 50,
            requiredDocuments = listOf("Animal Health & Tagging Certificate", "Aadhaar Card", "Bank Account Details"),
            isPopular = false,
            rating = 4.7f,
            totalProcessedCount = 21000,
            departmentName = "Animal Husbandry & Dairying"
        ),

        // Department 24: Minority Welfare
        CitizenService(
            id = "srv_begum_hazrat",
            title = "Begum Hazrat Mahal National Scholarship",
            titleHindi = "बेगम हजरत महल राष्ट्रीय छात्रवृत्ति",
            categoryId = "cat_minority",
            description = "Merit scholarship for meritorious girl students belonging to minority communities studying in classes 9th to 12th.",
            processingDays = 20,
            feeRupees = 30,
            requiredDocuments = listOf("Marksheet of previous class with 50%+ marks", "Annual Income Certificate (< ₹2 Lakh)", "Self-declaration"),
            isPopular = false,
            rating = 4.7f,
            totalProcessedCount = 17500,
            departmentName = "Ministry of Minority Affairs"
        ),

        // Department 25: Jal Shakti
        CitizenService(
            id = "srv_nal_se_jal",
            title = "Har Ghar Nal Se Jal Tap Water Connection",
            titleHindi = "जल जीवन मिशन नल कनेक्शन आवेदन",
            categoryId = "cat_jal_shakti",
            description = "Apply for functional household tap connection under Jal Jeevan Mission for potable drinking water.",
            processingDays = 10,
            feeRupees = 0,
            requiredDocuments = listOf("House Tax Receipt or Panchayat NOC", "Aadhaar Card"),
            isPopular = false,
            rating = 4.6f,
            totalProcessedCount = 33000,
            departmentName = "Department of Drinking Water"
        ),

        // Department 26: Environment & Forest
        CitizenService(
            id = "srv_tree_cutting",
            title = "Private Land Tree Felling Permission",
            titleHindi = "निजी भूमि वृक्ष पातन अनुमति",
            categoryId = "cat_environment",
            description = "Obtain legal NOC and permission from Divisional Forest Officer (DFO) to cut mature trees on private land.",
            processingDays = 15,
            feeRupees = 100,
            requiredDocuments = listOf("Land Record (7/12 or Khatauni)", "Tree Species & Count Details", "Aadhaar Card"),
            isPopular = false,
            rating = 4.5f,
            totalProcessedCount = 8900,
            departmentName = "Forest Department"
        ),

        // Department 27: Commerce & GST
        CitizenService(
            id = "srv_gst_reg",
            title = "New GST Registration & GSTIN Certificate",
            titleHindi = "नया जीएसटी पंजीयन एवं सर्टिफिकेट",
            categoryId = "cat_commerce",
            description = "Apply for Goods and Services Tax Identification Number (GSTIN) for traders, manufacturers, and service providers.",
            processingDays = 7,
            feeRupees = 150,
            requiredDocuments = listOf("PAN Card of Business", "Aadhaar Card", "Electricity Bill / Rent Agreement of shop", "Bank Account Statement"),
            isPopular = true,
            rating = 4.8f,
            totalProcessedCount = 31000,
            departmentName = "GST Council / CBIC"
        ),

        // Department 28: Banking & DBT Inclusion
        CitizenService(
            id = "srv_jan_dhan",
            title = "PM Jan Dhan Zero Balance Account",
            titleHindi = "प्रधानमंत्री जन धन खाता पंजीयन",
            categoryId = "cat_banking_dbt",
            description = "Universal access to banking facilities with zero minimum balance, free RuPay debit card, and ₹2 Lakh accident cover.",
            processingDays = 2,
            feeRupees = 0,
            requiredDocuments = listOf("Aadhaar Card", "2 Passport Size Photos"),
            isPopular = true,
            rating = 4.9f,
            totalProcessedCount = 148000,
            departmentName = "Department of Financial Services"
        ),
        CitizenService(
            id = "srv_npci_dbt",
            title = "NPCI Aadhaar Bank Seeding & DBT Status Check",
            titleHindi = "एनपीसीआई आधार बैंक सीडिंग जांच",
            categoryId = "cat_banking_dbt",
            description = "Verify whether your Aadhaar is linked with NPCI mapper to receive government subsidies (PM-Kisan, Ladki Bahin, Scholarship, LPG) without interruption.",
            processingDays = 0,
            feeRupees = 10,
            requiredDocuments = listOf("Aadhaar Number", "Mobile OTP"),
            isPopular = true,
            rating = 5.0f,
            totalProcessedCount = 270000,
            departmentName = "NPCI / UIDAI"
        )
    )

    override suspend fun getCategories(): Resource<List<ServiceCategory>> {
        delay(80)
        return Resource.Success(categories)
    }

    override suspend fun getServices(categoryId: String?): Resource<List<CitizenService>> {
        delay(100)
        return if (categoryId == null) {
            Resource.Success(services)
        } else {
            Resource.Success(services.filter { it.categoryId == categoryId })
        }
    }

    override suspend fun getPopularServices(): Resource<List<CitizenService>> {
        delay(60)
        return Resource.Success(services.filter { it.isPopular })
    }

    override suspend fun searchServices(query: String): Resource<List<CitizenService>> {
        delay(60)
        val clean = query.trim().lowercase()
        if (clean.isEmpty()) return Resource.Success(services)
        val filtered = services.filter {
            it.title.lowercase().contains(clean) ||
            it.titleHindi.contains(clean) ||
            it.description.lowercase().contains(clean) ||
            (it.departmentName?.lowercase()?.contains(clean) == true) ||
            (it.formNumber?.lowercase()?.contains(clean) == true)
        }
        return Resource.Success(filtered)
    }

    override suspend fun getServiceById(serviceId: String): Resource<CitizenService> {
        delay(50)
        val match = services.firstOrNull { it.id == serviceId }
        return if (match != null) {
            Resource.Success(match)
        } else {
            Resource.Error(AppError.NotFound("Service with ID $serviceId not found."))
        }
    }
}

class FakeApplicationRepository : ApplicationRepository {

    // Active applications including the User's Form 3 Learner's Licence from the uploaded PDF!
    private val activeApplications = MutableStateFlow(
        listOf(
            ApplicationStatus(
                id = "app_dl_5104819326",
                applicationNumber = "5104819326",
                serviceId = "srv_dl_learner",
                serviceTitle = "Learner's Driving Licence (Form 3 - LMV & MCWG)",
                applicantName = "Dhruva Rajendra Khatavkar",
                currentStage = ApplicationStage.APPROVED,
                submissionDate = "13 Nov 2026",
                estimatedCompletionDate = "13 Nov 2026",
                remark = "MH23 /0039233/2026 issued by RTO Beed, MH. Valid till 12/05/2027. Ready to download Form 3."
            ),
            ApplicationStatus(
                id = "app_101",
                applicationNumber = "JSK-2026-98124",
                serviceId = "srv_aadhaar_update",
                serviceTitle = "Aadhaar Card Mobile & Address Update",
                applicantName = "Rajesh Kumar Sharma",
                currentStage = ApplicationStage.DOCUMENTS_VERIFYING,
                submissionDate = "12 Nov 2026",
                estimatedCompletionDate = "16 Nov 2026",
                remark = "Document verification under progress by UIDAI verification desk."
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
                remark = "Digitally signed certificate issued by Tehsildar. Ready to download."
            )
        )
    )

    override fun observeActiveApplications(): Flow<List<ApplicationStatus>> = activeApplications.asStateFlow()

    override suspend fun getApplications(): Resource<List<ApplicationStatus>> {
        delay(60)
        return Resource.Success(activeApplications.value)
    }

    override suspend fun getApplicationById(id: String): Resource<ApplicationStatus> {
        delay(40)
        val app = activeApplications.value.firstOrNull { it.id == id }
        return if (app != null) Resource.Success(app) else Resource.Error(AppError.NotFound("Application not found."))
    }
}

class FakeWelfareSchemeRepository : WelfareSchemeRepository {
    private val schemes = listOf(
        WelfareScheme(
            id = "sch_ladki_bahin",
            title = "Majhi Ladki Bahin Yojana (माझी लाड़की बहिन योजना)",
            titleHindi = "मुख्यमंत्री माझी लाड़की बहिन योजना",
            department = "Women & Child Development, Maharashtra",
            benefitDescription = "Direct Benefit Transfer (DBT) of ₹1,500 credited monthly directly to bank account.",
            eligibilitySummary = "Women aged 21-65 years with family income below ₹2.5 Lakh per annum.",
            applicationDeadline = "Ongoing",
            tag = "Cash Transfer",
            bannerGradientHexStart = 0xFFC2185B,
            bannerGradientHexEnd = 0xFFE91E63
        ),
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
        delay(60)
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
            name = "Dhruva Rajendra Khatavkar",
            mobileNumber = mobileNumber,
            email = "dhruva.khatavkar@example.com",
            language = AppLanguage.ENGLISH,
            district = "Beed",
            state = "Maharashtra",
            isVerified = true,
            activeApplicationsCount = 3
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
    private val languageState = MutableStateFlow(AppLanguage.ENGLISH)
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
