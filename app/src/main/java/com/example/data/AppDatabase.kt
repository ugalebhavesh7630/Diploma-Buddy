package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// ROOM ENTITIES
// ==========================================

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val emailOrMobile: String,
    val fullName: String,
    val mobileNumber: String,
    val email: String,
    val collegeName: String,
    val branch: String,
    val profilePhotoUrl: String = "",
    val language: String = "English", // English, Marathi, Hindi
    val themeMode: String = "System", // Light, Dark, System
    val isNotificationEnabled: Boolean = true,
    val enableScholarshipNotif: Boolean = true,
    val enableResultNotif: Boolean = true,
    val enableTimetableNotif: Boolean = true,
    val enableMsbteNotif: Boolean = true,
    val enableBranchNotif: Boolean = true,
    val downloadOnlyOnWifi: Boolean = false,
    val autoDownload: Boolean = false
)

@Entity(tableName = "branch_contents")
data class BranchContentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val branch: String,               // Mechanical, Civil, Computer Engineering
    val category: String,             // Notes, Study Materials, PYQP, Assignment, Lab Manual
    val title: String,
    val subject: String,
    val contentText: String,          // Short preview or mock page content
    val downloadUrl: String = "",
    val viewCount: Int = 0,
    val downloadCount: Int = 0,
    val isBookmarked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,             // MSBTE Circular, Notice, Scholarship, Announcement, Timetable, Result
    val title: String,
    val description: String,
    val eligibility: String = "",     // For scholarships
    val lastDate: String = "",        // Scholarship or timetable target date
    val officialLink: String = "",
    val branch: String = "All",       // Branch target (All, Computer, Civil, Mechanical)
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val branch: String,               // General, Mechanical, Civil, Computer
    val category: String,             // Doubt, Discussion
    val authorName: String,
    val authorCollege: String,
    val content: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val refType: String,              // post, notice, content
    val refId: Int,                   // Connected ID
    val parentCommentId: Int? = null, // Nested comments support
    val authorName: String,
    val content: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "in_app_notifications")
data class InAppNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,             // Notes, Scholarship, Results, Circulars
    val title: String,
    val message: String,
    val branch: String = "All",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

// ==========================================
// ROOM DAOS
// ==========================================

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getActiveUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getActiveUserSync(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearUser()
}

@Dao
interface BranchContentDao {
    @Query("SELECT * FROM branch_contents ORDER BY timestamp DESC")
    fun getAllContents(): Flow<List<BranchContentEntity>>

    @Query("SELECT * FROM branch_contents WHERE branch = :branch ORDER BY timestamp DESC")
    fun getContentsByBranch(branch: String): Flow<List<BranchContentEntity>>

    @Query("SELECT * FROM branch_contents WHERE branch = :branch AND category = :category ORDER BY timestamp DESC")
    fun getBranchCategoryContents(branch: String, category: String): Flow<List<BranchContentEntity>>

    @Query("SELECT * FROM branch_contents WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedContents(): Flow<List<BranchContentEntity>>

    @Query("SELECT * FROM branch_contents WHERE id = :id")
    suspend fun getContentById(id: Int): BranchContentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: BranchContentEntity)

    @Query("UPDATE branch_contents SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun updateBookmark(id: Int, bookmarked: Boolean)

    @Query("UPDATE branch_contents SET downloadCount = downloadCount + 1, isBookmarked = 1 WHERE id = :id")
    suspend fun simulateDownload(id: Int)

    @Delete
    suspend fun deleteContent(content: BranchContentEntity)
}

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY isPinned DESC, timestamp DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Query("SELECT * FROM notices WHERE category = :category ORDER BY isPinned DESC, timestamp DESC")
    fun getNoticesByCategory(category: String): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNoticeById(id: Int)
}

@Dao
interface CommunityPostDao {
    @Query("SELECT * FROM community_posts WHERE branch = :branch OR branch = 'General' ORDER BY isPinned DESC, timestamp DESC")
    fun getPostsByBranch(branch: String): Flow<List<CommunityPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPostEntity)

    @Query("UPDATE community_posts SET likesCount = likesCount + :delta, isLiked = :isLiked WHERE id = :id")
    suspend fun updateLike(id: Int, delta: Int, isLiked: Boolean)

    @Delete
    suspend fun deletePost(post: CommunityPostEntity)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE refType = :refType AND refId = :refId ORDER BY timestamp ASC")
    fun getComments(refType: String, refId: Int): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :id")
    suspend fun deleteCommentById(id: Int)

    @Query("UPDATE comments SET likesCount = likesCount + :delta, isLiked = :isLiked WHERE id = :id")
    suspend fun updateCommentLike(id: Int, delta: Int, isLiked: Boolean)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM in_app_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<InAppNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notif: InAppNotificationEntity)

    @Query("UPDATE in_app_notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM in_app_notifications")
    suspend fun clearAllNotifications()
}

// ==========================================
// DATABASE INITIALIZATION
// ==========================================

@Database(
    entities = [
        UserEntity::class,
        BranchContentEntity::class,
        NoticeEntity::class,
        CommunityPostEntity::class,
        CommentEntity::class,
        InAppNotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun branchContentDao(): BranchContentDao
    abstract fun noticeDao(): NoticeDao
    abstract fun communityPostDao(): CommunityPostDao
    abstract fun commentDao(): CommentDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diploma_student_helper_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
