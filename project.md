Summary of Project deliverables by ChatGPT :  
[https://chatgpt.com/s/t\_69624a35eb548191938c9de179f50203](https://chatgpt.com/s/t_69624a35eb548191938c9de179f50203) 

Project : Carousell imitation.  
**MiniSell SG** – A local peer-to-peer marketplace app

### **Core Idea**

A **simplified Carousell-like Android app** that allows users to:

* Browse listings

* Post items for sale

* Chat with sellers

* Save favourites

* Use Singapore-relevant context (location, categories, mobile usage)

com.example.minicarousell  
├── MainActivity.kt  
├── MarketplaceApp.kt  
├── data  
│   ├── model  
│   │   ├── User.kt  
│   │   ├── Listing.kt  
│   │   └── Category.kt  
│   └── repository  
│       └── ListingRepository.kt  
├── ui  
│   ├── navigation  
│   │   └── NavGraph.kt  
│   └── screens  
│       ├── home  
│       │   ├── HomeScreen.kt  
│       │   └── HomeViewModel.kt  
│       ├── detail  
│       │   ├── ListingDetailScreen.kt  
│       │   └── ListingDetailViewModel.kt  
│       ├── create  
│       │   ├── CreateListingScreen.kt  
│       │   └── CreateListingViewModel.kt  
│       └── profile  
│           ├── ProfileScreen.kt  
│           └── ProfileViewModel.kt

# 

# **2\. Feature Selection (Mapped to Requirements)**

You must hit **3+ mobile features**. This app can comfortably hit **5–6**.

## **2.1 Chosen Mobile Features (Recommended)**

### **✅ 1\. Database (Mandatory, Core Feature)**

**Room (SQLite)** with multiple tables:

Tables:

* `User`  
* `Listing`  
* `Category`  
* `ChatMessage`  
* `Favourite`

Why this scores well:

* Multiple tables ✔

* Persistent storage ✔

* Real app relevance ✔

---

### **✅ 2\. Camera & Media**

* Take photos for listings

* Choose from gallery

* Compress images before saving

APIs:

* CameraX

* MediaStore

---

### **✅ 3\. Networking**

* Fetch mock listings from a REST API

  * Public mock API

  * Firebase / Supabase

  * Or self-hosted simple backend

Demonstrates:

* HTTP calls

* JSON parsing

* Async data handling

---

### **✅ 4\. Multi-threading**

* Coroutines / Executors used for:

  * Image loading

  * Database queries

  * Network calls

Make this explicit in documentation.

---

### **✅ 5\. Location (Singapore Context)**

* GPS location tagging for listings

* Show approximate distance (e.g. “2.3 km away”)

APIs:

* FusedLocationProviderClient

---

### **(Optional Bonus)**

* Animations (RecyclerView item animations)

* Push notifications (new message)

* Canvas (custom price tags / badges)

---

## **3\. App Functional Scope (What the App Can Do)**

### **3.1 User Flow (Core Screens)**

#### **1\. Home / Browse Listings**

* RecyclerView grid/list  
* Filter by category  
* Search bar (basic)

#### **2\. Listing Detail Page**

* Image carousel  
* Item description  
* Seller info  
* Location  
* “Chat” & “Favourite” buttons

#### **3\. Create Listing**

* Take/upload photos  
* Enter price, title, description  
* Choose category  
* Auto-tag location

#### **4\. Chat (Simplified)**

* Buyer ↔ Seller text messages  
* Stored locally or via backend  
* No real-time requirement (polling is OK)

#### **5\. Profile**

* User’s listings  
* Favourites  
* Edit profile (basic)

---

### **🧑‍💻 Member 1 – Tech Lead / Architecture**

**Responsibilities:**

* Project setup  
* MVVM structure  
* Navigation graph  
* Code standards

**Deliverables:**

* App skeleton  
* Base classes  
* CI setup (optional)

---

### **🎨 Member 2 – UI / UX Engineer**

**Responsibilities:**

* XML layouts / Compose  
* RecyclerViews  
* Animations  
* Responsive UI

**Deliverables:**

* Home screen  
* Listing detail UI  
* Create listing UI

---

### **🗄️ Member 3 – Database & Persistence**

**Responsibilities:**

* Room schema  
* DAOs  
* Entity relationships  
* Favourite system

**Deliverables:**

* Database implementation  
* Local data caching

---

### **🌐 Member 4 – Networking & Chat**

**Responsibilities:**

* REST API integration  
* JSON parsing  
* Chat system  
* Background threading

**Deliverables:**

* Network layer  
* Messaging logic

---

### **📷 Member 5 – Camera, Media & Location**

**Responsibilities:**

* Camera integration  
* Image handling  
* GPS tagging  
* Permissions handling

**Deliverables:**

* Photo upload  
* Location services

---

## **6\. Week-by-Week Scope (Half Trimester)**

### **Week 1 – Planning & Setup**

* Finalize features  
* Repo \+ CI  
* Wireframes  
* Database schema

---

### **Week 2 – Core Functionality**

* Listing browsing  
* Database CRUD  
* Camera integration

---

### **Week 3 – Advanced Features**

* Networking  
* Chat  
* Location services

---

### **Week 4 – Polish & Stability**

* UI polish  
* Bug fixes  
* Documentation  
* Demo prep

---

**4\. Architecture (Grading-Friendly)**

Use a **clean, explainable architecture**:

### **Recommended:**

**MVVM**

* View (Activity / Fragment)  
* ViewModel  
* Repository  
* Room / Network layer

This:

* Looks professional  
* Is expected in Android  
* Is easy to justify in report

# Week 1 : 10 Jan 2026 Saturday

, figuring out a base working template on Android Studio.

Prompt:  
“If I were to use android studios to make me this carousell app off of this project scope, how should I prompt it to have a good working project that we can add onto it. eliminating alot of the brunt work. let me know if you know more context”

## 🔹 Master Scaffold Prompt

You are an expert Android engineer.

Generate a clean, compiling Android Studio project in Kotlin using MVVM architecture.

App concept:  
A simplified Carousell-style marketplace app for Singapore.

Project requirements:  
\- Native Android app (no Unity, no hybrid frameworks)  
\- Kotlin  
\- MVVM architecture  
\- Single-activity architecture  
\- Navigation Component with fragments  
\- XML layouts (not Compose)  
\- Minimum SDK: 26  
\- Target SDK: latest stable

Core features to scaffold (with placeholder implementations):  
1\. Browse listings screen (RecyclerView with mock data)  
2\. Listing detail screen  
3\. Create listing screen (form only, no camera yet)  
4\. Profile screen

Architecture requirements:  
\- ViewModels for each screen  
\- Repository layer with fake in-memory data  
\- Data models for User, Listing, Category  
\- No networking yet (mock repository)  
\- No database yet (in-memory lists)

Other requirements:  
\- App must build and run immediately  
\- Use Material Design components  
\- Clear package structure  
\- TODO comments where future features will be added  
\- Simple navigation graph connecting all screens

Do NOT implement:  
\- Camera  
\- GPS  
\- Networking  
\- Database  
\- Authentication

Output:  
\- Explain the project structure  
\- Provide key Kotlin classes  
\- Provide example XML layouts  
\- Ensure everything compiles together

### **🔹 Prompt 2: Room Database**

`Given the existing MVVM project structure,`  
`add a Room database with multiple tables:`

`- User`  
`- Listing`  
`- Category`  
`- Favourite`

`Requirements:`  
`- Entities`  
`- DAOs`  
`- Database class`  
`- Repository update to use Room instead of in-memory data`  
`- Use suspend functions and coroutines`  
`- Ensure project still builds`

`Do not modify UI code beyond what is necessary.`

---

### **🔹 Prompt 3: Camera Integration (Isolated)**

`Add CameraX support ONLY to the Create Listing screen.`

`Requirements:`  
`- Take photo`  
`- Save photo to app storage`  
`- Return image URI to ViewModel`  
`- Handle runtime permissions`  
`- No gallery picker yet`

`Do not refactor unrelated code.`

**`Handover Summary for your Teammate:`**  
`Infrastructure Ready: Room, Retrofit, and CameraX dependencies are synced.`  
`Data Layer: CameraRepository handles the ProcessCameraProvider initialization.`  
`DI Layer: CameraViewModelFactory handles injecting the repository into the ViewModel.`  
`UI Layer: CameraScreen handles the runtime permission flow and observes the initialization state.`

`Your teammate's first task: Implement the AndroidView inside the if (isReady) block to show the live camera feed.`

---

### **🔹 Prompt 4: Networking**

`Add a simple networking layer using Retrofit.`

`Requirements:`  
`- Fetch listings from a mock REST API`  
`- Map JSON to Listing model`  
`- Repository decides between network and database`  
`- Handle errors gracefully`  
`- Use coroutines`

`Do not change UI navigation.`

# UPDATED Master prompt

You are a senior Android engineer.

Generate a clean, compiling Android Studio project using:  
\- Kotlin  
\- Jetpack Compose  
\- Material 3  
\- MVVM architecture  
\- Navigation Compose  
\- Single-activity design

Device target:  
\- Google Pixel 9  
\- API level 34+

App concept:  
A simplified Carousell-style marketplace app for Singapore.

Screens to scaffold (with mock data only):  
1\. Home screen – browse listings (LazyColumn)  
2\. Listing detail screen  
3\. Create listing screen (form only)  
4\. Profile screen

Architecture requirements:  
\- ViewModel per screen  
\- Repository layer using in-memory mock data  
\- Data models: User, Listing, Category  
\- No database  
\- No networking  
\- No camera  
\- No location  
\- No authentication

Compose requirements:  
\- Stateless composables where possible  
\- State handled in ViewModels  
\- Use Material 3 components  
\- Navigation via NavHost

Other constraints:  
\- App must build and run immediately  
\- Clear package structure  
\- TODO comments where future features (Room, Camera, Networking) will go  
\- Avoid overengineering

Output:  
\- Explain the project structure  
\- Provide full Kotlin files for key classes  
\- Ensure all imports are correct and code compiles

# Alternate method of prompting  :  3\. Exact Prompts Per Team Member (Role-Based)

Each member **only touches their slice**.  
 No one regenerates the whole app.

---

## **👤 Member 1 — Tech Lead / Integration (HARDEST)**

`Given the existing Jetpack Compose MVVM project,`  
`review and improve the architecture without changing functionality.`

`Goals:`  
`- Ensure clean separation of UI, ViewModel, Repository`  
`- Improve navigation structure`  
`- Add basic UI state classes (UiState)`  
`- Ensure consistency across screens`

`Constraints:`  
`- Do NOT add new features`  
`- Do NOT refactor unrelated code`  
`- App must still build`

`Explain any architectural decisions briefly.`

---

## **👤 Member 2 — UI / UX (EASIEST)**

`Improve the UI of the existing Jetpack Compose screens.`

`Requirements:`  
`- Enhance HomeScreen listing cards (price, image placeholder, category)`  
`- Improve spacing, typography, and colors using Material 3`  
`- Add top app bar and bottom navigation`  
`- No logic changes`

`Constraints:`  
`- Do not modify ViewModels`  
`- Use mock data only`  
`- App must still compile`

`Focus on visual polish and usability.`

---

## **👤 Member 3 — Database (Room)**

`Extend the existing project by adding Room database support.`

`Requirements:`  
`- Entities: User, Listing, Category, Favourite`  
`- DAOs for each entity`  
`- RoomDatabase class`  
`- Update Repository to use Room instead of in-memory data`  
`- Use coroutines (suspend functions)`

`Constraints:`  
`- Do NOT modify UI code unless strictly necessary`  
`- Provide migration strategy (v1 only)`  
`- App must still build`

`Add TODOs for future expansion.`

---

## **👤 Member 4 — Networking & Chat**

`Add a networking layer to the project using Retrofit.`

`Requirements:`  
`- Fetch listings from a mock REST API`  
`- JSON → Listing mapping`  
`- Repository decides between network and local cache`  
`- Handle loading and error states`

`Optional:`  
`- Simple chat model (non real-time)`

`Constraints:`  
`- No authentication`  
`- No WebSockets`  
`- No refactor of UI navigation`

`Ensure clean error handling and stability.`

---

## **👤 Member 5 — Camera & Media (HARDEST AFTER LEAD)**

`Add camera support to the Create Listing screen.`

`Requirements:`  
`- Use CameraX`  
`- Capture photo`  
`- Save image to app storage`  
`- Return image URI to ViewModel`  
`- Display preview in Compose`  
`- Handle runtime permissions`

`Constraints:`  
`- Modify CreateListing screen only`  
`- No gallery picker`  
`- No refactoring of unrelated code`

`Ensure compatibility with Pixel 9.`

# Week 1 11 Jan 2026 Sunday \+ 12 Jan Monday

Starting to Prompt on Android Studio ‘s gemini.  
[https://chatgpt.com/s/t\_6963873663608191b0198f08e241ee8b](https://chatgpt.com/s/t_6963873663608191b0198f08e241ee8b) 

What I am doing today

# **1️⃣ What To Do NEXT (Order Matters)**

## **Phase 1 — Dependency & Tooling Scaffolding (TODAY)**

You will scaffold **but not fully implement**:

| Library | Status Today |
| ----- | ----- |
| Room | ✅ Add & wire |
| Retrofit \+ OkHttp | ✅ Add & wire |
| Coroutines | ✅ Already used |
| CameraX | ✅ Add deps only |
| Location | ⏳ Later (optional) |
| Permissions helper | ⏳ Later |

---

# 

To do for Room/ database stuff  
This was suggested after I did networking stuff..  
“ Next Step: Would you like me to show how to create a unified Repository that decides whether to fetch data from the local Room database or the remote Retrofit API?”

Ops have not done camera/location yet

Things done by ChatGPT

## **📦 Features Implemented in Your Repo (Post-Fix)**

1. **Kotlin & Compose Setup**  
   * Kotlin 1.9.24 fully integrated.  
   * Jetpack Compose 1.5.14 (via Compose BOM 2024.10.00).  
   * Compose compiler compatible with Kotlin 1.9.24.  
   * Compose preview and tooling enabled for UI development.  
   * Compose navigation enabled with `androidx.navigation:navigation-compose`.

2. **Android Gradle Plugin (AGP)**  
   * AGP 8.6.1 configured.  
   * Compile SDK 36, Target SDK 36\.  
   * Min SDK 26\.

3. **Room Database Integration**  
   * Room runtime \+ KTX for coroutine support.  
   * Room compiler integrated via **KSP**, fully compatible with Kotlin 1.9.24.  
   * KSP configured as incremental annotation processor.

4. **Networking**  
   * Retrofit 2.9.0 for REST API integration.  
   * Gson converter for JSON serialization/deserialization.  
   * OkHttp Logging Interceptor 4.12.0 for network debugging.

5. **Testing Frameworks**  
   * JUnit 4.13.2 for unit testing.  
   * AndroidX Test JUnit 1.3.0 for instrumentation tests.  
   * Espresso Core 3.7.0 for UI testing.  
   * Compose UI test framework integrated (`ui-test-junit4`, `ui-test-manifest`).

6. **Project Structure & Utilities**

   * Kotlin JVM target set to 11\.  
   * Java compatibility set to 11\.  
   * All plugin versions locked in root `build.gradle.kts`.  
   * Gradle clean & cache reset steps recommended for stability.  
   * Compose enabled via `buildFeatures { compose = true }`.

What Gemini did  
Here is a summary of the critical changes made to your app/build.gradle.kts and project configuration to ensure compatibility and successful compilation:  
1\. Compose Compiler Alignment  
The Problem: You are using Kotlin 1.9.24. The modern id("org.jetbrains.kotlin.plugin.compose") is only for Kotlin 2.0+.  
The Fix: I reverted to the legacy composeOptions block and set kotlinCompilerExtensionVersion \= "1.5.14". This specific version is the only stable compiler that works with Kotlin 1.9.24.

2\. SDK and Java Version Stabilization  
The Problem: compileSdk was set to an invalid value (release(36)), and the Java version was set to 11\.  
The Fix:  
Set compileSdk and targetSdk to 35 (Stable Android 15).  
Upgraded sourceCompatibility and jvmTarget to Java 17\.  
This is required because Room 2.6.1 and modern OkHttp versions are compiled with Java 17 bytecode.

3\. Lifecycle Version Control  
The Problem: The project was trying to pull in Lifecycle 2.10.0-alpha, which is unstable and often causes "Duplicate Class" errors with older Compose versions.  
The Fix: Forced the version to 2.8.7. This is the highest stable version compatible with your current Compose Bill of Materials (BOM).

4\. KSP Integration for Room  
The Problem: Room requires an annotation processor (KSP) to generate code for your DAOs and Database. Without it, the project fails to compile because the generated implementations don't exist.  
The Fix: Applied the KSP plugin and linked ksp("androidx.room:room-compiler:2.6.1"). I also ensured the KSP version in the root build file matches your Kotlin version (1.9.24-1.0.20).

5\. Experimental API Suppression  
The Problem: Material3 APIs often trigger "Experimental Material API" errors that prevent build completion.  
The Fix: Added a global compiler argument freeCompilerArgs \+= "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api" to the kotlinOptions block to satisfy the compiler without needing annotations on every file.

6\. Dependency Cleanup  
The Problem: Overlapping versions of Retrofit (2.9.0 vs 3.0.0) and OkHttp.  
The Fix: Standardized all networking on Retrofit 2.9.0 and OkHttp 4.12.0 for maximum stability.

# 

# **2️⃣ BUILT-IN ACTION PROMPTS (What Gemini Is Good At)**

These prompts correspond to **how Gemini is best used inside Android Studio**.

---

## **🧠 Action Prompt 1 — Code Explanation / Reading Guide**

`Explain this file in plain English.`

`Describe:`  
`1. The purpose of the file`  
`2. The role it plays in the app architecture`  
`3. How data flows in and out`  
`4. How a teammate should read and extend this file`

`Do NOT suggest refactors or new features.`

`$CURRENT_FILE`

Use this for:

* ViewModels  
* Screens  
* Repositories

---

## **🧠 Action Prompt 2 — Architecture Consistency Check**

`Analyze this file for consistency with MVVM and Jetpack Compose best practices.`

`Focus on:`  
`- Separation of concerns`  
`- State ownership`  
`- Lifecycle safety`

`Do NOT generate code.`  
`Do NOT refactor.`  
`List only observations and risks.`

`$CURRENT_FILE`

---

## **🧠 Action Prompt 3 — Performance Analysis (Safe Version)**

`Analyze this file for potential performance issues.`

`Consider:`  
`- Recomposition triggers`  
`- Unnecessary object creation`  
`- Inefficient state usage`

`Do NOT optimize prematurely.`  
`Do NOT refactor.`  
`Explain findings conceptually.`

`$CURRENT_FILE`

---

## **🧠 Action Prompt 4 — Permission Analysis (You already have this one)**

(This is already excellent — keep it exactly.)

`Please analyze the following file and let me know what Android system permissions it is likely to need, and which of those are missing.`

`Please be succinct in your output.`

`Use the following format for each permission identified in this file:`

`Permission analysis...`  
`* {PERMISSION NAME}`  
`Why: (REASON WHY PERMISSION MAY BE REQUIRED)`

`Summary of missing permissions...`  
`* {PERMISSION NAME}`

`Additional comments...`  
`{ADDITIONAL OBSERVATIONS}`

`$CURRENT_FILE`

---

## **🧠 Action Prompt 5 — Dependency Identification**

`Analyze this file and list any Android libraries or Gradle dependencies it relies on or assumes.`

`Do NOT add dependencies.`  
`Do NOT generate Gradle code.`  
`Just list required libraries and why they are needed.`

`$CURRENT_FILE`

---

# **3️⃣ SAVED PROMPTS YOU SHOULD HAVE (Your Toolkit)**

These are **reusable prompts** you’ll use repeatedly throughout the project.

---

## **⭐ Saved Prompt 1 — MASTER PROJECT CONTEXT**

**Name:** `Project Context – TinyCell`

`Project: TinyCell (Carousell-style Android app)`

`Tech Stack:`  
`- Kotlin`  
`- Jetpack Compose`  
`- Material 3`  
`- MVVM architecture`  
`- StateFlow (no LiveData)`  
`- Navigation Compose`  
`- Gradle Kotlin DSL`  
`- Min SDK 26`  
`- Target device: Pixel 9`

`Architecture Rules:`  
`- One ViewModel per screen`  
`- Repository pattern`  
`- Stateless Composables`  
`- No hybrid frameworks`  
`- No refactors unless requested`

`Current State:`  
`- Base app compiles`  
`- HomeScreen shows listings`  
`- Fake data via repository`  
`- Navigation working`

`Future Features:`  
`- Room (SQLite)`  
`- Retrofit (Networking)`  
`- CameraX`  
`- Location services`

---

## **⭐ Saved Prompt 2 — Library Scaffolding Prompt**

**Name:** `Android Library Scaffolding`

`You are assisting with Android library scaffolding.`

`Goal:`  
`Prepare the project to use {LIBRARY NAME}.`

`Rules:`  
`- Do NOT modify existing UI or ViewModels`  
`- Add new files only`  
`- Use recommended Android APIs`  
`- Kotlin + Compose compatible`  
`- Min SDK 26`  
`- Leave methods stubbed`  
`- Add comments for integration points`

`Output:`  
`1. Required Gradle dependencies`  
`2. New files with minimal code`  
`3. Explanation of how it connects to existing architecture`

---

## **⭐ Saved Prompt 3 — Feature Implementation Prompt**

**Name:** `Feature Implementation – Safe`

`You are implementing a single feature in an existing Android app.`

`Feature:`  
`{FEATURE NAME}`

`Constraints:`  
`- Do NOT refactor existing code`  
`- Do NOT change architecture`  
`- ViewModel is the single source of truth`  
`- UI observes StateFlow only`  
`- Repository handles all data access`

`Generate:`  
`- ViewModel changes`  
`- Repository changes (if needed)`  
`- Minimal UI updates`

---

## **⭐ Saved Prompt 4 — CRUD Form Wizard (Compose)**

**Name:** `Compose CRUD Form Generator`

(Your example is good — keep it.)

`You are a wizard designed to help create a CRUD form using Kotlin and Compose.`

`Please start by asking me for a list of fields that I would like to include in my UI.`

`Once I have replied, generate:`  
`- A Kotlin data class`  
`- Compose UI code for the form`  
`- A repository class with placeholder persistence methods`

---

## **⭐ Saved Prompt 5 — Commit Message Generator**

**Name:** `Commit Message Generator`

(Again, yours is already perfect.)

`You are an experienced software engineer and efficient technical communicator.`  
`Summarize the given diffs into a concise commit message.`  
`Focus on specific changes.`  
`Do NOT output names, e-mail addresses, or any other personally identifiable information.`  
`Do NOT output bug IDs unless explicitly present.`

`Diff:`  
`$COMMIT_DIFF`

---

# 

# 

# **🚀 Gemini MASTER BOOTSTRAP PROMPT (VERY IMPORTANT)**

This is the **exact prompt** every teammate should paste into Gemini **at the start of a session**.

---

### **✅ Gemini Bootstrap Prompt**

`You are assisting on an Android project.`

`Please read and internalize the following files.`  
`Do NOT generate code yet.`

`Your task is to confirm understanding and wait for instructions.`

`=== RULES ===`  
`[PASTE ai/RULES.md]`

`=== PROJECT CONTEXT ===`  
`[PASTE ai/PROJECT_CONTEXT.md]`

`Once understood, reply ONLY with:`  
`"Context loaded. Ready for tasks."`

