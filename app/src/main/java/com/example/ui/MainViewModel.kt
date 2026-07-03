package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db)

    // UI & Language State
    private val _language = MutableStateFlow("English")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow("System")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // Query Filters & State
    private val _selectedBranch = MutableStateFlow("Computer Engineering")
    val selectedBranch: StateFlow<String> = _selectedBranch.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeUser = MutableStateFlow<UserEntity?>(null)
    val activeUser: StateFlow<UserEntity?> = _activeUser.asStateFlow()

    // Data lists observed reactively
    val allBranchContents: StateFlow<List<BranchContentEntity>> = repository.allBranchContents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedContents: StateFlow<List<BranchContentEntity>> = repository.bookmarkedContents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotices: StateFlow<List<NoticeEntity>> = repository.allNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _communityPosts = MutableStateFlow<List<CommunityPostEntity>>(emptyList())
    val communityPosts: StateFlow<List<CommunityPostEntity>> = _communityPosts.asStateFlow()

    val allNotifications: StateFlow<List<InAppNotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Seed content database on startup if empty
            repository.seedMockDataIfEmpty()
            
            // Observe branch and collect community posts
            launch {
                _selectedBranch.collect { branch ->
                    repository.getPostsByBranch(branch).collect { posts ->
                        _communityPosts.value = posts
                    }
                }
            }
            
            // Observe logged in user state
            repository.activeUser.collect { user ->
                _activeUser.value = user
                if (user != null) {
                    _language.value = user.language
                    _themeMode.value = user.themeMode
                    _selectedBranch.value = user.branch
                }
            }
        }
    }

    // ==========================================
    // MULTI-LANGUAGE HELPER MAPPING
    // ==========================================
    fun translate(key: String): String {
        return Localization.getString(key, _language.value)
    }

    // ==========================================
    // AUTHENTICATION LOGIC (MOCK REAL FLOWS)
    // ==========================================
    fun performLogin(emailOrMobile: String, loginType: String) {
        viewModelScope.launch {
            // Check if user already exists
            val existing = repository.getActiveUserSync()
            if (existing != null && existing.emailOrMobile == emailOrMobile) {
                // Keep existing user and log in
            } else {
                // Register a mock user with clean starter presets matching the login identifier
                val tempUser = UserEntity(
                    emailOrMobile = emailOrMobile,
                    fullName = if (loginType == "Google") "Prof. Amit Deshmukh" else "Bhavesh Ugale",
                    mobileNumber = if (loginType == "Mobile") emailOrMobile else "9854736251",
                    email = if (loginType == "Email") emailOrMobile else "ugalebhavesh7630@gmail.com",
                    collegeName = "Government Polytechnic, Pune",
                    branch = "Computer Engineering",
                    profilePhotoUrl = "https://example.com/avatar1.png"
                )
                repository.saveUser(tempUser)
            }
        }
    }

    fun performCustomRegister(
        name: String,
        email: String,
        mobile: String,
        college: String,
        branch: String
    ) {
        viewModelScope.launch {
            val user = UserEntity(
                emailOrMobile = email.ifEmpty { mobile },
                fullName = name,
                email = email,
                mobileNumber = mobile,
                collegeName = college,
                branch = branch
            )
            repository.saveUser(user)
        }
    }

    fun modifyProfile(
        name: String,
        email: String,
        mobile: String,
        college: String,
        branch: String
    ) {
        viewModelScope.launch {
            val currentUser = _activeUser.value ?: return@launch
            val updatedUser = currentUser.copy(
                fullName = name,
                email = email,
                mobileNumber = mobile,
                collegeName = college,
                branch = branch
            )
            repository.saveUser(updatedUser)
        }
    }

    fun changeLanguage(lang: String) {
        _language.value = lang
        viewModelScope.launch {
            val user = _activeUser.value ?: return@launch
            repository.saveUser(user.copy(language = lang))
        }
    }

    fun changeThemeMode(mode: String) {
        _themeMode.value = mode
        viewModelScope.launch {
            val user = _activeUser.value ?: return@launch
            repository.saveUser(user.copy(themeMode = mode))
        }
    }

    fun changeSelectedBranch(branch: String) {
        _selectedBranch.value = branch
        viewModelScope.launch {
            val user = _activeUser.value ?: return@launch
            repository.saveUser(user.copy(branch = branch))
        }
    }

    fun toggleSetting(settingKey: String, value: Boolean) {
        viewModelScope.launch {
            val user = _activeUser.value ?: return@launch
            val updated = when (settingKey) {
                "notifications" -> user.copy(isNotificationEnabled = value)
                "scholarship" -> user.copy(enableScholarshipNotif = value)
                "result" -> user.copy(enableResultNotif = value)
                "timetable" -> user.copy(enableTimetableNotif = value)
                "msbte" -> user.copy(enableMsbteNotif = value)
                "branch" -> user.copy(enableBranchNotif = value)
                "wifi" -> user.copy(downloadOnlyOnWifi = value)
                "autodownload" -> user.copy(autoDownload = value)
                else -> user
            }
            repository.saveUser(updated)
        }
    }

    fun setAdminMode(active: Boolean) {
        _isAdminMode.value = active
    }

    fun logout() {
        viewModelScope.launch {
            _isAdminMode.value = false
            repository.clearUser()
        }
    }

    // ==========================================
    // SEARCH & FILTER SYSTEM
    // ==========================================
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // ==========================================
    // BRANCH MATERIAL & DOWNLOADS
    // ==========================================
    fun toggleBookmark(id: Int, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.updateBookmark(id, isBookmarked)
        }
    }

    fun simulateDownloadFile(id: Int) {
        viewModelScope.launch {
            repository.simulateDownload(id)
        }
    }

    // ==========================================
    // COMMUNITY POSTS & REPLIES
    // ==========================================
    fun addNewPost(content: String, category: String) {
        viewModelScope.launch {
            val user = _activeUser.value
            val authorName = user?.fullName ?: "Anonymous Diploma"
            val authorCol = user?.collegeName ?: "GP Pune"
            val newPost = CommunityPostEntity(
                branch = _selectedBranch.value,
                category = category, // Doubt, Discussion
                authorName = authorName,
                authorCollege = authorCol,
                content = content
            )
            repository.insertPost(newPost)
            
            // Add internal notification triggers for community (useful to demo interactive app)
            repository.insertNotification(
                InAppNotificationEntity(
                    category = "Forum",
                    title = "New post in ${_selectedBranch.value}!",
                    message = "$authorName posted in community discussions: \"${content.take(45)}...\"",
                    branch = _selectedBranch.value
                )
            )
        }
    }

    fun togglePostLike(postId: Int, currentlyLiked: Boolean) {
        viewModelScope.launch {
            val delta = if (currentlyLiked) -1 else 1
            repository.updateLike(postId, delta, !currentlyLiked)
        }
    }

    fun deletePost(post: CommunityPostEntity) {
        viewModelScope.launch {
            repository.deletePost(post)
        }
    }

    // Comments query helper (Linear and Nested replies simulator)
    fun getCommentsForPost(refType: String, refId: Int): Flow<List<CommentEntity>> {
        return repository.getComments(refType, refId)
    }

    fun addComment(refType: String, refId: Int, content: String, parentId: Int? = null) {
        viewModelScope.launch {
            val author = _activeUser.value?.fullName ?: "Guest Student"
            val comment = CommentEntity(
                refType = refType,
                refId = refId,
                parentCommentId = parentId,
                authorName = author,
                content = content
            )
            repository.insertComment(comment)
        }
    }

    fun toggleCommentLike(commentId: Int, currentlyLiked: Boolean) {
        viewModelScope.launch {
            val delta = if (currentlyLiked) -1 else 1
            repository.updateCommentLike(commentId, delta, !currentlyLiked)
        }
    }

    fun deleteComment(id: Int) {
        viewModelScope.launch {
            repository.deleteCommentById(id)
        }
    }

    // ==========================================
    // ADMIN FUNCTIONS
    // ==========================================
    fun uploadMaterialsByAdmin(
        branch: String,
        category: String,
        title: String,
        subject: String,
        contentText: String,
        downloadUrl: String
    ) {
        viewModelScope.launch {
            val content = BranchContentEntity(
                branch = branch,
                category = category,
                title = title,
                subject = subject,
                contentText = contentText,
                downloadUrl = downloadUrl
            )
            repository.insertContent(content)

            // Trigger notification
            repository.insertNotification(
                InAppNotificationEntity(
                    category = "Notes",
                    title = "New $category Uploaded",
                    message = "$title for $subject ($branch) is now active.",
                    branch = branch
                )
            )
        }
    }

    fun createNoticeByAdmin(
        category: String,
        title: String,
        description: String,
        eligibility: String,
        lastDate: String,
        link: String,
        branch: String,
        isPinned: Boolean
    ) {
        viewModelScope.launch {
            val notice = NoticeEntity(
                category = category,
                title = title,
                description = description,
                eligibility = eligibility,
                lastDate = lastDate,
                officialLink = link,
                branch = branch,
                isPinned = isPinned
            )
            repository.insertNotice(notice)

            // Trigger alert log
            repository.insertNotification(
                InAppNotificationEntity(
                    category = category,
                    title = "Notice: $title",
                    message = description.take(60) + "...",
                    branch = branch
                )
            )
        }
    }

    fun deleteNotice(id: Int) {
        viewModelScope.launch {
            repository.deleteNoticeById(id)
        }
    }

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }
}
