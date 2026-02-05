package com.example.tinycell.ui.screens.create


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.* // Ensure ONLY Material3 is used
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme

// for a dropdown menu for category
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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

    // FIX: Define the missing 'expanded' state for the dropdown menu
    var expanded by remember { mutableStateOf(false) }

    // Image picker launcher - allows selecting multiple images
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
        onResult = { uris: List<Uri> ->
            // Add selected image URIs to the view model
            uris.forEach { uri ->
                viewModel.addImage(uri.toString())
            }
        }
    )

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
            Text("Images (Optional)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Add up to 5 photos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.imagePaths) { imagePath ->
                    ImagePreviewCard(
                        imageUri = imagePath,
                        onRemove = { viewModel.removeImage(imagePath) }
                    )
                }
                item {
                    OutlinedButton(
                        onClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.size(100.dp),
                        shape = MaterialTheme.shapes.medium,
                        enabled = uiState.imagePaths.size < 5
                    ) {
                        Text("+", style = MaterialTheme.typography.headlineMedium)
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

            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    uiState.availableCategories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                viewModel.onCategoryChange(selectionOption)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }// end of dropdown menu

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

/**
 * Image Preview Card - Shows selected image with remove button
 */
@Composable
private fun ImagePreviewCard(
    imageUri: String,
    onRemove: () -> Unit
) {
    Box(modifier = Modifier.size(100.dp)) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.medium
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Selected image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Remove button overlay
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove image",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(2.dp)
            )
        }
    }
}


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