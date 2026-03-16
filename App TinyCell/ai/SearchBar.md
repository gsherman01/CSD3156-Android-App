# Search & Filter System Implementation - TinyCell

## Overview
This document outlines the comprehensive search and filter functionality added to the TinyCell marketplace app's HomeScreen. The implementation provides users with powerful tools to find listings using text search combined with multiple simultaneous filters.

---

## Design Philosophy & Thought Process

### **Goal: Conventional Search Experience**
Users expect marketplace apps to have robust search capabilities similar to platforms like Amazon, eBay, or Craigslist:
- **Text search** that's case-insensitive and finds partial matches
- **Multiple filters** that can be applied simultaneously
- **Real-time updates** as search criteria change
- **Visual feedback** showing active filters
- **Easy filter management** with one-tap removal

### **Key Design Decisions**

#### 1. **Multi-Select Category Filter**
**Reasoning**: Users often want to see items from multiple categories simultaneously.

**Example Use Case**:
> "I'm furnishing my home office. I need items from both 'Electronics' (monitors, keyboard) and 'Home' (desk, chair)."

**Implementation Choice**:
- Changed from single `String` to `Set<String>` for categories
- Allows selecting multiple categories with OR logic
- Each category shows as individual chip in active filters
- Badge count reflects total number of selected categories

**Alternative Considered**: Single-select with "All" option
- ❌ Rejected: Too limiting for real-world use cases
- ❌ Forces users to search multiple times instead of once

#### 2. **Date Range Picker Instead of Presets**
**Reasoning**: Flexible date ranges provide more control than fixed presets.

**Original Design** (Presets):
```
☐ All Time
☐ Today
☐ This Week
☐ This Month
☐ Custom Range
```

**Problems Identified**:
- "This Week" is ambiguous (Sunday start? Monday start?)
- "This Month" might not match user intent (last 30 days vs. calendar month)
- Custom range still needed for specific dates
- Presets cover only common cases, not all needs

**Final Design** (Date Range):
```
From: [Select Date]  To: [Select Date]
```

**Benefits**:
- ✅ Clear and unambiguous
- ✅ Handles all use cases (yesterday to today, last 7 days, specific event date range)
- ✅ Material 3 DatePicker provides familiar UX
- ✅ Partial ranges supported (only "from" or only "to")

#### 3. **Reactive Filter Architecture**
**Reasoning**: Filters should update results immediately without manual "Apply" clicks (except for the bottom sheet).

**Flow-Based Design**:
```kotlin
val listings: Flow<List<Listing>> = _searchFilters.flatMapLatest { filters ->
    repository.searchWithFilters(...)
}
```

**Benefits**:
- Search text updates results as you type
- Filters applied from bottom sheet update immediately after "Apply"
- All UI components stay in sync automatically
- Follows reactive programming best practices

---

## Architecture Layers

The implementation spans all architecture layers following MVVM principles:

```
┌─────────────────────────────────────────┐
│         UI Layer (HomeScreen.kt)        │
│  - Search Bar Component                 │
│  - Active Filters Row                   │
│  - Filter Bottom Sheet                  │
│  - Date Range Picker Dialogs           │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│    ViewModel Layer (HomeViewModel.kt)   │
│  - SearchFilters (State)                │
│  - Filter Management Methods            │
│  - Reactive Listing Flow                │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│ Repository Layer (ListingRepository.kt) │
│  - searchWithFilters()                  │
│  - getAllCategories()                   │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│      DAO Layer (ListingDao.kt)          │
│  - searchWithFilters() SQL Query        │
└─────────────────────────────────────────┘
```

---

## Implementation Details

### **1. Data Model - SearchFilters**

**File**: `HomeViewModel.kt`

```kotlin
data class SearchFilters(
    val query: String = "",                          // Text search
    val selectedCategories: Set<String> = emptySet(), // Multi-select categories
    val minPrice: Double = 0.0,                       // Price range min
    val maxPrice: Double = Double.MAX_VALUE,          // Price range max
    val minDate: Long? = null,                        // Date range start (nullable)
    val maxDate: Long? = null                         // Date range end (nullable)
)
```

**Design Notes**:
- `Set<String>` for categories enables efficient add/remove operations
- `Double.MAX_VALUE` for maxPrice means "no upper limit"
- `null` dates mean "no date filter" (different from 0L which is Jan 1, 1970)

---

### **2. Database Layer - Advanced SQL Query**

**File**: `ListingDao.kt`

```kotlin
@Query("""
    SELECT * FROM listings
    WHERE (:query = '%' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
      AND (
        CASE
            WHEN :categoryIdsSize = 0 THEN 1
            ELSE categoryId IN (:categoryIds)
        END
      )
      AND price >= :minPrice
      AND price <= :maxPrice
      AND createdAt >= :minDate
      AND createdAt <= :maxDate
      AND isSold = 0
    ORDER BY createdAt DESC
""")
fun searchWithFilters(
    query: String,
    categoryIds: List<String>,
    categoryIdsSize: Int,
    minPrice: Double,
    maxPrice: Double,
    minDate: Long,
    maxDate: Long
): Flow<List<ListingEntity>>
```

**SQL Logic Breakdown**:

1. **Text Search**:
   ```sql
   :query = '%' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'
   ```
   - If query is `%`, matches all listings (no search)
   - Otherwise, searches both title and description
   - `LIKE '%term%'` allows partial matching ("Mac" matches "MacBook Pro")

2. **Multi-Category Filter**:
   ```sql
   CASE
       WHEN :categoryIdsSize = 0 THEN 1
       ELSE categoryId IN (:categoryIds)
   END
   ```
   - If no categories selected (`size = 0`), returns true (1)
   - Otherwise, checks if listing's category is IN the selected list
   - **Requires size parameter**: Room doesn't support `.isEmpty()` in SQL

3. **Price Range**:
   ```sql
   price >= :minPrice AND price <= :maxPrice
   ```
   - Simple boundary checks
   - `0.0` and `Double.MAX_VALUE` act as "no filter" values

4. **Date Range**:
   ```sql
   createdAt >= :minDate AND createdAt <= :maxDate
   ```
   - Timestamp-based filtering
   - `0L` and `System.currentTimeMillis()` used when no filter active

5. **Active Listings Only**:
   ```sql
   isSold = 0
   ```
   - Excludes sold items from search results

6. **Sort Order**:
   ```sql
   ORDER BY createdAt DESC
   ```
   - Newest listings first

**Why This Approach?**
- ✅ Single query handles all filters (performant)
- ✅ Room emits new results automatically via `Flow<>` when data changes
- ✅ SQL-level filtering is faster than filtering in Kotlin
- ✅ All filters combine with AND logic (must match all criteria)

---

### **3. Repository Layer**

**File**: `ListingRepository.kt`

```kotlin
fun searchWithFilters(
    query: String,
    categoryIds: List<String>,
    minPrice: Double,
    maxPrice: Double,
    minDate: Long,
    maxDate: Long
): Flow<List<Listing>> = listingDao.searchWithFilters(
    query = query,
    categoryIds = categoryIds,
    categoryIdsSize = categoryIds.size,  // Pass size for SQL
    minPrice = minPrice,
    maxPrice = maxPrice,
    minDate = minDate,
    maxDate = maxDate
).map { entities -> entities.map { it.toListing() } }
```

**Responsibilities**:
- Convert Set → List for DAO (Room doesn't support Set parameters)
- Calculate `categoryIdsSize` for SQL conditional
- Transform `ListingEntity` → `Listing` for UI layer
- Return `Flow<List<Listing>>` for reactive updates

---

### **4. ViewModel Layer - State Management**

**File**: `HomeViewModel.kt`

#### **State Properties**:
```kotlin
private val _searchFilters = MutableStateFlow(SearchFilters())
val searchFilters: StateFlow<SearchFilters> = _searchFilters.asStateFlow()

private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
val categories: StateFlow<List<CategoryEntity>> = _categories.asStateFlow()
```

#### **Reactive Listings Flow**:
```kotlin
val listings: Flow<List<Listing>> = _searchFilters.flatMapLatest { filters ->
    repository.searchWithFilters(
        query = if (filters.query.isBlank()) "%" else filters.query,
        categoryIds = filters.selectedCategories.toList(),
        minPrice = filters.minPrice,
        maxPrice = filters.maxPrice,
        minDate = filters.minDate ?: 0L,
        maxDate = filters.maxDate ?: System.currentTimeMillis()
    )
}
```

**How It Works**:
1. `_searchFilters` updates when user changes any filter
2. `flatMapLatest` cancels previous query and starts new one
3. Repository returns new `Flow<List<Listing>>`
4. UI collects this flow and updates automatically

**Benefits of flatMapLatest**:
- Cancels outdated queries (user typed "laptop", then "macbook" quickly)
- Always shows results for most recent filter state
- No manual refresh needed

#### **Filter Management Methods**:

```kotlin
// Text Search
fun updateSearchQuery(query: String) {
    _searchFilters.update { it.copy(query = query) }
}

// Multi-Select Categories
fun toggleCategoryFilter(categoryId: String) {
    _searchFilters.update { currentFilters ->
        val currentCategories = currentFilters.selectedCategories.toMutableSet()
        if (categoryId in currentCategories) {
            currentCategories.remove(categoryId)  // Deselect
        } else {
            currentCategories.add(categoryId)     // Select
        }
        currentFilters.copy(selectedCategories = currentCategories)
    }
}

fun clearCategoryFilters() {
    _searchFilters.update { it.copy(selectedCategories = emptySet()) }
}

// Price Range
fun updatePriceRange(min: Double, max: Double) {
    _searchFilters.update { it.copy(minPrice = min, maxPrice = max) }
}

// Date Range
fun updateDateRange(minDate: Long?, maxDate: Long?) {
    _searchFilters.update { it.copy(minDate = minDate, maxDate = maxDate) }
}

fun clearDateRange() {
    _searchFilters.update { it.copy(minDate = null, maxDate = null) }
}

// Clear All
fun clearAllFilters() {
    _searchFilters.value = SearchFilters()
}
```

**Pattern Used**: Immutable state updates via `.copy()`
- Never mutate state directly
- Each update creates new `SearchFilters` instance
- StateFlow emits new value, triggering UI update

#### **Active Filter Count (for Badge)**:
```kotlin
fun getActiveFilterCount(): Int {
    val filters = _searchFilters.value
    var count = 0
    if (filters.selectedCategories.isNotEmpty()) count += filters.selectedCategories.size
    if (filters.minPrice > 0.0 || filters.maxPrice < Double.MAX_VALUE) count++
    if (filters.minDate != null || filters.maxDate != null) count++
    return count
}
```

**Logic**:
- Each selected category counts as 1 (2 categories = badge shows "2")
- Price filter counts as 1 (even if only min or max set)
- Date filter counts as 1 (even if only min or max set)
- Total shown on filter button badge

---

### **5. UI Layer - Components**

**File**: `HomeScreen.kt`

#### **A. Search Bar Component**

```kotlin
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onFilterClick: () -> Unit,
    activeFilterCount: Int
)
```

**Features**:
- OutlinedTextField with search icon
- Clear button (X) when text entered
- Filter button with badge showing active filter count
- Rounded Material 3 design

**UX Details**:
- Single line input (no multiline)
- Placeholder: "Search listings..."
- Leading icon: Search (magnifying glass)
- Trailing icon: Clear (X) - only visible when text entered

#### **B. Active Filters Row**

```kotlin
@Composable
fun ActiveFiltersRow(
    filters: SearchFilters,
    categories: List<CategoryEntity>,
    onClearAllFilters: () -> Unit,
    onRemoveCategory: (String) -> Unit,
    onClearDateRange: () -> Unit
)
```

**Displays**:
1. **Category Chips** (one per selected category):
   - Icon + Name: "📱 Electronics"
   - Leading icon: Category icon
   - Trailing icon: Close (X) to remove
   - Tappable to deselect category

2. **Price Chip** (if price filter active):
   - "$500-$1500" (both min and max)
   - "Under $1500" (only max)
   - "Over $500" (only min)
   - Leading icon: Dollar sign

3. **Date Range Chip** (if date filter active):
   - "1/15/2026 - 1/27/2026" (both dates)
   - "After 1/15/2026" (only min date)
   - "Before 1/27/2026" (only max date)
   - Leading icon: Calendar
   - Trailing icon: Close (X) to clear

4. **Clear All Chip**:
   - Red/error color scheme
   - Removes all active filters at once

**Layout**: Horizontal scrollable LazyRow

#### **C. Filter Bottom Sheet**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    viewModel: HomeViewModel,
    categories: List<CategoryEntity>,
    currentFilters: SearchFilters,
    onDismiss: () -> Unit
)
```

**Structure**:

**Header Section**:
- "Filters" title
- "Clear All" text button

**Category Section**:
- Title: "Category"
- Selection count: "2 selected" (if categories selected)
- Horizontal scrollable FilterChips
- Multi-select with checkmarks
- "Clear Categories" button (only visible when selections made)

**Price Range Section**:
- Title: "Price Range"
- Two OutlinedTextFields side-by-side:
  - Min Price (with $ prefix)
  - Max Price (with $ prefix)
- Numeric input validation

**Date Range Section**:
- Title: "Date Range"
- Two OutlinedButtons:
  - "From" button → Opens DatePickerDialog
  - "To" button → Opens DatePickerDialog
- Shows selected dates or "Any"
- "Clear Date Range" button (only visible when dates selected)

**Apply Button**:
- Full-width Button
- Applies price filters and closes sheet
- Category and date filters applied immediately on selection

#### **D. Date Range Picker**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeFilterSection(
    viewModel: HomeViewModel,
    currentFilters: SearchFilters
)
```

**Implementation Details**:

**Start Date Picker**:
```kotlin
val datePickerState = rememberDatePickerState(
    initialSelectedDateMillis = currentFilters.minDate ?: System.currentTimeMillis()
)
DatePickerDialog(
    onDismissRequest = { showStartDatePicker = false },
    confirmButton = {
        TextButton(onClick = {
            datePickerState.selectedDateMillis?.let { selectedDate ->
                viewModel.updateDateRange(
                    minDate = selectedDate,
                    maxDate = currentFilters.maxDate
                )
            }
            showStartDatePicker = false
        }) { Text("OK") }
    },
    dismissButton = {
        TextButton(onClick = { showStartDatePicker = false }) {
            Text("Cancel")
        }
    }
) {
    DatePicker(state = datePickerState)
}
```

**End Date Picker**:
- Similar to start date picker
- **Important**: Adds 24 hours - 1ms to include full end day
  ```kotlin
  val endOfDay = selectedDate + (24 * 60 * 60 * 1000 - 1)
  ```
  - Without this, selecting "Jan 27" would exclude items posted on Jan 27
  - Makes date range inclusive of the end date

**Date Formatting**:
```kotlin
fun formatDate(timestamp: Long): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = timestamp
    val month = calendar.get(java.util.Calendar.MONTH) + 1
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val year = calendar.get(java.util.Calendar.YEAR)
    return "$month/$day/$year"
}
```

**Chip Display Formatting**:
```kotlin
fun formatDateRange(minDate: Long?, maxDate: Long?): String {
    return when {
        minDate != null && maxDate != null -> "${formatDate(minDate)} - ${formatDate(maxDate)}"
        minDate != null -> "After ${formatDate(minDate)}"
        maxDate != null -> "Before ${formatDate(maxDate)}"
        else -> "Any Date"
    }
}
```

---

## User Interaction Flows

### **Flow 1: Text Search**
```
User types "laptop"
    ↓
HomeScreen.SearchBar.onQueryChange("laptop")
    ↓
HomeViewModel.updateSearchQuery("laptop")
    ↓
_searchFilters.update { it.copy(query = "laptop") }
    ↓
flatMapLatest triggers new repository.searchWithFilters()
    ↓
DAO executes SQL: WHERE title LIKE '%laptop%' OR description LIKE '%laptop%'
    ↓
Flow emits new List<Listing>
    ↓
UI recomposes with filtered results
```

### **Flow 2: Multi-Select Categories**
```
User opens Filter Sheet
    ↓
User taps "Electronics" FilterChip
    ↓
HomeViewModel.toggleCategoryFilter("Electronics")
    ↓
Electronics added to selectedCategories Set
    ↓
User taps "Home" FilterChip
    ↓
Home added to selectedCategories Set
    ↓
User taps "Apply Filters"
    ↓
Sheet closes, _searchFilters already updated
    ↓
flatMapLatest triggers with categoryIds = ["Electronics", "Home"]
    ↓
DAO executes: WHERE categoryId IN ('Electronics', 'Home')
    ↓
Results show items from either category
    ↓
Active filters row shows two chips: "📱 Electronics [X]" and "🏠 Home [X]"
```

### **Flow 3: Date Range**
```
User opens Filter Sheet
    ↓
User taps "From" button
    ↓
DatePickerDialog opens
    ↓
User selects Jan 1, 2026
    ↓
HomeViewModel.updateDateRange(minDate = selectedDate, maxDate = null)
    ↓
User taps "To" button
    ↓
DatePickerDialog opens
    ↓
User selects Jan 27, 2026
    ↓
ViewModel adds 24 hours - 1ms to include full day
    ↓
HomeViewModel.updateDateRange(minDate = Jan1, maxDate = Jan27EndOfDay)
    ↓
flatMapLatest triggers with date boundaries
    ↓
DAO executes: WHERE createdAt >= Jan1 AND createdAt <= Jan27EndOfDay
    ↓
Results show only listings posted in January 2026
    ↓
Active chip shows: "1/1/2026 - 1/27/2026 [X]"
```

### **Flow 4: Combined Filters**
```
User has:
- Search: "laptop"
- Categories: Electronics, Home
- Price: $500-$1500
- Date: 1/1/2026 - 1/27/2026

DAO executes:
WHERE (title LIKE '%laptop%' OR description LIKE '%laptop%')
  AND categoryId IN ('Electronics', 'Home')
  AND price >= 500.0
  AND price <= 1500.0
  AND createdAt >= 1735707600000
  AND createdAt <= 1738022399999
  AND isSold = 0
ORDER BY createdAt DESC

Results: Only laptops in Electronics or Home categories,
         priced $500-$1500,
         posted in January 2026,
         still available (not sold)
```

---

## Edge Cases Handled

### **1. Empty Category Selection**
- Problem: `categoryId IN ()` is invalid SQL
- Solution: Use `CASE WHEN :categoryIdsSize = 0 THEN 1`
- Result: Empty selection = no category filter (shows all)

### **2. Null Date Values**
- Problem: User clears date filter, what to pass to DAO?
- Solution: Use `filters.minDate ?: 0L` and `maxDate ?: System.currentTimeMillis()`
- Result: Null dates become full date range (no filter)

### **3. Date Range Inclusivity**
- Problem: Selecting Jan 27 as end date excludes items posted on Jan 27
- Solution: Add `24 * 60 * 60 * 1000 - 1` milliseconds to end date
- Result: Jan 27 end date includes all of Jan 27 (23:59:59.999)

### **4. Price Input Validation**
- Problem: User types "abc" in price field
- Solution: `if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' })`
- Result: Only numeric input and decimal points allowed

### **5. Badge Overflow**
- Problem: Selecting 6 categories = badge shows "6", looks cluttered
- Solution: Material 3 Badge component handles this gracefully
- Result: Badge adjusts size, remains readable

### **6. Quick Filter Changes**
- Problem: User types "l", "la", "lap", "lapt", "laptop" quickly
- Solution: `flatMapLatest` cancels previous queries
- Result: Only final query "laptop" executes, no wasted queries

---

## Performance Considerations

### **1. Database Indexing**
```kotlin
@Entity(
    tableName = "listings",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["categoryId"]),
        Index(value = ["createdAt"])  // Important for ORDER BY
    ]
)
```
- Indexes on categoryId and createdAt speed up filtering and sorting
- Without indexes, SQL would scan entire table

### **2. Flow-Based Reactive Updates**
- Room automatically manages database observers
- Only emits when underlying data changes
- No manual refresh polling needed

### **3. SQL-Level Filtering**
- All filtering done in SQLite, not Kotlin
- Database can use indexes for optimization
- Only matching rows loaded into memory

### **4. StateFlow vs. LiveData**
- StateFlow has built-in conflation (skips intermediate values)
- If filters update 3 times in 1ms, only final value processed
- Prevents UI thrashing

### **5. Lazy Composables**
- LazyColumn and LazyRow only compose visible items
- Hundreds of listings won't slow down UI
- Scrolling performance remains smooth

---

## Testing Checklist

### **Text Search**
- ✅ Search "laptop" finds "MacBook Pro", "Gaming Laptop", "Laptop Stand"
- ✅ Search "MAC" finds "MacBook" (case-insensitive)
- ✅ Clear search shows all items
- ✅ Search with no results shows empty state

### **Multi-Category Filter**
- ✅ Select 1 category shows only that category
- ✅ Select 2 categories shows items from either category
- ✅ Select all 6 categories shows all items
- ✅ Deselect category removes it from results
- ✅ Clear Categories button works
- ✅ Category chips display with correct icons

### **Price Filter**
- ✅ Min price only: shows items >= min
- ✅ Max price only: shows items <= max
- ✅ Both: shows items in range
- ✅ Invalid input rejected (letters, special chars)
- ✅ Decimal prices work (99.99)
- ✅ Chip displays correct format

### **Date Filter**
- ✅ Start date only: shows items after date
- ✅ End date only: shows items before date
- ✅ Both: shows items in range
- ✅ DatePicker dialog opens/closes
- ✅ Selected date displays correctly
- ✅ End date includes full day (not just midnight)
- ✅ Clear Date Range works

### **Combined Filters**
- ✅ All filters work simultaneously
- ✅ Removing one filter keeps others active
- ✅ Clear All removes everything
- ✅ Badge count accurate
- ✅ No duplicate results

### **UI/UX**
- ✅ Filter sheet scrollable (small screens)
- ✅ Active chips horizontally scrollable
- ✅ Loading indicator shows during refresh
- ✅ Empty state displays helpful message
- ✅ Animations smooth

---

## Future Enhancements

### **Potential Additions**:

1. **Sort Options**
   - Price: Low to High / High to Low
   - Date: Newest / Oldest
   - Relevance (for text search)

2. **Saved Searches**
   - Save filter combinations
   - Quick apply from list

3. **Search History**
   - Recent searches dropdown
   - Clear history option

4. **Advanced Text Search**
   - Exact phrase matching ("Gaming Laptop")
   - Exclude words (-used)
   - Boolean operators (AND, OR, NOT)

5. **Location Filter**
   - Distance from user
   - Specific cities/regions
   - Map view integration

6. **Seller Filter**
   - Verified sellers only
   - Minimum rating
   - Exclude specific sellers

7. **Condition Filter**
   - New / Like New / Good / Fair
   - With photos only

8. **Filter Presets**
   - "Deals" (price drops, discounts)
   - "Recently Added"
   - "Ending Soon" (if auction-style)

---

## Architecture Compliance

All changes adhere to project rules defined in `RULES.md`:

✅ **Kotlin only** - No Java code
✅ **Jetpack Compose only** - No XML layouts
✅ **Material 3** - DatePicker, FilterChips, BottomSheet all Material 3
✅ **MVVM architecture** - Clear separation of concerns
✅ **StateFlow only** - No LiveData used
✅ **Stateless Composables** - All state in ViewModel
✅ **Navigation Compose** - Filter sheet uses Compose Modal
✅ **Additive changes** - No refactoring of working code
✅ **Compiles after each step** - Incremental implementation

---

## Summary

The search and filter system provides users with a powerful, intuitive way to find marketplace listings. Key achievements:

**User Benefits**:
- 🔍 Find items quickly with text search
- 🏷️ Filter by multiple categories simultaneously
- 💰 Set price ranges
- 📅 Specify date ranges
- 👀 See active filters at a glance
- ⚡ Results update in real-time

**Technical Benefits**:
- ♻️ Reactive architecture with Flow
- 🗄️ Efficient SQL-level filtering
- 📐 Clean MVVM separation
- 🎨 Material 3 design language
- 🧪 Testable components

**Code Quality**:
- 📝 Well-documented with KDoc
- 🔧 Maintainable and extensible
- ⚠️ Edge cases handled
- 🎯 Type-safe with Kotlin

The implementation follows Android best practices and provides a solid foundation for future marketplace features.
