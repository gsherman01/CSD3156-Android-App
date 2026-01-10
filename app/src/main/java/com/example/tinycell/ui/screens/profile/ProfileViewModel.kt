/*
package com.example.tinyce1ll.ui.screens.profile

import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    val username = "Demo User"
}
*/

package com.example.tinycell.ui.screens.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {

    private val _username = MutableStateFlow("Demo User")
    //public val
    val username: StateFlow<String> = _username
}
