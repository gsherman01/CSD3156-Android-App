package com.example.tinycell.ui.screens.camera

import android.content.res.Configuration
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.tinycell.data.repository.CameraRepository
import com.google.common.util.concurrent.ListenableFuture

/**
 * TODO: UI Implementation for Teammate
 * 1. [PREVIEW]: Use 'AndroidView' to inflate a 'androidx.camera.view.PreviewView'.
 * 2. [LIFECYCLE]: Bind the 'cameraProvider' from the ViewModel to the LocalLifecycleOwner.
 * 3. [CONTROLS]: Add a FloatingActionButton to trigger the 'takePhoto' action.
 */
@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Using the factory to provide the ViewModel with its dependency
    val factory = CameraViewModelFactory(context)
    val viewModel: CameraViewModel = viewModel(factory = factory)

    val hasPermission by viewModel.hasCameraPermission.collectAsState()
    val isReady by viewModel.isCameraReady.collectAsState()

    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation

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
            val previewView = remember {
                PreviewView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
            val preview = remember { Preview.Builder().build() }
            // Issue 5: ImageCapture Should Live in ViewModel (Design)
            // !! TODO Camera Preview View Model it is inside camera viewmodel
            //val imageCapture = remember { ImageCapture.Builder().build() }
            // Perform Camera Lifecycle binding
            LaunchedEffect(isReady) {
                val cameraProvider = viewModel.cameraProvider.value ?: return@LaunchedEffect

                preview.setSurfaceProvider(previewView.surfaceProvider)
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    viewModel.imageCapture
                )
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AndroidView(factory = {previewView}, modifier = Modifier.fillMaxSize())
                FloatingActionButton(
                    onClick = { viewModel.captureImage() },
                    shape = CircleShape,
                    modifier = Modifier
                        .align(if (orientation == Configuration.ORIENTATION_PORTRAIT) Alignment.BottomCenter else Alignment.CenterEnd)
                        .padding(48.dp)
                        .size(80.dp)
                ) {
                    Icon(Icons.Default.Camera, contentDescription = "Capture", modifier = Modifier.fillMaxSize(0.5f))
                }
            }
        } else {
            // TODO: Show a loading state while CameraX initializes
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    } else {
        // TODO: Show a UI explaining why the permission is needed with a button
        // TODO: Test if UI is too small
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally ) {
            Text("Camera access is required to take photos for listings.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                Text("Grant Permission")
            }
        }
    }
}

/*
Risk Assessment
•
Dependency Flow: You will need to pass the CameraRepository
instance into CameraScreen from your NavHost or MainActivity.
 */