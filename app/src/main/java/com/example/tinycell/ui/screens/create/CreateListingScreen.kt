package com.example.tinycell.ui.screens.create


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.* // Ensure ONLY Material3 is used
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tinycell.data.repository.ListingRepository

//to fix material  3 smalltopbar
/*
A Create Listing screen must always have a back affordance.

Material3 expects:
navigationIcon for back
IconButton + Icons.Default.ArrowBac
 */
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme

/**
 * [TODO_CAMERA_INTEGRATION]:
 * - ACTION: Camera lead to implement a button/icon that navigates to the Camera screen
 *   or triggers the CameraX capture flow.
 *
 * [TODO_GALLERY_INTEGRATION]:
 * - ACTION: Hardware lead to implement 'rememberLauncherForActivityResult'
 *   using 'PickVisualMedia' to select images from the gallery.
 */

//  oo android supports
// I think naviate back is navigation 2 backwards right?
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    repository: ListingRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val factory = CreateListingViewModelFactory(repository)
    val viewModel: CreateListingViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    /*
    // to enable this scroll behaviour
    Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
        SmallTopAppBar(
            title = { Text("Create Listing") },
            navigationIcon = { ... },
            scrollBehavior = scrollBehavior
        )
    }
)
     */

    // Handle successful submission
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar( // Changed from SmallTopAppBar to TopAppBar for better M3 compatibility
                title = { Text("Create Listing") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Images", style = MaterialTheme.typography.titleMedium)

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.imagePaths) { path ->
                    Card(modifier = Modifier.size(100.dp)) {
                        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("Img", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { /* TODO: Trigger Camera/Gallery */ },
                        modifier = Modifier.size(100.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("+")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.price,
                onValueChange = { viewModel.onPriceChange(it) },
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = { viewModel.submit() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Submit Listing")
            }
        }
    }//end of scaffolding
}//end of function


/*

Experimental API: SmallTopAppBar may require the
@OptIn(ExperimentalMaterial3Api::class) annotation
depending on your specific Material3 version.

Image Loading: I have used a placeholder for the image grid.
The integrator will need to add a library like Coil to display
actual image files from the provided paths.
 */

/*
//old old old
package com.example.tinycell.ui.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreateListingScreen(
    viewModel: CreateListingViewModel = viewModel()
) {
    val title by viewModel.title.collectAsState()
    val price by viewModel.price.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Create Listing",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = viewModel::onTitleChange,
            label = { Text("Item Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = viewModel::onPriceChange,
            label = { Text("Price") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = viewModel::submitListing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Post Listing")
        }
    }
}
*/