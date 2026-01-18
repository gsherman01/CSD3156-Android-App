package com.example.tinycell.ui.screens.camera

import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tinycell.data.repository.CameraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * TODO: Camera UI State Management
 * 1. [PERMISSION_STATE]: Handle different UI branches for Permission Granted, Denied, and Permanently Denied.
 * 2. [LIFECYCLE_BINDING]: Logic to bind the ProcessCameraProvider to the LifecycleOwner (Activity/Fragment).
 * 3. [IMAGE_URI]: State to store the URI of the last successfully captured image.
 * 4. [ERROR_HANDLING]: Capture and expose camera initialization errors via a UI event/toast flow.
 */
class CameraViewModel(private val cameraRepository: CameraRepository) : ViewModel() {

    // 1. Add state to hold the actual provider instance
    private val _cameraProvider = MutableStateFlow<ProcessCameraProvider?>(null)
    val cameraProvider: StateFlow<ProcessCameraProvider?> = _cameraProvider.asStateFlow()

    // Internal state for camera readiness
    private val _isCameraReady = MutableStateFlow(false)
    val isCameraReady: StateFlow<Boolean> = _isCameraReady.asStateFlow()

    // Internal state for permission status
    private val _hasCameraPermission = MutableStateFlow(false)
    val hasCameraPermission: StateFlow<Boolean> = _hasCameraPermission.asStateFlow()

    // Internal state for current session captured image URI
    private val _currentSessionCapturedImageUri = MutableStateFlow<Uri?>(null)
    val currentSessionCapturedImageUri: StateFlow<Uri?> = _currentSessionCapturedImageUri.asStateFlow()

    // Internal state for last session captured image URI
    private val _lastCapturedImageUri = MutableStateFlow<Uri?>(null)
    val lastCapturedImageUri: StateFlow<Uri?> = _lastCapturedImageUri.asStateFlow()

    // Internal state for camera capture session (capture mode)
    private val _isCaptureSession = MutableStateFlow(false)
    val isCaptureSession: StateFlow<Boolean> = _isCaptureSession.asStateFlow()

    init{
        _lastCapturedImageUri.value = cameraRepository.lastCapturedImageUri.value
    }

    fun onPermissionResult(isGranted: Boolean) {
        _hasCameraPermission.value = isGranted
        if (isGranted) {
            initializeCamera()
        }
    }

    private fun initializeCamera() {
        viewModelScope.launch {
            try {
                val provider = cameraRepository.getCameraProvider()
                // 2. Persist the provider so the UI can bind it to the lifecycle
                _cameraProvider.value = provider
                _isCameraReady.value = true
            } catch (e: Exception) {
                _isCameraReady.value = false
                _cameraProvider.value = null
                // TODO: Log error to a specialized error state
            }
        }
    }

    fun captureImage(imageCapture: ImageCapture, onImageCaptured: () -> Unit) {
        viewModelScope.launch {
            if (_isCaptureSession.value) {
                cameraRepository.takePhoto(imageCapture, onImageCaptured)
                    .collect { uri -> _currentSessionCapturedImageUri.value = uri }
            }
            else{
                throw IllegalStateException("attempted to capture, when capture session is not set!")
            }
        }
    }

    // do not call this if you are not trying to take a photo. if you are trying to access photos taken in a previous session use lastCapturedUri instead
    fun startCaptureSession(){
        if (!_isCaptureSession.value) {
            _isCaptureSession.value = true
            cameraRepository.clearLastCapturedImageUri()
        }
    }
}

/*
    Risk Assessment:
•
Dependency Injection:
This ViewModel currently requires a CameraRepository in its constructor.
You will need to ensure your DI framework (Hilt/Koin)
 or your manual ViewModelFactory is updated to provide this dependency.
•
Dependency Injection:
As noted in your file's comments, this ViewModel cannot be instantiated by the
default Compose viewModel() delegate because it has a constructor parameter.
 You must implement a ViewModelProvider.Factory or use Hilt/Koin to provide the CameraRepository.

•
Context Usage:
The CameraRepository requires a Context.
Ensure you are passing the ApplicationContext to avoid memory leaks.
     */