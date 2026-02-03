# TinyCell - Application Architecture Documentation

## Table of Contents
1. [Overview](#overview)
2. [Architecture Pattern](#architecture-pattern)
3. [Project Structure](#project-structure)
4. [Data Layer](#data-layer)
5. [Domain Layer](#domain-layer)
6. [UI Layer](#ui-layer)
7. [Dependency Injection](#dependency-injection)
8. [Data Flow](#data-flow)
9. [Key Components](#key-components)
10. [File Reference](#file-reference)

---

## Overview

**TinyCell** is a native Android marketplace application built using modern Android development practices. It implements a Carousell-style peer-to-peer marketplace where users can browse, create, and manage listings, as well as chat with other users.

### Tech Stack
- **Language:** Kotlin 1.9.24
- **UI Framework:** Jetpack Compose with Material 3
- **Architecture:** MVVM (Model-View-ViewModel)
- **Local Database:** Room (SQLite)
- **Backend:** Firebase (Firestore, Auth, Storage)
- **Networking:** Retrofit 2.9.0
- **Navigation:** Navigation Compose
- **Async:** Kotlin Coroutines + StateFlow
- **Dependency Injection:** Manual DI Container

---

## Architecture Pattern

The app follows **MVVM (Model-View-ViewModel)** architecture with a clear separation of concerns:

```
┌─────────────────────────────────────────────┐
│              UI Layer (Compose)             │
│  ┌─────────────┐  ┌─────────────┐          │
│  │   Screen    │  │  ViewModel  │          │
│  │ (Stateless) │←─│ (StateFlow) │          │
│  └─────────────┘  └─────────────┘          │
└─────────────────────┬───────────────────────┘
                      │
┌─────────────────────▼───────────────────────┐
│          Repository Layer                   │
│  ┌──────────────────────────────┐          │
│  │  Repository (Business Logic) │          │
│  └──────────┬──────────┬────────┘          │
└─────────────┼──────────┼───────────────────┘
              │          │
    ┌─────────▼────┐  ┌──▼─────────┐
    │ Local (Room) │  │   Remote   │
    │   Database   │  │ (Firebase) │
    └──────────────┘  └────────────┘
```

### Key Principles
1. **Single Source of Truth:** ViewModels hold UI state
2. **Unidirectional Data Flow:** UI → ViewModel → Repository → Data Source
3. **Stateless Composables:** UI components receive state and emit events
4. **Repository Pattern:** Abstracts data sources (local + remote)
5. **Dual-Write Strategy:** Data written to both Room and Firestore

---

## Project Structure

```
com.example.tinycell/
├── MainActivity.kt                    # Single activity host
├── MarketplaceApp.kt                  # Application class
│
├── data/                              # Data layer
│   ├── local/                         # Room database
│   │   ├── dao/                       # Data Access Objects
│   │   ├── entity/                    # Database entities
│   │   ├── AppDatabase.kt             # Room database instance
│   │   └── DatabaseSeeder.kt          # Initial data seeding
│   │
│   ├── remote/                        # Firebase integration
│   │   ├── datasource/                # Firestore data sources
│   │   ├── model/                     # DTOs (Data Transfer Objects)
│   │   ├── firestore/                 # Firestore provider
│   │   ├── ApiService.kt              # Retrofit API interface
│   │   └── NetworkModule.kt           # Network configuration
│   │
│   ├── model/                         # Domain models
│   │   ├── User.kt
│   │   ├── Listing.kt
│   │   ├── Category.kt
│   │   ├── ChatMessage.kt
│   │   └── ChatRoom.kt
│   │
│   └── repository/                    # Repository implementations
│       ├── ListingRepository.kt
│       ├── AuthRepository.kt
│       ├── ChatRepository.kt
│       ├── UserRepository.kt
│       ├── FavouriteRepository.kt
│       └── CameraRepository.kt
│
├── di/                                # Dependency injection
│   └── AppContainer.kt                # Manual DI container
│
└── ui/                                # UI layer
    ├── navigation/                    # Navigation setup
    │   ├── NavGraph.kt                # Navigation graph
    │   └── Screen.kt                  # Route definitions
    │
    ├── screens/                       # Feature screens
    │   ├── home/                      # Browse listings
    │   ├── detail/                    # Listing details
    │   ├── create/                    # Create listing
    │   ├── profile/                   # User profile
    │   ├── chat/                      # Chat interface
    │   ├── camera/                    # Camera/Gallery
    │   ├── mylistings/                # User's listings
    │   └── listingchats/              # Listing chat rooms
    │
    ├── components/                    # Reusable UI components
    │   └── MarketplaceComponents.kt
    │
    └── theme/                         # Material 3 theme
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## Data Layer

### 1. Local Database (Room)

**AppDatabase.kt** - Room database singleton
- Manages all database entities
- Provides DAO instances
- Handles database migrations

**Entities:**
- `UserEntity` - User information
- `ListingEntity` - Product listings
- `CategoryEntity` - Product categories
- `FavouriteEntity` - User favorites
- `ChatMessageEntity` - Chat messages
- `OfferEntity` - Price offers
- `AppEntity` - Generic app data

**DAOs (Data Access Objects):**
- `UserDao` - User CRUD operations
- `ListingDao` - Listing CRUD + queries
- `CategoryDao` - Category operations
- `FavouriteDao` - Favorite management
- `ChatMessageDao` - Chat persistence
- `OfferDao` - Offer management
- `AppDao` - Generic operations

All DAOs use `suspend` functions for coroutine support.

### 2. Remote Data (Firebase)

**Firestore Data Sources:**
- `FirestoreChatDataSource` - Real-time chat sync
- `FirestoreListingDataSource` - Listing cloud storage
- `FirestoreUserDataSource` - User profile sync

**DTOs (Data Transfer Objects):**
- `UserDto` - Firestore user format
- `ListingDto` - Firestore listing format
- `ChatMessageDto` - Firestore message format
- `ChatRoomDto` - Chat room metadata
- `OfferDto` - Offer data format

**Providers:**
- `FirestoreProvider` - Firestore instance management

### 3. Domain Models

**User.kt**
```kotlin
data class User(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Long
)
```

**Listing.kt**
```kotlin
data class Listing(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val userId: String,
    val sellerName: String,
    val categoryId: String,
    val location: String,
    val imageUrls: List<String>,
    val createdAt: Long,
    val isSold: Boolean
)
```

**Category.kt**
```kotlin
data class Category(
    val id: String,
    val name: String,
    val icon: String
)
```

**ChatMessage.kt**
```kotlin
data class ChatMessage(
    val id: String,
    val chatRoomId: String,
    val senderId: String,
    val text: String,
    val timestamp: Long
)
```

**ChatRoom.kt**
```kotlin
data class ChatRoom(
    val id: String,
    val listingId: String,
    val buyerId: String,
    val sellerId: String,
    val lastMessage: String?,
    val lastMessageTime: Long
)
```

### 4. Repositories

Repositories implement the **Single Source of Truth** pattern and handle data synchronization.

**ListingRepository.kt**
- Primary repository for product listings
- **Dual-write strategy:** Writes to both Room and Firestore
- **Real-time sync:** Listens to Firestore changes
- **Offline-first:** Reads from Room, syncs with Firestore

Key Methods:
- `getAllListings()` - Fetch all listings from Room
- `getListingById(id)` - Get single listing
- `createListing(listing)` - Create new listing (dual-write)
- `updateListing(listing)` - Update listing (dual-write)
- `deleteListing(id)` - Delete listing (dual-write)
- `syncFromRemote()` - Pull Firestore data to Room
- `startRealTimeSync()` - Enable live updates

**AuthRepository.kt**
- Interface for authentication operations
- Implementation: `FirebaseAuthRepositoryImpl`

Key Methods:
- `signInAnonymously()` - Anonymous auth
- `getCurrentUserId()` - Get current user ID
- `getCurrentUserName()` - Get current user name

**ChatRepository.kt**
- Manages chat functionality
- Interface with implementation: `ChatRepositoryImpl`
- Integrates Room (local cache) + Firestore (real-time)

Key Methods:
- `getChatRooms(userId)` - Get user's chat rooms
- `getChatMessages(chatRoomId)` - Get messages for a chat
- `sendMessage(message)` - Send new message
- `observeChatMessages(chatRoomId)` - Real-time message flow

**UserRepository.kt**
- User profile management
- Handles user CRUD operations

**FavouriteRepository.kt**
- Manages user favorites
- Toggle favorite status
- Query favorite listings

**CameraRepository.kt**
- Camera provider initialization
- Image capture handling

**RemoteListingRepository.kt**
- Pure Firestore listing operations
- Implementation: `FirestoreListingRepositoryImpl`

**RemoteImageRepository.kt**
- Firebase Storage image uploads
- Implementation: `FirebaseStorageRepositoryImpl`

---

## Domain Layer

The domain layer consists of:
1. **Domain Models** - Pure Kotlin data classes
2. **Repository Interfaces** - Abstract data operations

No business logic in this layer - repositories handle it.

---

## UI Layer

### 1. Navigation

**Screen.kt** - Sealed class defining all routes
```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreateListing : Screen("create_listing")
    object Profile : Screen("profile")
    object ListingDetail : Screen("listing_detail/{listingId}")
    object Chat : Screen("chat/{chatRoomId}/{listingId}/{listingTitle}/{otherUserId}/{otherUserName}")
    object MyListings : Screen("my_listings")
    object ListingChats : Screen("listing_chats/{listingId}/{listingTitle}")
}
```

**NavGraph.kt** - Navigation graph setup
- Defines all composable destinations
- Handles route parameters
- Manages back stack

### 2. Screens

Each screen follows the same pattern:
- **Screen Composable** - UI (stateless)
- **ViewModel** - State management + business logic
- **ViewModel Factory** (if needed) - ViewModel instantiation with dependencies

#### Home Screen
**HomeScreen.kt** - Browse listings
- LazyColumn/Grid of listings
- Search functionality
- Category filters
- Pull-to-refresh

**HomeViewModel.kt**
- `StateFlow<List<Listing>>` - Listings state
- `StateFlow<String>` - Search query
- `loadListings()` - Fetch data from repository
- `searchListings(query)` - Filter listings

#### Listing Detail Screen
**ListingDetailScreen.kt** - View listing details
- Image carousel
- Listing information
- Seller info
- "Chat" and "Favorite" buttons

**ListingDetailViewModel.kt**
- `StateFlow<Listing?>` - Current listing
- `loadListing(id)` - Fetch specific listing
- `toggleFavorite()` - Add/remove favorite

#### Create Listing Screen
**CreateListingScreen.kt** - Create new listing form
- Input fields (title, description, price, category)
- Image picker/camera integration
- Location tagging
- Submit button

**CreateListingViewModel.kt**
- `StateFlow<CreateListingState>` - Form state
- `updateTitle(text)` - Update title
- `updatePrice(value)` - Update price
- `submitListing()` - Create listing via repository

**CreateListingViewModelFactory.kt**
- Provides ViewModel with repository dependencies

#### Profile Screen
**ProfileScreen.kt** - User profile
- User information display
- User's listings
- Favorites section
- Edit profile option

**ProfileViewModel.kt**
- `StateFlow<User?>` - User state
- `StateFlow<List<Listing>>` - User's listings
- `loadUserData()` - Fetch user info

#### Chat Screen
**ChatScreen.kt** - Chat interface
- Message list (LazyColumn)
- Message input field
- Send button
- Real-time message updates

**ChatViewModel.kt**
- `StateFlow<List<ChatMessage>>` - Messages
- `StateFlow<String>` - Input text
- `sendMessage(text)` - Send message
- `observeMessages()` - Listen to new messages

#### Camera Screen
**CameraScreen.kt** - Camera interface
- Camera preview
- Capture button
- Permission handling

**CameraViewModel.kt**
- `StateFlow<CameraState>` - Camera initialization state
- `initializeCamera()` - Setup camera provider
- `capturePhoto()` - Take picture

**CameraViewModelFactory.kt**
- Provides ViewModel with camera repository

**GalleryScreen.kt** - Image gallery picker
- Grid of images from device
- Multi-select capability

#### My Listings Screen
**MyListingsScreen.kt** - User's own listings
- List of user's created listings
- Edit/delete options
- View chat inquiries

**MyListingsViewModel.kt**
- `StateFlow<List<Listing>>` - User's listings
- `loadMyListings()` - Fetch user's listings
- `deleteListing(id)` - Remove listing

#### Listing Chats Screen
**ListingChatsScreen.kt** - All chats for a specific listing
- List of chat rooms for a listing
- Shows buyer information
- Navigate to specific chat

**ListingChatsViewModel.kt**
- `StateFlow<List<ChatRoom>>` - Chat rooms
- `loadChatsForListing(listingId)` - Fetch chats

### 3. Components

**MarketplaceComponents.kt** - Reusable UI components
- `ListingCard()` - Product card display
- `CategoryChip()` - Category selector
- `PriceTag()` - Formatted price display
- `UserAvatar()` - User profile picture
- `MessageBubble()` - Chat message bubble
- `SearchBar()` - Search input
- `EmptyState()` - Empty list placeholder

### 4. Theme

**Color.kt** - Material 3 color scheme
- Primary, secondary, tertiary colors
- Light and dark theme colors

**Theme.kt** - App theme configuration
- Material 3 theme setup
- Typography integration
- Color scheme application

**Type.kt** - Typography definitions
- Font families
- Text styles (headings, body, labels)

---

## Dependency Injection

**AppContainer.kt** - Manual DI container

### Initialization Flow
```kotlin
class MarketplaceApp : Application() {
    val container: AppContainer

    override fun onCreate() {
        container = AppContainer(this)
        container.initializeData()
    }
}
```

### Container Responsibilities
1. **Firebase Instances**
   - Firestore
   - Firebase Auth
   - Firebase Storage

2. **Repositories**
   - ListingRepository
   - AuthRepository
   - ChatRepository
   - RemoteListingRepository
   - RemoteImageRepository

3. **Database**
   - Room database instance
   - DAO providers

4. **Initialization Tasks**
   - Anonymous sign-in
   - Database seeding
   - Real-time sync setup
   - Remote data synchronization

### Lazy Initialization
All dependencies use `lazy` delegation for on-demand initialization:

```kotlin
val listingRepository: ListingRepository by lazy {
    ListingRepository(
        database.listingDao(),
        database.userDao(),
        database.offerDao(),
        remoteListingRepository,
        remoteImageRepository,
        authRepository
    )
}
```

### Data Seeding
On first launch, the app seeds the database with:
- Current authenticated user
- Default categories (General, Electronics, Fashion, Home, Toys, Books)

---

## Data Flow

### 1. Read Flow (Offline-First)
```
User Action (Browse Listings)
    ↓
HomeViewModel.loadListings()
    ↓
ListingRepository.getAllListings()
    ↓
Room Database (listingDao.getAll())
    ↓
StateFlow emission
    ↓
UI updates (HomeScreen recomposes)
```

### 2. Write Flow (Dual-Write)
```
User Action (Create Listing)
    ↓
CreateListingViewModel.submitListing()
    ↓
ListingRepository.createListing()
    ├→ Room Database (local insert)
    └→ Firestore (remote insert)
    ↓
StateFlow emission
    ↓
Navigation to Home
```

### 3. Real-Time Sync Flow
```
Firestore Change (New listing added by another user)
    ↓
ListingRepository.startRealTimeSync()
    ↓
Firestore Snapshot Listener
    ↓
Room Database (sync new data)
    ↓
StateFlow emission
    ↓
UI automatically updates
```

### 4. Chat Flow (Real-Time)
```
User sends message
    ↓
ChatViewModel.sendMessage()
    ↓
ChatRepository.sendMessage()
    ├→ Room (local cache)
    └→ Firestore (real-time DB)
    ↓
Firestore triggers snapshot listener
    ↓
ChatRepository.observeChatMessages()
    ↓
StateFlow emission
    ↓
ChatScreen updates with new message
```

---

## Key Components

### MainActivity.kt
- Single activity host
- Initializes `AppContainer` from `MarketplaceApp`
- Sets up Compose with `TinyCellTheme`
- Creates `NavController`
- Renders `TinyCellNavHost`

```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = (application as MarketplaceApp).container

        setContent {
            TinyCellTheme {
                val navController = rememberNavController()
                TinyCellNavHost(
                    navController = navController,
                    listingRepository = appContainer.listingRepository,
                    authRepository = appContainer.authRepository,
                    chatRepository = appContainer.chatRepository,
                    appContainer = appContainer
                )
            }
        }
    }
}
```

### MarketplaceApp.kt
- Application class
- Initializes `AppContainer` on app start
- Triggers background initialization tasks:
  - Anonymous authentication
  - Database seeding
  - Real-time sync setup
  - Remote data pull

```kotlin
class MarketplaceApp : Application() {
    private var _container: AppContainer? = null
    val container: AppContainer get() = _container ?:
        throw IllegalStateException("AppContainer not initialized")

    override fun onCreate() {
        super.onCreate()
        _container = AppContainer(this)
        container.initializeData()
    }
}
```

---

## File Reference

### Entry Points
| File | Purpose | Lines of Code |
|------|---------|---------------|
| `MainActivity.kt` | Activity host | ~49 |
| `MarketplaceApp.kt` | Application class | ~38 |

### Data Layer - Local
| File | Purpose | Key Methods |
|------|---------|-------------|
| `AppDatabase.kt` | Room database singleton | `getDatabase()`, migration logic |
| `DatabaseSeeder.kt` | Initial data population | `seedDatabase()` |
| **DAOs** | | |
| `UserDao.kt` | User CRUD | `insert()`, `getById()`, `update()`, `delete()` |
| `ListingDao.kt` | Listing CRUD + queries | `getAll()`, `getById()`, `searchByTitle()`, `getByCategory()` |
| `CategoryDao.kt` | Category operations | `getAll()`, `insert()` |
| `FavouriteDao.kt` | Favorite management | `toggleFavorite()`, `getUserFavorites()` |
| `ChatMessageDao.kt` | Message persistence | `insertMessage()`, `getMessagesForRoom()` |
| `OfferDao.kt` | Offer CRUD | `insertOffer()`, `getOffersForListing()` |
| `AppDao.kt` | Generic operations | App-wide queries |
| **Entities** | | |
| `UserEntity.kt` | User table schema | Room entity with primary key |
| `ListingEntity.kt` | Listing table schema | Includes foreign key to User |
| `CategoryEntity.kt` | Category table schema | Simple lookup table |
| `FavouriteEntity.kt` | Favorites junction table | User-Listing relationship |
| `ChatMessageEntity.kt` | Chat message schema | Includes timestamp, sender |
| `OfferEntity.kt` | Offer table schema | Price offer data |
| `AppEntity.kt` | Generic app data | Miscellaneous storage |

### Data Layer - Remote
| File | Purpose | Key Methods |
|------|---------|-------------|
| `FirestoreChatDataSource.kt` | Chat Firestore ops | `sendMessage()`, `observeMessages()` |
| `FirestoreListingDataSource.kt` | Listing Firestore ops | `createListing()`, `getAllListings()`, `observeListings()` |
| `FirestoreUserDataSource.kt` | User Firestore ops | `createUser()`, `getUserById()` |
| `FirestoreProvider.kt` | Firestore instance | Singleton provider |
| `ApiService.kt` | Retrofit API interface | (Currently unused - placeholder) |
| `NetworkModule.kt` | Retrofit configuration | (Currently unused - placeholder) |
| **DTOs** | | |
| `UserDto.kt` | Firestore user format | Serialization/deserialization |
| `ListingDto.kt` | Firestore listing format | Maps to/from domain model |
| `ChatMessageDto.kt` | Firestore message format | Timestamp handling |
| `ChatRoomDto.kt` | Firestore chat room | Metadata storage |
| `OfferDto.kt` | Firestore offer format | Price negotiation data |

### Data Layer - Repositories
| File | Purpose | Pattern |
|------|---------|---------|
| `ListingRepository.kt` | Primary listing repository | Dual-write (Room + Firestore) |
| `AuthRepository.kt` | Auth interface | Interface + implementation |
| `FirebaseAuthRepositoryImpl.kt` | Firebase auth impl | Anonymous auth |
| `ChatRepository.kt` | Chat interface | Interface + implementation |
| `ChatRepositoryImpl.kt` | Chat implementation | Real-time + caching |
| `UserRepository.kt` | User management | CRUD operations |
| `FavouriteRepository.kt` | Favorites logic | Toggle + query |
| `CameraRepository.kt` | Camera initialization | ProcessCameraProvider |
| `RemoteListingRepository.kt` | Firestore listing ops | Pure remote operations |
| `FirestoreListingRepositoryImpl.kt` | Implementation | Firestore CRUD |
| `RemoteImageRepository.kt` | Image upload interface | Firebase Storage |
| `FirebaseStorageRepositoryImpl.kt` | Image upload impl | Storage operations |
| `NetworkRepository.kt` | Network operations | (Placeholder for future API) |
| `AppRepository.kt` | Generic app repo | App-wide data |

### Dependency Injection
| File | Purpose | Contains |
|------|---------|----------|
| `AppContainer.kt` | Manual DI container | All repository instances, Firebase instances, initialization logic |

### Domain Models
| File | Purpose | Fields |
|------|---------|--------|
| `User.kt` | User domain model | id, name, email, createdAt |
| `Listing.kt` | Listing domain model | id, title, description, price, userId, sellerName, categoryId, location, imageUrls, createdAt, isSold |
| `Category.kt` | Category domain model | id, name, icon |
| `ChatMessage.kt` | Message domain model | id, chatRoomId, senderId, text, timestamp |
| `ChatRoom.kt` | Chat room domain model | id, listingId, buyerId, sellerId, lastMessage, lastMessageTime |

### UI Layer - Navigation
| File | Purpose | Lines |
|------|---------|-------|
| `Screen.kt` | Route definitions | ~50 |
| `NavGraph.kt` | Navigation graph | Composable destinations, parameter handling |

### UI Layer - Screens
| Screen | Files | ViewModel State | Key Features |
|--------|-------|-----------------|--------------|
| Home | `HomeScreen.kt`, `HomeViewModel.kt` | `StateFlow<List<Listing>>` | Browse, search, filter |
| Detail | `ListingDetailScreen.kt`, `ListingDetailViewModel.kt` | `StateFlow<Listing?>` | View details, chat, favorite |
| Create | `CreateListingScreen.kt`, `CreateListingViewModel.kt`, `CreateListingViewModelFactory.kt` | `StateFlow<CreateState>` | Form, camera, submit |
| Profile | `ProfileScreen.kt`, `ProfileViewModel.kt` | `StateFlow<User?>` | User info, listings, favorites |
| Chat | `ChatScreen.kt`, `ChatViewModel.kt` | `StateFlow<List<ChatMessage>>` | Real-time messaging |
| Camera | `CameraScreen.kt`, `CameraViewModel.kt`, `CameraViewModelFactory.kt` | `StateFlow<CameraState>` | Camera preview, capture |
| Gallery | `GalleryScreen.kt` | N/A | Image picker |
| My Listings | `MyListingsScreen.kt`, `MyListingsViewModel.kt` | `StateFlow<List<Listing>>` | User's listings, manage |
| Listing Chats | `ListingChatsScreen.kt`, `ListingChatsViewModel.kt` | `StateFlow<List<ChatRoom>>` | All chats for listing |

### UI Layer - Components & Theme
| File | Purpose | Components |
|------|---------|-----------|
| `MarketplaceComponents.kt` | Reusable components | ListingCard, CategoryChip, PriceTag, UserAvatar, MessageBubble, SearchBar, EmptyState |
| `Color.kt` | Material 3 colors | Primary, secondary, tertiary palettes |
| `Theme.kt` | Theme configuration | Light/dark theme setup |
| `Type.kt` | Typography | Font definitions, text styles |

---

## Sync Strategy

### Dual-Write Pattern
When creating/updating data:
1. Write to Room (local) first for immediate UI update
2. Write to Firestore (remote) for persistence and sync
3. If remote write fails, local data remains (offline-first)

### Real-Time Sync
- Firestore snapshot listeners update Room in background
- UI observes Room via StateFlow
- Changes from other users automatically appear

### Offline Support
- All reads come from Room (works offline)
- Writes queued if no connection (Firebase handles retry)
- App fully functional without internet

---

## Threading Model

### Coroutines
- All repository operations use `suspend` functions
- ViewModels launch coroutines in `viewModelScope`
- Background work on `Dispatchers.IO`
- UI updates on `Dispatchers.Main`

```kotlin
viewModelScope.launch {
    _uiState.value = UiState.Loading
    val result = repository.getData() // Dispatchers.IO
    _uiState.value = UiState.Success(result) // Dispatchers.Main
}
```

### StateFlow
- Hot observable stream
- Replaces LiveData for Compose
- UI collects as state: `state.collectAsState()`

---

## Build Configuration

### app/build.gradle.kts
- **AGP:** 8.6.1
- **Kotlin:** 1.9.24
- **Compose BOM:** 2024.10.00
- **Compose Compiler:** 1.5.14
- **Room:** 2.6.1 (with KSP 1.9.24-1.0.20)
- **Firebase BOM:** 33.3.0
- **CameraX:** 1.3.4
- **Retrofit:** 2.9.0
- **Min SDK:** 26
- **Target SDK:** 35
- **Java Compatibility:** 17

### Key Plugins
- `com.android.application`
- `org.jetbrains.kotlin.android`
- `com.google.devtools.ksp` (for Room)
- `com.google.gms.google-services` (for Firebase)

---

## Summary

TinyCell is a well-structured Android application following modern best practices:

**Strengths:**
- Clear separation of concerns (MVVM)
- Offline-first architecture with real-time sync
- Type-safe navigation with Compose
- Coroutine-based async handling
- Material 3 design system
- Comprehensive feature set

**Data Flow:**
- Unidirectional: UI → ViewModel → Repository → Data Source
- StateFlow for reactive state management
- Dual-write for data persistence

**Key Features:**
- Browse and search listings
- Create listings with camera
- Real-time chat
- Favorites management
- Firebase backend integration
- Offline support

This architecture supports scalability, testability, and maintainability for the student project requirements.
