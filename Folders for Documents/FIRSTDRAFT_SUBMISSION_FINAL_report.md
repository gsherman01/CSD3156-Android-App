# CSD3156 Mobile and Cloud Computing — Team Project Report

**Course:** CSD3156 Mobile and Cloud Computing, Spring 2026
**Team ID:** `[TEAM ID]`
**Deadline:** 24 February 2026

| Name | SIT Student ID |
|------|---------------|
| `[Member 1 Name]` | `[SIT ID]` |
| `[Member 2 Name]` | `[SIT ID]` |
| `[Member 3 Name]` | `[SIT ID]` |
| `[Member 4 Name]` | `[SIT ID]` |
| `[Member 5 Name]` | `[SIT ID]` |
| `[Member 6 Name]` | `[SIT ID]` |

---

## Links

| Resource | URL |
|----------|-----|
| GitHub Repository | `[GITHUB LINK]` |
| App Demo Video | `[DEMO VIDEO LINK]` |
| Presentation Video | `[PRESENTATION VIDEO LINK]` |

---

## Table of Contents

1. [Application Overview](#1-application-overview)
2. [Design & Use Cases](#2-design--use-cases)
3. [Software Architecture](#3-software-architecture)
4. [Implementation Details](#4-implementation-details)
5. [Mobile Features](#5-mobile-features)
6. [Database Design](#6-database-design)
7. [Firebase & Cloud Integration](#7-firebase--cloud-integration)
8. [UI/UX Design](#8-uiux-design)
9. [Third-Party Libraries](#9-third-party-libraries)
10. [Software Engineering Practices](#10-software-engineering-practices)
11. [Results & Evaluation](#11-results--evaluation)
12. [AI Usage Declaration](#12-ai-usage-declaration)

---

## 1. Application Overview

**TinyCell** is a fully native Android peer-to-peer marketplace application, conceptually similar to Carousell. Users can browse listings, create their own listings with photos, chat in real time with buyers or sellers, make and negotiate formal price offers, manage a favourites list, and receive in-app notifications — all backed by a local Room SQLite database synchronised with Firebase Cloud Firestore.

| Attribute | Value |
|-----------|-------|
| Platform | 100% Native Android |
| Language | Kotlin 1.9.24 |
| UI Framework | Jetpack Compose (Material 3) |
| Min SDK | 26 (Android 8.0) |
| Target / Compile SDK | 35 |
| JVM Target | Java 17 |
| Architecture Pattern | MVVM (Model-View-ViewModel) |
| Backend | Firebase (Auth, Firestore, Storage) |
| Local Database | Room 2.6.1 (SQLite) |

---

## 2. Design & Use Cases

### 2.1 Core Concept

TinyCell provides a familiar buy-and-sell marketplace experience optimised for a mobile form factor. The design prioritises:

- **Discoverability** — a 2-column listing grid with search and multi-dimensional filters on the home screen.
- **Trust** — a seller review and star-rating system, plus a formal offer workflow so both parties have a clear record of negotiations.
- **Communication** — a real-time chat system supporting text, image, and embedded offer messages in a single thread per listing per pair of users.
- **Awareness** — an in-app notification system with unread count badges that surface events such as new offers and accepted deals.

### 2.2 User Roles & Use Cases

| Actor | Use Case |
|-------|---------|
| **Buyer** | Browse listings, search & filter, view details, add to favourites, contact seller, make an offer, view notifications, leave review |
| **Seller** | Create listings with photos, manage own listings, view inquiries per listing, accept/reject offers, mark as sold |
| **Both** | View public profiles, chat in real time, send image messages, track notification history |

### 2.3 Navigation Flow

```
MainActivity
└── TinyCellNavHost (Navigation Compose)
    ├── Home  [Bottom Nav]
    │   ├── → Listing Detail  {listingId}
    │   │   ├── → Chat  {chatRoomId, listingId, otherUser…}
    │   │   │   └── → Public Profile  {userId}
    │   │   └── → Public Profile  {userId}
    │   ├── → Create Listing
    │   ├── → My Favourites
    │   └── → Notifications
    │       └── → Listing Detail
    ├── Create Listing  [Bottom Nav — "Sell"]
    │   └── → Home  (on success)
    ├── All Chats  [Bottom Nav — "Chats"]
    │   └── → Chat
    │       └── → Public Profile
    └── Profile  [Bottom Nav]
        └── → My Listings
            └── → Listing Chats  {listingId}
                └── → Chat
```

---

## 3. Software Architecture

### 3.1 Architecture Pattern — MVVM

The application follows the **Model-View-ViewModel (MVVM)** pattern as recommended by the Android Jetpack guidelines, with a strict layering of concerns:

```
┌──────────────────────────────────────────────────┐
│              UI Layer  (Jetpack Compose)          │
│  Stateless composables — receive UiState,        │
│  emit events via lambdas / ViewModel calls       │
└───────────────────────┬──────────────────────────┘
                        │  observes StateFlow / collectAsState
┌───────────────────────▼──────────────────────────┐
│           ViewModel Layer  (StateFlow)            │
│  Business logic, UI state, coroutine scope       │
│  e.g. HomeViewModel, ChatViewModel …             │
└───────────────────────┬──────────────────────────┘
                        │  suspend / Flow calls
┌───────────────────────▼──────────────────────────┐
│              Repository Layer                    │
│  Single source of truth; abstracts local         │
│  and remote data sources, handles sync logic     │
└──────────┬────────────────────────┬──────────────┘
           │                        │
┌──────────▼──────────┐  ┌──────────▼──────────────┐
│   Local (Room)      │  │   Remote (Firebase)      │
│   SQLite — 8 tables │  │   Firestore / Auth /     │
│   8 DAOs, Entities  │  │   Storage                │
└─────────────────────┘  └──────────────────────────┘
```

**Unidirectional Data Flow:**
- UI emits user intents → ViewModel → Repository → DataSource
- DataSource emits `Flow<T>` → Repository → ViewModel `StateFlow<UiState>` → UI recomposes

### 3.2 Project Package Structure

```
com.example.tinycell/
├── MarketplaceApp.kt          # Application class; creates AppContainer
├── MainActivity.kt            # Single-activity host for Navigation Compose
│
├── di/
│   └── AppContainer.kt        # Manual service-locator / DI container
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt     # Room database singleton (v6, 8 tables)
│   │   ├── DatabaseSeeder.kt  # Initial data population
│   │   ├── entity/            # 8 Room entity data classes
│   │   └── dao/               # 8 DAO interfaces
│   │
│   ├── remote/
│   │   ├── firestore/         # FirestoreProvider
│   │   ├── datasource/        # Firestore data sources (listings, chat, users)
│   │   └── model/             # DTO (Data Transfer Object) classes
│   │
│   ├── model/                 # Domain models (Listing, User, ChatMessage…)
│   └── repository/            # 12 repository classes (interfaces + impls)
│
└── ui/
    ├── theme/                 # Material 3 colour, typography, theme
    ├── navigation/            # Screen sealed class + NavGraph
    ├── components/            # Reusable composables (ListingCard, etc.)
    └── screens/               # 12 feature screens, each with ViewModel
        ├── home/
        ├── detail/
        ├── create/
        ├── profile/
        ├── publicprofile/
        ├── chat/
        ├── allchats/
        ├── listingchats/
        ├── mylistings/
        ├── myfavorites/
        ├── camera/
        └── notifications/
```

### 3.3 Dependency Injection (Manual DI Container)

Rather than adopting a framework such as Hilt, the team implemented a manual **service-locator** pattern through `AppContainer`. This keeps the DI visible and avoids annotation-processing overhead for a project of this scale.

```kotlin
// MarketplaceApp.kt
class MarketplaceApp : Application() {
    lateinit var appContainer: AppContainer
    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        appContainer.initializeData()   // anonymous sign-in, DB seed, real-time sync
    }
}

// AppContainer.kt (simplified)
class AppContainer(context: Context) {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val authRepository: AuthRepository by lazy { FirebaseAuthRepositoryImpl(auth) }
    val listingRepository: ListingRepository by lazy {
        ListingRepository(database.listingDao(), remoteListingRepository, ...)
    }
    val chatRepository: ChatRepository by lazy {
        ChatRepositoryImpl(firestoreChatDataSource, database.chatMessageDao(), ...)
    }
    // … other repositories
}
```

### 3.4 Offline-First / Dual-Write Strategy

A key architectural decision is the **offline-first + real-time sync** approach:

1. **Writes go to Room first** → the UI updates immediately, even without a network connection.
2. **Writes are mirrored to Firestore** → cloud persistence and cross-device propagation.
3. **Firestore snapshot listeners** detect remote changes → update Room → UI recomposes automatically via `Flow`.

```
User creates listing
    │
    ├─ 1. listingDao.insert(entity)    ← Room (instant UI feedback)
    ├─ 2. firestore.set(listingDto)    ← Cloud persistence
    │
Firestore change event (own or another device)
    │
    └─ 3. snapshotListener → listingDao.insert(entity) → Flow emits → UI recomposes
```

---

## 4. Implementation Details

### 4.1 ViewModels & UI State

Every screen has a dedicated ViewModel that exposes **`StateFlow<UiState>`** collected by the composable via `collectAsState()`. This ensures the UI is always driven by a single, observable source of truth and survives configuration changes.

| ViewModel | Key State |
|-----------|-----------|
| `HomeViewModel` | `listings`, `searchFilters`, `favouriteStates`, `unreadNotificationCount`, `categories`, `isRefreshing` |
| `ListingDetailViewModel` | `listing`, `isFavourited`, `favouriteCount`, `isLoading` |
| `CreateListingViewModel` | `uiState` (form fields, validation, loading), `selectedImages` |
| `ChatViewModel` | `messages`, `messageText`, `offerAmount`, `showOfferDialog`, `showReviewDialog` |
| `AllChatsViewModel` | `chatRooms`, `unreadCounts` |
| `NotificationViewModel` | `notifications`, `unreadCount` |
| `MyListingsViewModel` | `uiState`, `listings` |
| `MyFavoritesViewModel` | `favouriteListings` |
| `ProfileViewModel` | `currentUser`, `displayName` |
| `PublicProfileViewModel` | `user`, `listings`, `averageRating`, `reviews` |
| `CameraViewModel` | `hasCameraPermission`, `isCameraReady`, `cameraProvider` |

### 4.2 Repository Layer

12 repositories provide a clean abstraction over data sources:

| Repository | Responsibility |
|------------|---------------|
| `ListingRepository` | CRUD, search/filter, offer workflow, status transitions, notification creation |
| `FirebaseAuthRepositoryImpl` | Anonymous sign-in, display name, debug UID override |
| `ChatRepositoryImpl` | Chat room lifecycle, message send/receive, unread tracking |
| `FavouriteRepository` | Add/remove favourites, count, watchers |
| `UserRepository` | Local user CRUD |
| `FirestoreListingRepositoryImpl` | Remote listing CRUD + snapshot sync |
| `FirebaseStorageRepositoryImpl` | Multi-image upload, URL generation |
| `FirestoreNotificationRepositoryImpl` | Remote notification push/pull |
| `CameraRepository` | CameraX provider lifecycle |
| `NetworkRepository` | Network connectivity helper |
| `RemoteListingRepository` (interface) | Abstraction for remote listing ops |
| `AppRepository` | Miscellaneous app-level data |

### 4.3 Real-Time Chat System

The chat system is one of the most complex modules. Chat rooms are identified by a **deterministic, order-independent ID**:

```kotlin
fun generateChatRoomId(listingId: String, userId1: String, userId2: String): String =
    "${listingId}_${minOf(userId1, userId2)}_${maxOf(userId1, userId2)}"
```

This guarantees the same room is opened regardless of who initiates the conversation. Messages support three types:

| Type | Description |
|------|-------------|
| `TEXT` | Plain text message |
| `OFFER` | Embedded offer card with accept/reject actions |
| `IMAGE` | Photo message uploaded to Firebase Storage |

The `ChatScreen` auto-scrolls to the latest message via `LazyListState` and marks messages read when the screen is visible. Firestore snapshot listeners keep messages in sync in real time across devices.

### 4.4 Offer & Negotiation Workflow

```
Buyer makes offer (amount)
    │
    ├─ OfferEntity created (status=PENDING) → Room + Firestore
    ├─ Listing status → PENDING
    ├─ NotificationEntity → seller's notification feed
    │
Seller views notification → opens chat
    │
    ├─ Accept → listing status → RESERVED + buyer notification
    └─ Reject → offer status → REJECTED
```

Status transitions for a listing follow: `AVAILABLE → PENDING → RESERVED → SOLD`.

### 4.5 Notification System

Notifications are generated on key events (new offer, accepted offer, price change, status change) and stored in both Room and Firestore. The `HomeScreen` displays a **badge** showing unread count:

```kotlin
BadgedBox(
    badge = {
        if (unreadCount > 0) Badge { Text(if (unreadCount > 9) "9+" else "$unreadCount") }
    }
) { Icon(Icons.Default.Notifications, …) }
```

### 4.6 Search & Filter

The Home screen supports multi-dimensional filtering, all executed as a single Room query:

- **Full-text search** on title and description
- **Category multi-select** (Electronics, Fashion, Home, Toys, Books, General)
- **Price range** (min / max slider)
- **Date range** filter
- **Active listings only** (excludes sold items)

### 4.7 URL Encoding for Navigation Parameters

Navigation routes that carry free-text parameters (listing titles, usernames) apply `URLEncoder` on departure and `URLDecoder` on arrival to safely pass strings with spaces and special characters through the Compose Navigation back-stack.

---

## 5. Mobile Features

The application implements **six** of the advanced mobile features required by the course specification (minimum three required):

### Feature 1 — Local Database (Room / SQLite)

Room is used as the primary offline data store with **8 tables and 8 DAOs**. All queries return `Flow<T>` for reactive, coroutine-native streaming. The schema uses foreign keys and composite unique indices (e.g., `UNIQUE(userId, listingId)` on favourites).

### Feature 2 — Networking (Firebase Cloud Firestore + Firebase Storage)

All user-generated content is synchronised to Google Firebase:
- **Cloud Firestore** stores 7 document collections and powers real-time snapshot listeners.
- **Firebase Storage** hosts listing and chat images, generating public download URLs.
- **Retrofit + OkHttp** is configured as a secondary networking layer for future REST API integration (currently initialised but ready to extend).

### Feature 3 — Camera (CameraX)

`CameraScreen` integrates **CameraX** (androidx.camera 1.3.4) to provide an in-app camera preview using `AndroidView` with `PreviewView`, lifecycle binding, and photo capture. Runtime camera permission is handled via `ActivityResultContracts.RequestPermission`. Captured images can be attached directly to new listings.

### Feature 4 — Multi-threading (Kotlin Coroutines)

All I/O operations (database queries, network calls, image uploads) run on background dispatchers via Kotlin Coroutines and are scoped to `viewModelScope` or `lifecycleScope` to prevent leaks. `Flow` streams propagate data reactively without blocking the main thread.

### Feature 5 — Animations (Jetpack Compose)

The app uses Compose animation APIs to enhance interactivity:
- **Favourite heart button** animates with `animateFloatAsState` scale on toggle.
- **Status badges** use colour transitions.
- **List updates** leverage Compose's default recomposition diffing for smooth insertions.

### Feature 6 — Multimedia / Image Handling

- **Gallery multi-select** via `ActivityResultContracts.PickMultipleVisualMedia` (up to 5 images per listing).
- **Coil** (`io.coil-kt:coil-compose 2.5.0`) loads remote images asynchronously with loading/error placeholder states.
- **Image messages** in chat upload directly to Firebase Storage and embed download URLs.

---

## 6. Database Design

### 6.1 Entity Relationship Overview

```
users ──────< listings >────── categories
  │               │
  │           < favourites >
  │               │
  └──< chat_messages >──< offers >
  │
  └──< reviews >
  │
  └──< notifications >
```

### 6.2 Table Schemas

#### `users`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | TEXT | PRIMARY KEY |
| `name` | TEXT | NOT NULL |
| `email` | TEXT | NOT NULL |
| `profilePicUrl` | TEXT | nullable |
| `createdAt` | INTEGER | NOT NULL |

#### `listings`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | TEXT | PRIMARY KEY |
| `title` | TEXT | NOT NULL |
| `description` | TEXT | NOT NULL |
| `price` | REAL | NOT NULL |
| `userId` | TEXT | FK → users.id CASCADE |
| `sellerName` | TEXT | NOT NULL |
| `categoryId` | TEXT | FK → categories.id |
| `location` | TEXT | nullable |
| `imageUrls` | TEXT | comma-separated URLs |
| `createdAt` | INTEGER | NOT NULL |
| `isSold` | INTEGER | DEFAULT 0 |
| `status` | TEXT | AVAILABLE / PENDING / RESERVED / SOLD |

#### `chat_messages`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | TEXT | PRIMARY KEY |
| `chatRoomId` | TEXT | NOT NULL (indexed) |
| `senderId` | TEXT | FK → users.id CASCADE |
| `receiverId` | TEXT | FK → users.id CASCADE |
| `listingId` | TEXT | FK → listings.id CASCADE |
| `message` | TEXT | NOT NULL |
| `timestamp` | INTEGER | NOT NULL (indexed) |
| `isRead` | INTEGER | DEFAULT 0 |
| `offerId` | TEXT | nullable |
| `imageUrl` | TEXT | nullable |
| `messageType` | TEXT | TEXT / OFFER / IMAGE |

#### `offers`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | TEXT | PRIMARY KEY |
| `listingId` | TEXT | FK → listings.id CASCADE |
| `buyerId` | TEXT | NOT NULL |
| `sellerId` | TEXT | NOT NULL |
| `amount` | REAL | NOT NULL |
| `status` | TEXT | PENDING / ACCEPTED / REJECTED |
| `timestamp` | INTEGER | NOT NULL |

#### `favourites`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT |
| `userId` | TEXT | FK → users.id CASCADE |
| `listingId` | TEXT | FK → listings.id CASCADE |
| `createdAt` | INTEGER | NOT NULL |
| — | — | UNIQUE(userId, listingId) |

#### `reviews`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | TEXT | PRIMARY KEY |
| `listingId` | TEXT | FK → listings.id CASCADE |
| `reviewerId` | TEXT | FK → users.id CASCADE |
| `revieweeId` | TEXT | FK → users.id CASCADE |
| `rating` | INTEGER | 1–5 |
| `comment` | TEXT | NOT NULL |
| `timestamp` | INTEGER | NOT NULL |
| `role` | TEXT | BUYER / SELLER |

#### `notifications`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | TEXT | PRIMARY KEY |
| `userId` | TEXT | FK → users.id CASCADE (indexed) |
| `title` | TEXT | NOT NULL |
| `message` | TEXT | NOT NULL |
| `type` | TEXT | OFFER_MADE / OFFER_ACCEPTED / PRICE_CHANGE / STATUS_CHANGE |
| `referenceId` | TEXT | listingId or offerId |
| `timestamp` | INTEGER | NOT NULL (indexed) |
| `isRead` | INTEGER | DEFAULT 0 |

#### `categories`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | TEXT | PRIMARY KEY |
| `name` | TEXT | NOT NULL |
| `icon` | TEXT | nullable |

---

## 7. Firebase & Cloud Integration

### 7.1 Firebase Services

| Service | Usage |
|---------|-------|
| **Firebase Authentication** | Anonymous sign-in; UID-based user identity; display name management |
| **Cloud Firestore** | 7 collections; real-time snapshot listeners; offline persistence |
| **Firebase Storage** | Listing image hosting; chat image upload; public download URL generation |

### 7.2 Firestore Collections

```
listings/          chat_messages/     chatRooms/
offers/            notifications/     reviews/
users/
```

### 7.3 Anonymous Authentication Design

Firebase Anonymous Authentication is used so that users can participate immediately without registration friction. A user-friendly display name is derived from the last 4 hex digits of the UID:

```kotlin
fun getCurrentUserName(): String? {
    val name = auth.currentUser?.displayName
    if (!name.isNullOrBlank()) return name
    val uid = getCurrentUserId() ?: return null
    return "User_${uid.takeLast(4).uppercase()}"   // e.g. "User_7A2B"
}
```

---

## 8. UI/UX Design

### 8.1 Design System — Material 3

The app uses the **Material 3** design system (Material You) via Jetpack Compose:
- Dynamic colour theming (adapts to device wallpaper on Android 12+)
- Light and dark mode support
- Consistent typography scale
- Surface tones and elevation

### 8.2 Reusable Components (`MarketplaceComponents.kt`)

| Component | Purpose |
|-----------|---------|
| `ListingCard` | 2-column grid card with image, price, seller, status badge, favourite button |
| `ListingStatusBadge` | Colour-coded chip: green (Available), orange (Pending), blue (Reserved), red (Sold) |
| `PriceTag` | Formatted `$X.XX` price display |
| `MessageBubble` | Chat bubble with left/right alignment, timestamp, image support |
| `SearchBar` | Text field with clear and search icons |
| `EmptyState` | Icon + title + message + optional action button |

### 8.3 Screen Inventory

| Screen | Key UI Elements |
|--------|----------------|
| **Home** | 2-column grid, search bar, category chips, filter panel, notification badge, FAB |
| **Listing Detail** | Image gallery, price, status badge, favourite toggle, chat CTA, related listings |
| **Create Listing** | Form fields, image picker row (up to 5), category dropdown, submit button |
| **Profile** | User info, edit name dialog, My Listings shortcut |
| **My Listings** | Grid of own listings with status, delete, and chat inquiry buttons |
| **My Favourites** | Grid of bookmarked listings with remove option |
| **All Chats** | Conversation list with last message and unread indicator |
| **Chat** | Real-time message list, text input, offer dialog, review dialog, image send |
| **Listing Chats** | Buyer inquiry list for a specific listing (seller view) |
| **Public Profile** | User's listings, average star rating, review history |
| **Notifications** | Chronological notification list, unread highlighting, mark read |
| **Camera** | CameraX preview, capture button, permission handling |

---

## 9. Third-Party Libraries

All third-party libraries are listed here as required by the project specification.

| Library | Version | Purpose |
|---------|---------|---------|
| **Jetpack Compose BOM** | 2024.10.00 | Declarative UI framework |
| **androidx.navigation:navigation-compose** | 2.8.3 | Type-safe in-app navigation |
| **androidx.lifecycle:lifecycle-viewmodel-compose** | 2.8.7 | ViewModel integration with Compose |
| **androidx.room:room-runtime / room-ktx** | 2.6.1 | Local SQLite ORM |
| **androidx.room:room-compiler (KSP)** | 2.6.1 | Code generation for Room DAOs |
| **com.google.firebase:firebase-bom** | 33.3.0 | Firebase platform BOM |
| **firebase-firestore-ktx** | (BOM) | Cloud NoSQL database & real-time sync |
| **firebase-storage-ktx** | (BOM) | Cloud file storage |
| **firebase-auth-ktx** | (BOM) | User authentication (anonymous) |
| **io.coil-kt:coil-compose** | 2.5.0 | Asynchronous image loading |
| **androidx.camera:camera-core / camera2 / camera-lifecycle / camera-view** | 1.3.4 | In-app camera via CameraX |
| **com.squareup.retrofit2:retrofit** | 2.9.0 | HTTP networking client |
| **com.squareup.retrofit2:converter-gson** | 2.9.0 | JSON serialisation for Retrofit |
| **com.squareup.okhttp3:logging-interceptor** | 4.12.0 | HTTP request/response logging |
| **org.jetbrains.kotlinx:kotlinx-coroutines-play-services** | 1.7.3 | Coroutine extensions for Firebase Tasks |
| **com.google.guava:guava** | 31.1-android | Required by CameraX |
| **androidx.compose.material:material-icons-extended** | 1.7.8 | Extended Material icon set |
| **KSP (Kotlin Symbol Processing)** | 1.9.24-1.0.20 | Annotation processing for Room |
| **Google Services plugin** | 4.4.2 | Firebase project configuration |

---

## 10. Software Engineering Practices

### 10.1 Version Control

The project is maintained in a **GitHub repository** with regular commits across the development period. Each feature was developed incrementally, with the build kept compilable at every commit.

### 10.2 Code Quality

- **Kotlin coding conventions** (Android style guide) are followed throughout — `camelCase` for functions/variables, `PascalCase` for classes, trailing lambdas, expression bodies where appropriate.
- Code is **self-documenting** with descriptive naming (e.g., `generateChatRoomId`, `markMessagesAsRead`, `getUnreadNotificationCount`).
- Complex logic (e.g., dual-write strategy, chat room ID generation, offer workflow) is documented with inline comments.
- **No deprecated APIs** are used; Jetpack Compose and CameraX are both stable-release libraries.

### 10.3 Modular Code Structure

The codebase is divided into well-defined layers:
- **Data layer** — entities, DAOs, DTOs, data sources, repositories
- **Domain layer** — clean model classes decoupled from persistence and UI concerns
- **UI layer** — screens, ViewModels, components, theme, navigation

Each screen module (`home/`, `chat/`, `create/`, etc.) is self-contained, grouping the composable screen file and its ViewModel together.

### 10.4 Coroutine & Resource Management

- All ViewModel coroutines are launched in `viewModelScope` and cancelled automatically on ViewModel clearance.
- Firestore snapshot listeners are stored as `ListenerRegistration` references and removed `onCleared()` to prevent memory leaks.
- `SharingStarted.WhileSubscribed(5000)` is used on `stateIn` flows to upstream flows when no collectors are active.

### 10.5 Error Handling

Repository methods return `Result<T>` (Kotlin stdlib), propagating success and failure to the ViewModel without throwing unhandled exceptions into the UI layer.

### 10.6 Build Configuration

- **AGP 8.6.1** with **KSP** (replacing KAPT) for faster annotation processing.
- Firebase BOM pinned to 33.3.0 for stability with Kotlin 1.9.24.
- `fallbackToDestructiveMigration()` on Room is a development convenience (noted for replacement with proper migrations in production).
- Compile SDK 35 / Target SDK 35 / Min SDK 26 provides broad device coverage.

---

## 11. Results & Evaluation

### 11.1 Features Implemented

| Feature | Status |
|---------|--------|
| Marketplace listing browse (grid, infinite scroll) | Complete |
| Multi-dimensional search & filter | Complete |
| Create listing with multi-image upload | Complete |
| In-app camera capture (CameraX) | Complete |
| Real-time chat (text, image, offer messages) | Complete |
| Formal offer workflow (make / accept / reject) | Complete |
| Listing status lifecycle (Available → Sold) | Complete |
| In-app notification system with badge | Complete |
| Favourites (add/remove, count, watchers) | Complete |
| Seller review & star-rating system | Complete |
| Public seller profile page | Complete |
| Anonymous authentication (Firebase Auth) | Complete |
| Offline-first local database (Room, 8 tables) | Complete |
| Real-time cloud sync (Firestore snapshot listeners) | Complete |
| Firebase Storage image hosting | Complete |
| Material 3 light/dark theme | Complete |

### 11.2 Mobile Feature Checklist (Course Requirement)

| Course Feature | Implementation |
|---------------|---------------|
| Database (multiple tables) | Room SQLite with 8 tables and foreign-key relationships |
| Networking | Firebase Firestore + Storage; Retrofit configured |
| Camera / Sensor | CameraX in-app camera screen |
| Multimedia | Multi-image gallery picker; Coil image loading; image chat messages |
| Animations | Compose animated favourite toggle; badge transitions |
| Multi-threading | Kotlin Coroutines throughout; `Flow`-based reactive data |

### 11.3 Known Limitations

- The Retrofit `ApiService` interface is initialised but not yet wired to a REST endpoint — it is a prepared extension point.
- Location-based geo-queries are not implemented despite the `location` field being present on listings.
- Room is configured with destructive migration (`fallbackToDestructiveMigration`) which drops data on schema changes — suitable for development, but should be replaced with proper migration scripts before production release.

---

## 12. AI Usage Declaration

AI tools were used at the following stages of the project:

| Stage | Tool | Description |
|-------|------|-------------|
| **Design & Architecture** | Claude (Anthropic) | Assisted in evaluating architectural trade-offs (e.g., manual DI container vs. Hilt; offline-first dual-write strategy vs. remote-only); helped structure the MVVM layer separation. |
| **Report Writing** | Claude Code (Anthropic) | This report was generated with the assistance of Claude Code, which analysed the repository codebase and produced the initial draft of all sections. |

**Prompts used:**

> *Design phase:* "We are building a peer-to-peer marketplace app in Android with Jetpack Compose and Firebase. What architecture pattern and DI approach would you recommend for a team of six, keeping complexity manageable while supporting offline-first with real-time sync?"

> *Report writing:* "Read the PDF documents CSD3156 Team Project Details.pdf and CSD3156 Team Project Grading Criteria.pdf and the Android repository at [path]. Scan the repository and generate a comprehensive markdown report based on the architecture found while adhering to the guidelines laid out in the PDF."

---

*Report generated for CSD3156 Mobile and Cloud Computing, Spring 2026.*
