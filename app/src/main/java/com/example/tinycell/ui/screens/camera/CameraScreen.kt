package com.example.tinycell.ui.screens.camera

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tinycell.data.repository.CameraRepository

/**
 * TODO: UI Implementation for Teammate
 * 1. [PREVIEW]: Use 'AndroidView' to inflate a 'androidx.camera.view.PreviewView'.
 * 2. [LIFECYCLE]: Bind the 'cameraProvider' from the ViewModel to the LocalLifecycleOwner.
 * 3. [CONTROLS]: Add a FloatingActionButton to trigger the 'takePhoto' action.
 */
@Composable
fun CameraScreen(
    cameraRepository: CameraRepository
) {
    val context = LocalContext.current

    // Using the factory to provide the ViewModel with its dependency
    val factory = CameraViewModelFactory(cameraRepository)
    val viewModel: CameraViewModel = viewModel(factory = factory)

    val hasPermission by viewModel.hasCameraPermission.collectAsState()
    val isReady by viewModel.isCameraReady.collectAsState()

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    // Trigger permission request automatically on screen entry
    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    if (hasPermission) {
        if (isReady) {
            // TODO: Teammate should implement the AndroidView/PreviewView here
        } else {
            // TODO: Show a loading state while CameraX initializes
        }
    } else {
        // TODO: Show a UI explaining why the permission is needed with a button
    }
}

/*
Risk Assessment
•
Dependency Flow: You will need to pass the CameraRepository
instance into CameraScreen from your NavHost or MainActivity.
 */