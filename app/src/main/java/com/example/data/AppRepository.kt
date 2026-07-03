package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {

    // User Operations
    val activeUser: Flow<UserEntity?> = db.userDao().getActiveUser()
    suspend fun getActiveUserSync() = db.userDao().getActiveUserSync()
    suspend fun saveUser(user: UserEntity) = db.userDao().saveUser(user)
    suspend fun clearUser() = db.userDao().clearUser()

    // Branch Contents
    val allBranchContents: Flow<List<BranchContentEntity>> = db.branchContentDao().getAllContents()
    val bookmarkedContents: Flow<List<BranchContentEntity>> = db.branchContentDao().getBookmarkedContents()

    fun getContentsByBranch(branch: String): Flow<List<BranchContentEntity>> = 
        db.branchContentDao().getContentsByBranch(branch)

    fun getBranchCategoryContents(branch: String, category: String): Flow<List<BranchContentEntity>> =
        db.branchContentDao().getBranchCategoryContents(branch, category)

    suspend fun getContentById(id: Int) = db.branchContentDao().getContentById(id)
    suspend fun insertContent(content: BranchContentEntity) = db.branchContentDao().insertContent(content)
    suspend fun updateBookmark(id: Int, bookmarked: Boolean) = db.branchContentDao().updateBookmark(id, bookmarked)
    suspend fun simulateDownload(id: Int) = db.branchContentDao().simulateDownload(id)
    suspend fun deleteContent(content: BranchContentEntity) = db.branchContentDao().deleteContent(content)

    // Notices & Scholarships
    val allNotices: Flow<List<NoticeEntity>> = db.noticeDao().getAllNotices()
    fun getNoticesByCategory(category: String): Flow<List<NoticeEntity>> = 
        db.noticeDao().getNoticesByCategory(category)
    suspend fun insertNotice(notice: NoticeEntity) = db.noticeDao().insertNotice(notice)
    suspend fun deleteNoticeById(id: Int) = db.noticeDao().deleteNoticeById(id)

    // Discussions
    fun getPostsByBranch(branch: String): Flow<List<CommunityPostEntity>> = 
        db.communityPostDao().getPostsByBranch(branch)
    suspend fun insertPost(post: CommunityPostEntity) = db.communityPostDao().insertPost(post)
    suspend fun updateLike(id: Int, delta: Int, isLiked: Boolean) = db.communityPostDao().updateLike(id, delta, isLiked)
    suspend fun deletePost(post: CommunityPostEntity) = db.communityPostDao().deletePost(post)

    // Comments
    fun getComments(refType: String, refId: Int): Flow<List<CommentEntity>> = 
        db.commentDao().getComments(refType, refId)
    suspend fun insertComment(comment: CommentEntity) = db.commentDao().insertComment(comment)
    suspend fun deleteCommentById(id: Int) = db.commentDao().deleteCommentById(id)
    suspend fun updateCommentLike(id: Int, delta: Int, isLiked: Boolean) = db.commentDao().updateCommentLike(id, delta, isLiked)

    // Notifications
    val allNotifications: Flow<List<InAppNotificationEntity>> = db.notificationDao().getAllNotifications()
    suspend fun insertNotification(notif: InAppNotificationEntity) = db.notificationDao().insertNotification(notif)
    suspend fun markAllAsRead() = db.notificationDao().markAllAsRead()
    suspend fun clearAllNotifications() = db.notificationDao().clearAllNotifications()

    // Database Seeding
    suspend fun seedMockDataIfEmpty() {
        // Only seed if branch content is empty
        val userCheck = db.userDao().getActiveUserSync()
        // We'll proceed with checking branch contents
        val existingContentsCount = db.branchContentDao().getContentById(1)
        if (existingContentsCount == null) {
            // Seed Branch Contents
            val contents = listOf(
                // Mechanical Engineering
                BranchContentEntity(
                    branch = "Mechanical Engineering",
                    category = "Notes",
                    title = "Thermal Engineering Thermodynamics Principles",
                    subject = "Thermal Engineering (TEN)",
                    contentText = "Thermodynamics is the branch of physics that deals with the relationships between heat and other forms of energy. In particular, it describes how thermal energy is converted to and from other forms of energy and how it affects matter.\n\nFirst Law of Thermodynamics:\nEnergy can neither be created nor destroyed; it can only change form.",
                    downloadUrl = "https://example.com/me_thermal_notes.pdf"
                ),
                BranchContentEntity(
                    branch = "Mechanical Engineering",
                    category = "Previous Year Papers",
                    title = "Theory of Machines (TOM) Summer 2025 Paper",
                    subject = "Theory of Machines (TOM)",
                    contentText = "MSBTE Diploma - Summer 2025 Question Paper.\n\nAnswer any 5 questions:\n1. Describe inversion of four-bar chain with sketches.\n2. State and prove Kennedy's theorem of three centers in plane motion.\n3. Derive an expression for the length of path of contact for spur gears.",
                    downloadUrl = "https://example.com/me_tom_2025.pdf"
                ),
                BranchContentEntity(
                    branch = "Mechanical Engineering",
                    category = "Lab Manuals",
                    title = "Fluid Mechanics and Machinery Practical Manual",
                    subject = "Fluid Mechanics (FME)",
                    contentText = "Experiment No 1: Verification of Bernoulli's Theorem.\n\nApparatus: Bernoulli's test rig, stop watch, collecting tank.\n\nTheory: According to Bernoulli's theorem, for a steady, incompressible flow of a non-viscous fluid, the total energy per unit mass is constant.",
                    downloadUrl = "https://example.com/me_fm_manual.pdf"
                ),
                
                // Civil Engineering
                BranchContentEntity(
                    branch = "Civil Engineering",
                    category = "Notes",
                    title = "Reinforced Concrete Structures (RCS) design guide",
                    subject = "Design of Steel and RC Structures (DSR)",
                    contentText = "Design Philosophies:\n1. Working Stress Method (WSM)\n2. Limit State Method (LSM)\n\nLSM is based on limit states of collapse and serviceability. Major stress-strain parameters are drawn from IS 456:2000.",
                    downloadUrl = "https://example.com/ce_rcs_notes.pdf"
                ),
                BranchContentEntity(
                    branch = "Civil Engineering",
                    category = "Previous Year Papers",
                    title = "Geotechnical Engineering Winter 2024 paper",
                    subject = "Geotechnical Engineering (GTE)",
                    contentText = "MSBTE Question Paper - Winter 2024.\n\nSection A:\n1. Define void ratio, porosity, and degree of saturation.\n2. Differentiate between compaction and consolidation.\n3. Draw a neat labeled three-phase soil diagram.",
                    downloadUrl = "https://example.com/ce_gte_2024.pdf"
                ),
                BranchContentEntity(
                    branch = "Civil Engineering",
                    category = "Assignments",
                    title = "Highway Engineering Curve Design",
                    subject = "Highway Engineering (HEN)",
                    contentText = "CIVIL SEM-V Assignment.\n\nProblem Statement: Design an extra widening for a state highway of 2-lane road on horizontal curve of radius 250m. Design speed is 80kmph.",
                    downloadUrl = "https://example.com/ce_highway_assign.pdf"
                ),

                // Computer Engineering
                BranchContentEntity(
                    branch = "Computer Engineering",
                    category = "Notes",
                    title = "Mobile Application Development using Kotlin",
                    subject = "Mobile App Development (MAD)",
                    contentText = "Jetpack Compose is Android's modern toolkit for building native UI. It simplifies and accelerates UI development on Android. Write less code, use declarative paradigms, and build super robust applications.\n\nImportant Topics:\n- State & Recomposition\n- Remember & MuatbleState\n- Modifier styling parameters.",
                    downloadUrl = "https://example.com/co_mad_kot_notes.pdf"
                ),
                BranchContentEntity(
                    branch = "Computer Engineering",
                    category = "Study Materials",
                    title = "Client Side Scripting using JavaScript",
                    subject = "Client Side Scripting (CSS)",
                    contentText = "Module 1: Intro to JavaScript\nFunctions, Prototypes, closures, arrow notation, DOM manipulation methods, event handling and JSON formatting specifications.",
                    downloadUrl = "https://example.com/co_css_js_guide.pdf"
                ),
                BranchContentEntity(
                    branch = "Computer Engineering",
                    category = "Lab Manuals",
                    title = "Database Management Practicals using SQL",
                    subject = "Database Management System (DMS)",
                    contentText = "Experiment No 3: Writing DDL and DML queries.\n\nTask: Create tables (Student, Teacher, Result) with primary key and foreign key constraints. Perform JOIN operations.",
                    downloadUrl = "https://example.com/co_dms_sql_manual.pdf"
                ),
                BranchContentEntity(
                    branch = "Computer Engineering",
                    category = "Previous Year Papers",
                    title = "Software Engineering Summer 2025 Paper",
                    subject = "Software Engineering (SEN)",
                    contentText = "MSBTE Diploma - Summer 2025 Question Paper.\n\nWrite detailed answers:\n1. Explain Agile development lifecycle model.\n2. Draw Use-Case diagram for Library Management System.\n3. Differentiate between White-Box and Black-Box testing.",
                    downloadUrl = "https://example.com/co_sen_2025.pdf"
                )
            )
            for (c in contents) {
                db.branchContentDao().insertContent(c)
            }

            // Seed Notice / Announcements / Scholarships
            val notices = listOf(
                NoticeEntity(
                    category = "Scholarship",
                    title = "MAHDBT Rajarshi Shahu Maharaj Scholarship 2026",
                    description = "Applications open for the Rajarshi Chhatrapati Shahu Maharaj Shikshan Shulkh Shishyavrutti Scheme. Full tuition fees and exam fees reimbursement for eligible students.",
                    eligibility = "Annual Family income below ₹8 Lakhs. Minimum 60% in previous semester. Admitted under CAP rounds.",
                    lastDate = "31st August 2026",
                    officialLink = "https://mahadbt.maharashtra.gov.in/",
                    isPinned = true
                ),
                NoticeEntity(
                    category = "Scholarship",
                    title = "Tata Trust Millennium Fellowship Scheme 2026",
                    description = "Merit-based scholarships to support engineering diploma scholars belonging to underprivileged backgrounds.",
                    eligibility = "Open to 1st and 2nd year mechanical, civil and computer diploma students. Minimum score of 7.5 CGPA required.",
                    lastDate = "15th July 2026",
                    officialLink = "https://www.tatatrusts.org/",
                    isPinned = false
                ),
                NoticeEntity(
                    category = "MSBTE Circular",
                    title = "MSBTE Winter 2026 Practical & Theory Timetable",
                    description = "Maharashtra State Board of Technical Education (MSBTE) has official published tentative schedule. Practical exams will commence from November 1st, while Theory board exams will start from November 28th.",
                    lastDate = "Theory: 28 Nov, Pract: 1 Nov",
                    officialLink = "https://msbte.org.in/",
                    isPinned = true
                ),
                NoticeEntity(
                    category = "Notice",
                    title = "Important: College ID Cards and Fee Dues",
                    description = "All students are requested to clear academic term fee dues before June 30, and collect hall tickets from administrative counter.",
                    lastDate = "30th June 2026",
                    isPinned = false
                ),
                NoticeEntity(
                    category = "Result",
                    title = "MSBTE Summer 2026 Alternate Results Declared",
                    description = "Diploma Summer 2026 provisional marksheets and overall results have been officially declared on msbte.org.in. Enter Enrollment number to view result.",
                    lastDate = "Available Now",
                    officialLink = "https://msbte.org.in/",
                    isPinned = true
                )
            )
            for (n in notices) {
                db.noticeDao().insertNotice(n)
            }

            // Seed Community posts
            val posts = listOf(
                CommunityPostEntity(
                    branch = "Computer Engineering",
                    category = "Doubt",
                    authorName = "Aditya Kulkarni",
                    authorCollege = "Government Polytechnic, Mumbai",
                    content = "Hey everyone! Can someone share tips on working with Jetpack Compose LazyColumns? How do I pass custom state indices without triggering useless recompositions?",
                    likesCount = 14,
                    isLiked = false
                ),
                CommunityPostEntity(
                    branch = "General",
                    category = "Discussion",
                    authorName = "Prof. S. R. Patil",
                    authorCollege = "V.P. Polytechnic, Pune",
                    content = "MSBTE has strictly emphasized completing lab journals on time this semester. Make sure your mechanical models and computer programs are verified by respective lab heads.",
                    likesCount = 38,
                    isLiked = false,
                    isPinned = true
                ),
                CommunityPostEntity(
                    branch = "Civil Engineering",
                    category = "Doubt",
                    authorName = "Tejas Shinde",
                    authorCollege = "MSPW Polytechnic, Nagpur",
                    content = "Having a hard time plotting vertical curves for highway project design assignments. Which methods does IS code recommend?",
                    likesCount = 8,
                    isLiked = false
                )
            )
            for (p in posts) {
                db.communityPostDao().insertPost(p)
            }

            // Seed default alert logs
            val alerts = listOf(
                InAppNotificationEntity(
                    category = "Circulars",
                    title = "MSBTE Winter Timetable Released",
                    message = "Official Winter exam schedule has been released. Practical exams begin November 1st.",
                    branch = "All"
                ),
                InAppNotificationEntity(
                    category = "Scholarship",
                    title = "Rajarshi Shahu Maharaj open on Mahadbt",
                    message = "Go ahead and apply for Maharashtra government tuition rebates.",
                    branch = "All"
                )
            )
            for (a in alerts) {
                db.notificationDao().insertNotification(a)
            }
        }
    }
}
