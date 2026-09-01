package com.example.parttimego.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.MutedText
import com.example.parttimego.ui.theme.PartTimeGOTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = DarkNavy, // 确保底层全部是深蓝色
        contentWindowInsets = WindowInsets(0), // 禁用默认的 inset 填充，防止状态栏被填充白块
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Terms & Conditions",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy, // 明确 TopAppBar 的背景为 DarkNavy，覆盖系统状态栏区域
                    scrolledContainerColor = DarkNavy
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkNavy)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Text(
                        text = "PartTimeGO Master Terms of Service & User Agreement",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Effective Date: September 1, 2026 | Version 2.4",
                        fontSize = 12.sp,
                        color = MutedText
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(20.dp))

                    // Intro Notice
                    Text(
                        text = "PLEASE READ THESE TERMS AND CONDITIONS CAREFULLY BEFORE USING THE PARTTIMEGO PLATFORM. BY REGISTERING AN ACCOUNT, ACCESSING, OR USING ANY PART OF OUR SERVICES, YOU AGREE TO BE BOUND BY ALL TERMS AND POLICIES INCORPORATED HEREIN.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Section 1
                    TermsSection(
                        title = "1. Introduction & Agreement Scope",
                        content = "These Terms and Conditions (\"Terms\") constitute a legally binding agreement between you (whether as an Job Seeker, Employer, or general visitor) and PartTimeGO Inc. (\"PartTimeGO\", \"we\", \"us\", or \"our\").\n\n" +
                                "These Terms govern your access to and use of the PartTimeGO mobile application, website, related APIs, and match-making services (collectively, the \"Platform\"). If you do not agree with any part of these Terms, you must immediately cease all use of the Platform."
                    )

                    // Section 2
                    TermsSection(
                        title = "2. Nature of Platform & Non-Employment Relationship",
                        content = "PartTimeGO functions purely as a digital venue and information facilitator that connects independent Job Seekers with potential Employers offering short-term, casual, or part-time work opportunities.\n\n" +
                                "• No Direct Employment: PartTimeGO is not a direct employer, labor agency, headhunter, or joint venture. Creating an account or accepting a job through the Platform does NOT create an employer-employee relationship between you and PartTimeGO.\n" +
                                "• Independent Contracts: Any work agreement, verbal or written, formed as a result of a job posting on PartTimeGO is strictly between the Employer and the Applicant/Worker. Both parties are solely responsible for negotiating compensation, work hours, scope, and compliance with local statutory obligations."
                    )

                    // Section 3
                    TermsSection(
                        title = "3. Eligibility & Account Registration",
                        content = "To register and maintain an active account on PartTimeGO, you must satisfy the following criteria:\n\n" +
                                "• Minimum Age: You must be at least 18 years of age, or at least 16 years of age with express consent from a parent or legal guardian in full compliance with Malaysian Employment Act provisions regarding young persons.\n" +
                                "• Legal Capacity: You possess the full legal right, power, and authority to enter into binding legal agreements.\n" +
                                "• Identity Authenticity: You agree to provide accurate, current, and complete personal and contact details during registration. Impersonating another individual, using falsified documents, or maintaining multiple accounts to evade platform bans is strictly prohibited and subject to immediate legal prosecution."
                    )

                    // Section 4
                    TermsSection(
                        title = "4. Employer Guidelines & Posting Rules",
                        content = "Employers posting job listings on PartTimeGO agree to comply with all applicable local employment regulations and the following rules:\n\n" +
                                "• Accuracy & Truthfulness: All job details—including pay rates, working hours, exact physical address, attire requirements, and duties—must be clearly and accurately described.\n" +
                                "• Minimum Wages: Advertised wages must meet or exceed the national or local minimum wage requirements enforced in the operating jurisdiction.\n" +
                                "• Prohibited Postings: Employers shall NOT post listings that involve:\n" +
                                "   a) Illegal activities, multi-level marketing (MLM), pyramid schemes, or upfront fee collection from candidates;\n" +
                                "   b) Adult entertainment, illegal gambling, or sexually suggestive roles;\n" +
                                "   c) Unsafe or hazardous working environments violating safety codes;\n" +
                                "   d) Discriminatory criteria based on race, religion, gender, disability, or marital status.\n" +
                                "• Non-Payment Penalties: Employers who fail to pay worker fees upon satisfactory completion of work will face permanent blacklisting, forfeiture of deposits, and report to regulatory authorities."
                    )

                    // Section 5
                    TermsSection(
                        title = "5. Applicant Obligations & Attendance Policy",
                        content = "Job Seekers who apply for and accept part-time assignments through PartTimeGO agree to uphold professional conduct:\n\n" +
                                "• Punctuality & Fulfillment: Workers must report to the assigned location on time and complete duties with reasonable care and skill.\n" +
                                "• Cancellation & No-Show (\"Pigeon\") Policy:\n" +
                                "   a) If an applicant cannot attend a confirmed shift, they must cancel through the App at least 12 hours prior to start time.\n" +
                                "   b) Unexcused no-shows or last-minute cancellations without valid medical/emergency proof will result in immediate rating deduction.\n" +
                                "   c) Accumulating 2 no-show incidents will result in temporary suspension, and 3 incidents will lead to permanent account termination.\n" +
                                "• Professional Ethics: Workers shall not steal, damage employer property, disclose confidential business information, or perform work under the influence of alcohol or illegal substances."
                    )

                    // Section 6
                    TermsSection(
                        title = "6. Wages, Fees & Payment Terms",
                        content = "• Direct Settlement: Unless PartTimeGO explicitly introduces an in-app Escrow Payment System, wage payment arrangements are negotiated directly between the Employer and the Worker (e.g., Cash on Delivery, Instant Bank Transfer, or Touch 'n Go eWallet).\n" +
                                "• Service Charges: PartTimeGO reserves the right to introduce platform commission fees, subscription fees for premium job posts, or processing charges with prior advance notice to users.\n" +
                                "• Dispute Support: If a dispute arises over unpaid wages or incomplete work, users may submit evidence via the 'Contact Us' portal. PartTimeGO will conduct an internal review and may restrict the offending party's access."
                    )

                    // Section 7
                    TermsSection(
                        title = "7. Prohibited Uses & System Integrity",
                        content = "You expressly agree not to engage in any of the following prohibited behaviors:\n\n" +
                                "• Using automated scrapers, bots, or data mining tools to extract user profiles or job postings.\n" +
                                "• Bypassing or attempting to manipulate PartTimeGO's user rating and review system.\n" +
                                "• Sending unsolicited spam, promotional material, or off-platform harassment to other registered users.\n" +
                                "• Decompiling, reverse engineering, or attempting to compromise the security features of the PartTimeGO mobile application."
                    )

                    // Section 8
                    TermsSection(
                        title = "8. Intellectual Property Rights",
                        content = "All content, trademarks, logos, graphical interfaces, code, and documentation associated with PartTimeGO belong exclusively to PartTimeGO Inc. or its licensors. Users are granted a limited, non-exclusive, non-transferable license to access and use the Platform solely for personal job search or recruitment purposes."
                    )

                    // Section 9
                    TermsSection(
                        title = "9. Limitation of Liability & Disclaimers",
                        content = "• AS-IS Basis: The Platform is provided on an \"AS IS\" and \"AS AVAILABLE\" basis without warranties of any kind, either express or implied.\n" +
                                "• Workplace Incidents & Safety: PartTimeGO assumes NO liability for any personal injury, bodily harm, property damage, theft, verbal abuse, or statutory violations occurring during or as a result of a job match.\n" +
                                "• Financial Loss: PartTimeGO shall not be liable for direct, indirect, incidental, or consequential damages resulting from lost wages, system outages, data loss, or employer insolvency."
                    )

                    // Section 10
                    TermsSection(
                        title = "10. Indemnification",
                        content = "You agree to defend, indemnify, and hold harmless PartTimeGO, its officers, directors, employees, and agents from and against any claims, damages, obligations, losses, liabilities, costs, or debt (including attorney's fees) arising from:\n\n" +
                                "• Your breach of any provision in these Terms;\n" +
                                "• Your violation of any third-party rights, including employment rights, privacy, or intellectual property;\n" +
                                "• Any dispute between you and another registered user of the Platform."
                    )

                    // Section 11
                    TermsSection(
                        title = "11. Account Termination & Suspension",
                        content = "PartTimeGO reserves the right, in its sole discretion and without prior notice, to suspend, terminate, or delete any user account if we determine or suspect that:\n\n" +
                                "• You have violated any section of these Terms or local laws;\n" +
                                "• Your account has received multiple verified user complaints regarding fraud, safety hazards, or non-payment;\n" +
                                "• Your account has remained inactive for an extended period exceeding 12 months."
                    )

                    // Section 12
                    TermsSection(
                        title = "12. Governing Law & Dispute Resolution",
                        content = "These Terms shall be governed by and construed in accordance with the laws of Malaysia, without regard to its conflict of law principles. Any legal suit, action, or proceeding arising out of or related to these Terms shall be instituted exclusively in the courts of Malaysia."
                    )

                    // Section 13
                    TermsSection(
                        title = "13. Modifications to Terms & Contact Information",
                        content = "PartTimeGO reserves the right to modify or replace these Terms at any time. We will notify users of significant material changes via in-app announcements or email. Continued usage of the App after modifications implies full acceptance of the updated Terms.\n\n" +
                                "If you have questions, feedback, or need to report illegal activity on the platform, please contact our support team via:\n" +
                                "• WhatsApp Support: +60 11-3953 9985\n" +
                                "• Support Email: support@parttimego.com"
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun TermsSection(
    title: String,
    content: String
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = DarkNavy
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            fontSize = 13.sp,
            color = Color(0xFF475569),
            lineHeight = 20.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TermsAndConditionsScreenPreview() {
    PartTimeGOTheme {
        TermsAndConditionsScreen()
    }
}