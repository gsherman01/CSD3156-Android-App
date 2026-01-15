# Member 3: Database & Persistence - Task List

**Role:** Database & Persistence Engineer
**Team Member:** [Your Name]
**Project:** TinyCell (CSD3156 Android App)

---

## 📊 Overview

**Responsibilities:**
- Room database schema design
- DAO (Data Access Object) implementation
- Entity relationships and foreign keys
- Repository layer integration
- Favourite system implementation
- Local data caching strategy

**Current Status:** Database scaffolded but incomplete
**Priority:** HIGH - Core functionality depends on database

---

## ✅ Task Checklist

### Phase 1: Database Entities (Priority: HIGH)

- [ ] **Task 1:** Review current database files
- [ ] **Task 2:** Define User entity with Room annotations
- [ ] **Task 3:** Define Listing entity with relationships
- [ ] **Task 4:** Define Category entity
- [ ] **Task 5:** Define Favourite entity with foreign keys
- [ ] **Task 6:** Define ChatMessage entity with relationships

### Phase 2: Data Access Objects (Priority: HIGH)

- [ ] **Task 7:** Create UserDao with CRUD operations
- [ ] **Task 8:** Create ListingDao with advanced queries
- [ ] **Task 9:** Create CategoryDao
- [ ] **Task 10:** Create FavouriteDao with JOIN queries
- [ ] **Task 11:** Create ChatMessageDao with conversation queries

### Phase 3: Database Configuration (Priority: HIGH)

- [ ] **Task 12:** Update AppDatabase to include all entities and DAOs

### Phase 4: Repository Layer (Priority: CRITICAL)

- [ ] **Task 13:** Update ListingRepository to use Room
- [ ] **Task 14:** Create UserRepository
- [ ] **Task 15:** Create FavouriteRepository

### Phase 5: Testing & Data (Priority: MEDIUM)

- [ ] **Task 16:** Seed database with sample data
- [ ] **Task 17:** Test all database operations

---

## 📝 Detailed Tasks

### Task 1: Review Current Database Files
**Priority:** HIGH
**Time:** 15 minutes

**Files to Review:**
```
- data/local/AppDatabase.kt
- data/local/dao/AppDao.kt
- data/local/entity/AppEntity.kt
- data/repository/ListingRepository.kt
- data/model/User.kt
- data/model/Listing.kt
- data/model/Category.kt
```

**Goal:** Understand what's already scaffolded and what needs to be built

**Action Items:**
1. Read through each file
2. Note what's implemented vs. what's placeholder
3. Identify dependencies
4. Check current mock data structure in ListingRepository

---

### Task 2: Define User Entity
**Priority:** HIGH
**Time:** 30 minutes

**File to Create:**
```
data/local/entity/UserEntity.kt
```

**Requirements:**
```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val profilePicUrl: String?,
    val createdAt: Long
)
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Create a Room entity for User.

Requirements:
- Table name: "users"
- Fields: id (String, primary key), name, email, profilePicUrl (nullable), createdAt (Long timestamp)
- Follow existing project structure
- Use proper Room annotations
- No relationships yet

File location: data/local/entity/UserEntity.kt
```

**Acceptance Criteria:**
- [ ] UserEntity.kt created with @Entity annotation
- [ ] All fields have correct types
- [ ] Primary key defined
- [ ] Follows Kotlin naming conventions
- [ ] File compiles without errors

---

### Task 3: Define Listing Entity
**Priority:** HIGH
**Time:** 45 minutes

**File to Create:**
```
data/local/entity/ListingEntity.kt
```

**Requirements:**
```kotlin
@Entity(
    tableName = "listings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"]
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"]
        )
    ],
    indices = [Index("userId"), Index("categoryId")]
)
data class ListingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val userId: String,
    val categoryId: String,
    val location: String?,
    val imageUrls: String, // JSON string or comma-separated
    val createdAt: Long,
    val isSold: Boolean = false
)
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Create a Room entity for Listing with foreign key relationships.

Requirements:
- Table name: "listings"
- Foreign keys to User (userId) and Category (categoryId)
- Indices on userId and categoryId for query performance
- Fields: id, title, description, price, userId, categoryId, location (nullable), imageUrls, createdAt, isSold
- imageUrls stores multiple images (use String for now)
- Follow existing project structure

File location: data/local/entity/ListingEntity.kt
```

**Acceptance Criteria:**
- [ ] ListingEntity.kt created with @Entity annotation
- [ ] Foreign keys properly defined
- [ ] Indices created for query optimization
- [ ] All fields match requirements
- [ ] Compiles without errors

---

### Task 4: Define Category Entity
**Priority:** HIGH
**Time:** 20 minutes

**File to Create:**
```
data/local/entity/CategoryEntity.kt
```

**Requirements:**
```kotlin
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String? // Material icon name or emoji
)
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Create a Room entity for Category.

Requirements:
- Table name: "categories"
- Fields: id (primary key), name, icon (nullable)
- Simple entity, no relationships needed

File location: data/local/entity/CategoryEntity.kt
```

**Acceptance Criteria:**
- [ ] CategoryEntity.kt created
- [ ] Proper @Entity annotation
- [ ] Matches data model structure
- [ ] Compiles without errors

---

### Task 5: Define Favourite Entity
**Priority:** MEDIUM
**Time:** 30 minutes

**File to Create:**
```
data/local/entity/FavouriteEntity.kt
```

**Requirements:**
```kotlin
@Entity(
    tableName = "favourites",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ListingEntity::class,
            parentColumns = ["id"],
            childColumns = ["listingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("listingId"),
        Index(value = ["userId", "listingId"], unique = true)
    ]
)
data class FavouriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val listingId: String,
    val createdAt: Long
)
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Create a Room entity for Favourite (join table).

Requirements:
- Table name: "favourites"
- Foreign keys to User and Listing with CASCADE delete
- Composite unique index on (userId, listingId) to prevent duplicates
- Fields: id (auto-generated), userId, listingId, createdAt
- This is a many-to-many relationship table

File location: data/local/entity/FavouriteEntity.kt
```

**Acceptance Criteria:**
- [ ] FavouriteEntity.kt created
- [ ] Unique constraint on user-listing combination
- [ ] CASCADE delete configured
- [ ] Auto-generated primary key works
- [ ] Compiles without errors

---

### Task 6: Define ChatMessage Entity
**Priority:** MEDIUM
**Time:** 30 minutes

**File to Create:**
```
data/local/entity/ChatMessageEntity.kt
```

**Requirements:**
```kotlin
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["senderId"]
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["receiverId"]
        ),
        ForeignKey(
            entity = ListingEntity::class,
            parentColumns = ["id"],
            childColumns = ["listingId"]
        )
    ],
    indices = [Index("senderId"), Index("receiverId"), Index("listingId")]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val listingId: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
)
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Create a Room entity for ChatMessage.

Requirements:
- Table name: "chat_messages"
- Foreign keys to User (sender and receiver) and Listing
- Indices for query performance
- Fields: id, senderId, receiverId, listingId, message, timestamp, isRead
- Multiple foreign keys to same User table (sender/receiver)

File location: data/local/entity/ChatMessageEntity.kt
```

**Acceptance Criteria:**
- [ ] ChatMessageEntity.kt created
- [ ] Multiple foreign keys to User table handled correctly
- [ ] All indices created
- [ ] isRead flag included
- [ ] Compiles without errors

---

### Task 7: Create UserDao
**Priority:** HIGH
**Time:** 30 minutes

**File to Create:**
```
data/local/dao/UserDao.kt
```

**Requirements:**
```kotlin
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Update
    suspend fun update(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)
}
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Create DAO for UserEntity.

Requirements:
- Interface annotated with @Dao
- CRUD operations: insert, getUserById, getAllUsers, update, delete
- Use suspend functions for single operations
- Use Flow for observable queries
- OnConflictStrategy.REPLACE for insert

File location: data/local/dao/UserDao.kt
```

**Acceptance Criteria:**
- [ ] UserDao.kt created
- [ ] All CRUD operations defined
- [ ] Proper use of suspend and Flow
- [ ] Query syntax correct
- [ ] Compiles without errors

---

### Task 8: Create ListingDao
**Priority:** HIGH
**Time:** 45 minutes

**File to Create:**
```
data/local/dao/ListingDao.kt
```

**Requirements:**
```kotlin
@Dao
interface ListingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(listing: ListingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(listings: List<ListingEntity>)

    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :listingId")
    suspend fun getListingById(listingId: String): ListingEntity?

    @Query("SELECT * FROM listings WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getListingsByCategory(categoryId: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE userId = :userId ORDER BY createdAt DESC")
    fun getListingsByUser(userId: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchListings(query: String): Flow<List<ListingEntity>>

    @Update
    suspend fun update(listing: ListingEntity)

    @Delete
    suspend fun delete(listing: ListingEntity)
}
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Create DAO for ListingEntity with advanced queries.

Requirements:
- CRUD operations
- Query methods: getAllListings, getListingById, getListingsByCategory, getListingsByUser, searchListings
- Order by createdAt DESC for list queries
- Search by title OR description
- Use Flow for observable queries
- insertAll for batch operations

File location: data/local/dao/ListingDao.kt
```

**Acceptance Criteria:**
- [ ] ListingDao.kt created
- [ ] All query methods defined
- [ ] Search functionality included
- [ ] Proper ordering and Flow usage
- [ ] Compiles without errors

---

### Task 9: Create CategoryDao
**Priority:** HIGH
**Time:** 20 minutes

**File to Create:**
```
data/local/dao/CategoryDao.kt
```

**Requirements:**
```kotlin
@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories ORDER BY name")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?
}
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Create DAO for CategoryEntity.

Requirements:
- Basic CRUD operations
- getAllCategories ordered by name
- getCategoryById
- insertAll for bulk insert
- Use Flow and suspend appropriately

File location: data/local/dao/CategoryDao.kt
```

**Acceptance Criteria:**
- [ ] CategoryDao.kt created
- [ ] All operations defined
- [ ] Ordered by name
- [ ] Compiles without errors

---

### Task 10: Create FavouriteDao
**Priority:** MEDIUM
**Time:** 30 minutes

**File to Create:**
```
data/local/dao/FavouriteDao.kt
```

**Requirements:**
```kotlin
@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavourite(favourite: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE userId = :userId AND listingId = :listingId")
    suspend fun removeFavourite(userId: String, listingId: String)

    @Query("SELECT * FROM favourites WHERE userId = :userId")
    fun getUserFavourites(userId: String): Flow<List<FavouriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE userId = :userId AND listingId = :listingId)")
    suspend fun isFavourite(userId: String, listingId: String): Boolean

    @Query("""
        SELECT listings.* FROM listings
        INNER JOIN favourites ON listings.id = favourites.listingId
        WHERE favourites.userId = :userId
        ORDER BY favourites.createdAt DESC
    """)
    fun getUserFavouriteListings(userId: String): Flow<List<ListingEntity>>
}
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Create DAO for FavouriteEntity with join queries.

Requirements:
- addFavourite (IGNORE conflict to handle duplicates)
- removeFavourite by userId and listingId
- getUserFavourites
- isFavourite (boolean check)
- getUserFavouriteListings (JOIN query to get full ListingEntity)
- Use Flow for observable data

File location: data/local/dao/FavouriteDao.kt
```

**Acceptance Criteria:**
- [ ] FavouriteDao.kt created
- [ ] Toggle functionality supported
- [ ] JOIN query works correctly
- [ ] Boolean check method exists
- [ ] Compiles without errors

---

### Task 11: Create ChatMessageDao
**Priority:** MEDIUM
**Time:** 30 minutes

**File to Create:**
```
data/local/dao/ChatMessageDao.kt
```

**Requirements:**
```kotlin
@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Query("""
        SELECT * FROM chat_messages
        WHERE (senderId = :userId1 AND receiverId = :userId2)
           OR (senderId = :userId2 AND receiverId = :userId1)
        ORDER BY timestamp ASC
    """)
    fun getConversation(userId1: String, userId2: String): Flow<List<ChatMessageEntity>>

    @Query("""
        SELECT * FROM chat_messages
        WHERE listingId = :listingId
          AND ((senderId = :userId1 AND receiverId = :userId2)
           OR (senderId = :userId2 AND receiverId = :userId1))
        ORDER BY timestamp ASC
    """)
    fun getListingConversation(listingId: String, userId1: String, userId2: String): Flow<List<ChatMessageEntity>>

    @Query("UPDATE chat_messages SET isRead = 1 WHERE receiverId = :userId AND isRead = 0")
    suspend fun markAllAsRead(userId: String)

    @Query("SELECT COUNT(*) FROM chat_messages WHERE receiverId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>
}
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Create DAO for ChatMessageEntity with conversation queries.

Requirements:
- insert message
- getConversation (bidirectional between two users)
- getListingConversation (scoped to specific listing)
- markAllAsRead (update query)
- getUnreadCount (for badges)
- Order by timestamp ASC for chat history

File location: data/local/dao/ChatMessageDao.kt
```

**Acceptance Criteria:**
- [ ] ChatMessageDao.kt created
- [ ] Bidirectional conversation query works
- [ ] Unread count tracking
- [ ] Mark as read functionality
- [ ] Compiles without errors

---

### Task 12: Update AppDatabase
**Priority:** HIGH
**Time:** 30 minutes

**File to Modify:**
```
data/local/AppDatabase.kt
```

**Requirements:**
```kotlin
@Database(
    entities = [
        UserEntity::class,
        ListingEntity::class,
        CategoryEntity::class,
        FavouriteEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun listingDao(): ListingDao
    abstract fun categoryDao(): CategoryDao
    abstract fun favouriteDao(): FavouriteDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tinycell_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Update AppDatabase to include all entities and DAOs.

Requirements:
- Register all 5 entities: User, Listing, Category, Favourite, ChatMessage
- Provide abstract methods for all 5 DAOs
- Version 1
- Singleton pattern in companion object
- Database name: "tinycell_database"

File location: data/local/AppDatabase.kt
```

**Acceptance Criteria:**
- [ ] All entities registered in @Database annotation
- [ ] All DAOs have abstract methods
- [ ] Singleton pattern implemented correctly
- [ ] Database builds without errors
- [ ] Compiles without errors

---

### Task 13: Update ListingRepository
**Priority:** CRITICAL
**Time:** 45 minutes

**File to Modify:**
```
data/repository/ListingRepository.kt
```

**Current State:** Uses fake in-memory data
**Target State:** Use Room database

**Requirements:**
```kotlin
class ListingRepository(private val listingDao: ListingDao) {

    val allListings: Flow<List<Listing>> = listingDao.getAllListings()
        .map { entities -> entities.map { it.toListing() } }

    suspend fun getListingById(id: String): Listing? {
        return listingDao.getListingById(id)?.toListing()
    }

    fun getListingsByCategory(categoryId: String): Flow<List<Listing>> {
        return listingDao.getListingsByCategory(categoryId)
            .map { entities -> entities.map { it.toListing() } }
    }

    fun searchListings(query: String): Flow<List<Listing>> {
        return listingDao.searchListings(query)
            .map { entities -> entities.map { it.toListing() } }
    }

    suspend fun insertListing(listing: Listing) {
        listingDao.insert(listing.toEntity())
    }

    suspend fun updateListing(listing: Listing) {
        listingDao.update(listing.toEntity())
    }
}

// Extension functions for conversion
private fun ListingEntity.toListing(): Listing {
    return Listing(
        id = id,
        title = title,
        description = description,
        price = price,
        categoryId = categoryId,
        imageUrls = imageUrls.split(","), // Convert string to list
        location = location,
        createdAt = createdAt
    )
}

private fun Listing.toEntity(): ListingEntity {
    return ListingEntity(
        id = id,
        title = title,
        description = description,
        price = price,
        userId = "user_1", // TODO: Get from auth system
        categoryId = categoryId,
        location = location,
        imageUrls = imageUrls.joinToString(","), // Convert list to string
        createdAt = createdAt ?: System.currentTimeMillis()
    )
}
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Update ListingRepository to use Room instead of mock data.

Requirements:
- Replace fake data with ListingDao
- Convert Flow<ListingEntity> to Flow<Listing> using map
- Create extension functions toListing() and toEntity()
- Keep same public API (don't break existing ViewModels)
- Use coroutines properly
- Handle imageUrls conversion (String in DB, List in model)

File location: data/repository/ListingRepository.kt
```

**Acceptance Criteria:**
- [ ] Mock data removed completely
- [ ] Room integration complete
- [ ] Entity-Model conversion functions work
- [ ] Existing ViewModels still work
- [ ] App compiles and runs
- [ ] Data persists after app restart

---

### Task 14: Create UserRepository
**Priority:** MEDIUM
**Time:** 30 minutes

**File to Create:**
```
data/repository/UserRepository.kt
```

**Requirements:**
```kotlin
class UserRepository(private val userDao: UserDao) {

    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)?.toUser()
    }

    suspend fun insertUser(user: User) {
        userDao.insert(user.toEntity())
    }

    suspend fun updateUser(user: User) {
        userDao.update(user.toEntity())
    }
}

// Extension functions
private fun UserEntity.toUser(): User {
    return User(
        id = id,
        name = name,
        email = email,
        profilePicUrl = profilePicUrl
    )
}

private fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email,
        profilePicUrl = profilePicUrl,
        createdAt = System.currentTimeMillis()
    )
}
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Create UserRepository with Room integration.

Requirements:
- Wrap UserDao
- Provide methods: getUserById, insertUser, updateUser
- Entity-Model conversion functions
- Follow same pattern as ListingRepository

File location: data/repository/UserRepository.kt
```

**Acceptance Criteria:**
- [ ] UserRepository.kt created
- [ ] CRUD operations work
- [ ] Conversion functions included
- [ ] Follows project patterns
- [ ] Compiles without errors

---

### Task 15: Create FavouriteRepository
**Priority:** MEDIUM
**Time:** 30 minutes

**File to Create:**
```
data/repository/FavouriteRepository.kt
```

**Requirements:**
```kotlin
class FavouriteRepository(private val favouriteDao: FavouriteDao) {

    suspend fun toggleFavourite(userId: String, listingId: String) {
        if (favouriteDao.isFavourite(userId, listingId)) {
            favouriteDao.removeFavourite(userId, listingId)
        } else {
            favouriteDao.addFavourite(
                FavouriteEntity(
                    userId = userId,
                    listingId = listingId,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun isFavourite(userId: String, listingId: String): Boolean {
        return favouriteDao.isFavourite(userId, listingId)
    }

    fun getUserFavouriteListings(userId: String): Flow<List<Listing>> {
        return favouriteDao.getUserFavouriteListings(userId)
            .map { entities -> entities.map { it.toListing() } }
    }
}
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Create FavouriteRepository with toggle functionality.

Requirements:
- toggleFavourite (add if not exists, remove if exists)
- isFavourite check
- getUserFavouriteListings with conversion to Listing model
- Use FavouriteDao
- Reuse toListing() extension from ListingRepository

File location: data/repository/FavouriteRepository.kt
```

**Acceptance Criteria:**
- [ ] FavouriteRepository.kt created
- [ ] Toggle logic works correctly
- [ ] Returns full Listing objects
- [ ] Compiles without errors

---

### Task 16: Seed Database with Sample Data
**Priority:** MEDIUM
**Time:** 45 minutes

**File to Create:**
```
data/local/DatabaseSeeder.kt
```

**Requirements:**
```kotlin
class DatabaseSeeder(private val database: AppDatabase) {

    suspend fun seed() {
        // Check if already seeded
        val existingCategories = database.categoryDao().getAllCategories().first()
        if (existingCategories.isNotEmpty()) return

        // Seed categories
        val categories = listOf(
            CategoryEntity("cat_1", "Electronics", "📱"),
            CategoryEntity("cat_2", "Fashion", "👗"),
            CategoryEntity("cat_3", "Home & Living", "🏠"),
            CategoryEntity("cat_4", "Books", "📚"),
            CategoryEntity("cat_5", "Sports", "⚽")
        )
        database.categoryDao().insertAll(categories)

        // Seed users
        val users = listOf(
            UserEntity("user_1", "John Tan", "john@example.sg", null, System.currentTimeMillis()),
            UserEntity("user_2", "Mary Lim", "mary@example.sg", null, System.currentTimeMillis()),
            // Add more users...
        )
        users.forEach { database.userDao().insert(it) }

        // Seed listings
        val listings = listOf(
            ListingEntity(
                id = "list_1",
                title = "iPhone 13 Pro",
                description = "Good condition, 256GB",
                price = 899.00,
                userId = "user_1",
                categoryId = "cat_1",
                location = "Orchard",
                imageUrls = "",
                createdAt = System.currentTimeMillis(),
                isSold = false
            ),
            // Add more listings...
        )
        database.listingDao().insertAll(listings)
    }
}
```

**Gemini Prompt:**
```
[Paste RULES.md and PROJECT_CONTEXT.md first]

Task: Create DatabaseSeeder to populate sample data for testing.

Requirements:
- Create suspend function to seed all tables
- 5 sample categories: Electronics, Fashion, Home, Books, Sports (with emoji icons)
- 5-10 sample users with Singapore names
- 20-30 sample listings with realistic Singapore data (prices in SGD, locations)
- Check if database is already seeded (don't duplicate)
- Call from MainActivity onCreate for testing

File location: data/local/DatabaseSeeder.kt
```

**Acceptance Criteria:**
- [ ] DatabaseSeeder.kt created
- [ ] Sample data is realistic (Singapore context)
- [ ] Can be called on app startup
- [ ] Doesn't duplicate data on re-run
- [ ] Covers all important entities

**How to Use:**
Add to MainActivity.onCreate():
```kotlin
lifecycleScope.launch {
    val seeder = DatabaseSeeder(AppDatabase.getDatabase(this@MainActivity))
    seeder.seed()
}
```

---

### Task 17: Test Database Operations
**Priority:** HIGH
**Time:** 30 minutes

**Testing Checklist:**

**Build & Run:**
- [ ] Clean build succeeds: `./gradlew clean build`
- [ ] App installs on Pixel 9 emulator
- [ ] No Room errors in Logcat
- [ ] Database file created (check Device File Explorer)

**Database Inspector:**
- [ ] Open Database Inspector in Android Studio
- [ ] Verify all 5 tables exist
- [ ] Check sample data is present
- [ ] Verify foreign key relationships

**Functional Testing:**
- [ ] HomeScreen displays listings from database
- [ ] Listing detail shows correct data
- [ ] Can create new listing (it appears in list)
- [ ] Can search listings (results are correct)
- [ ] Can filter by category (works correctly)
- [ ] Data persists after app restart (close and reopen)

**Performance Testing:**
- [ ] List scrolls smoothly
- [ ] No lag when opening detail screen
- [ ] Search is responsive
- [ ] No memory leaks (check Profiler)

**Error Handling:**
- [ ] Check Logcat for any Room warnings
- [ ] Verify no database locked errors
- [ ] Check for proper coroutine scope usage

---

## 📦 Deliverables Summary

By completion, you will have created/modified:

### New Entity Files (6)
1. `UserEntity.kt`
2. `ListingEntity.kt`
3. `CategoryEntity.kt`
4. `FavouriteEntity.kt`
5. `ChatMessageEntity.kt`

### New DAO Files (5)
1. `UserDao.kt`
2. `ListingDao.kt`
3. `CategoryDao.kt`
4. `FavouriteDao.kt`
5. `ChatMessageDao.kt`

### Modified/New Repository Files (3)
1. `ListingRepository.kt` (modified)
2. `UserRepository.kt` (new)
3. `FavouriteRepository.kt` (new)

### Configuration Files (2)
1. `AppDatabase.kt` (modified)
2. `DatabaseSeeder.kt` (new)

**Total: 16 files**

---

## 🤝 Coordination with Other Team Members

### Before You Start:
- [x] Confirm with **Member 1 (Tech Lead)** that base architecture is stable
- [ ] Check that Room dependencies are in `build.gradle.kts`

### During Development:
- [ ] Inform **Member 4 (Networking)** when Room is ready for integration
- [ ] Coordinate with **Member 5 (Camera)** on image storage strategy
- [ ] Update **Member 2 (UI/UX)** when favourite functionality is ready

### After Completion:
- [ ] Notify **Member 1** that database layer is complete
- [ ] Provide database schema documentation
- [ ] Demo database features to team

---

## 🚨 Important Reminders

### Follow Team Workflow:
1. **Always** paste `ai/RULES.md` to Gemini first
2. **Then** paste `ai/PROJECT_CONTEXT.md`
3. **Then** use ONE task-specific prompt from above
4. **Review** generated code before committing
5. **Test** after each task
6. **Commit** in small, logical steps

### Code Quality:
- Follow MVVM architecture strictly
- Use StateFlow (not LiveData)
- All database operations must use suspend or Flow
- No blocking calls on main thread
- Proper error handling in repositories

### Testing:
- Test after completing each phase
- Use Database Inspector frequently
- Check Logcat for Room warnings
- Verify data persistence

---

## 📅 Timeline Recommendation

**Week 2: Core Functionality** (Current Week)

**Days 1-2:** Tasks 1-6 (Entities)
- Set up all entity classes
- Define relationships
- Ensure everything compiles

**Days 3-4:** Tasks 7-12 (DAOs + Database)
- Create all DAO interfaces
- Update AppDatabase
- Test compilation

**Days 5-6:** Tasks 13-15 (Repositories)
- Integrate Room with repositories
- Replace mock data
- Test basic CRUD operations

**Day 7:** Tasks 16-17 (Seeding + Testing)
- Add sample data
- Comprehensive testing
- Bug fixes

---

## 📞 Need Help?

**Common Issues:**

1. **Compilation errors after adding entities:**
   - Clean and rebuild: `./gradlew clean build`
   - Invalidate caches: File → Invalidate Caches / Restart
   - Check KSP is generating code properly

2. **Foreign key constraint failures:**
   - Ensure parent entities exist before children
   - Check IDs match exactly
   - Review CASCADE settings

3. **Flow not emitting updates:**
   - Verify using Flow (not suspend) for observable queries
   - Check collectAsState() in Composables
   - Ensure ViewModel scope is correct

4. **Database locked errors:**
   - Only one Room instance (use singleton)
   - Don't block database operations
   - Use proper coroutine scopes

**Resources:**
- [Room Documentation](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- Project `ai/RULES.md` and `ai/PROJECT_CONTEXT.md`

---

## ✅ Final Checklist

Before marking database work as complete:

- [ ] All 17 tasks completed
- [ ] All files compile without errors
- [ ] App runs successfully on emulator
- [ ] Database persists data correctly
- [ ] Sample data displays in HomeScreen
- [ ] No Room errors in Logcat
- [ ] Code committed to git with clear messages
- [ ] Team members notified of completion
- [ ] Ready for integration with networking layer

---

**Good luck! Remember to follow the team workflow and test frequently.**

*Last updated: [Date]*
