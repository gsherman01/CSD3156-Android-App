/** * TODO: Room Integration & Data Layer Completion
 *
 * 1. [DEPENDENCY INJECTION]
 *    - Currently, AppRepository requires an 'AppDao' in its constructor.
 *    - ACTION: Set up a Manual Provider or Hilt Module to provide the 'AppDatabase'
 *      instance and 'AppDao' to the Repository.
 *
 * 2. [DATABASE INITIALIZATION]
 *    - ACTION: In the Application class or a Dependency Provider, initialize
 *      Room.databaseBuilder().
 *    - RISK: Ensure the database name matches across the project to avoid multiple DB files.
 *
 * 3. [DATA SEEDING]
 *    - ACTION: Use the 'mockData' list defined above to prepopulate the database
 *      on the first launch (RoomDatabase.Callback).
 *
 * 4. [VIEWMODEL INTEGRATION]
 *    - ACTION: Create a ViewModel for the Home Screen that:
 *      a) Accepts AppRepository as a parameter.
 *      b) Uses 'allItems.stateIn()' to expose a StateFlow<List<AppEntity>> to the UI.
 *      c) Implements a function to call 'addItem()' using viewModelScope.
 *
 * 5. [STATELESS COMPOSABLES]
 *    - ACTION: Update HomeScreen.kt to observe the StateFlow and pass data/events
 *      down to stateless UI components.
 */

package com.example.tinycell.data.repository

import com.example.tinycell.data.local.dao.AppDao
import com.example.tinycell.data.local.entity.AppEntity
import kotlinx.coroutines.flow.Flow

// Add this to your AppRepository to provide initial development data
val mockData = listOf(
    AppEntity(id = 1, name = "Cell Membrane"),
    AppEntity(id = 2, name = "Nucleus"),
    AppEntity(id = 3, name = "Mitochondria"),
    AppEntity(id = 4, name = "Ribosomes"),
    AppEntity(id = 5, name = "Endoplasmic Reticulum")
)


class AppRepository(private val appDao: AppDao) {

    val allItems: Flow<List<AppEntity>> = appDao.getAllItems()

    suspend fun addItem(name: String) {
        appDao.insertItem(AppEntity(name = name))
    }
}
