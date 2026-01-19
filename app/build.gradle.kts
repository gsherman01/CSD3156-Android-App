plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // KSP is required for Room compiler
    id("com.google.devtools.ksp")
    // REQUIRED for modern Compose (Kotlin 1.9.24+) oop ,  now get abolish
    //id("org.jetbrains.kotlin.plugin.compose")


    // for firestore/firebase
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

    }

android {
    namespace = "com.example.tinycell"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tinycell"
        minSdk = 26
        targetSdk = 35 // Changed from 36 to 35 for stability
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // Upgrade for Room 2.6.1 stability
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        // Keeps your experimental opt-in fix
        freeCompilerArgs += "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
    }

    buildFeatures {
        compose = true
    }
    // REMOVED composeOptions { kotlinCompilerExtensionVersion }
    // It is handled by id("org.jetbrains.kotlin.plugin.compose")
    //  now  it got removed, so this it is.
    composeOptions {
        // This is the correct way to handle Compose on Kotlin 1.9.24
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    // Core & Lifecycle
    // Using 2.8.7 to avoid the Alpha/Beta issues found in 2.10.0
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    //coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")


    // Jetpack Compose
    // BOM 2024.10.00 is stable and compatible with your current setup
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    //implementation(libs.androidx.material3)

    // Room Database
    // Version 2.6.1 is required for Java 17 compatibility
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Retrofit Networking
    // Staying on stable 2.9.0 and OkHttp 4.12.0
    val retrofitVersion = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // UI Testing
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debug Tools (Required for Layout Inspector and Previews)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Camera
    val cameraxVersion = "1.3.4" // Stable version for your current SDK/Java setup
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    // REQUIRED: Guava (ListenableFuture)
    /*
    ProcessCameraProvider.getInstance(context) returns a Guava ListenableFuture
Your module does NOT have Guava (or the CameraX lifecycle artifact that bundles it) on the classpath
     */
    implementation("com.google.guava:guava:31.1-android")


    // FireBase/Server things
    // 1. Import the Firebase BoM (Bill of Materials)
    //implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    // to make it more stable with kotlin 1.9.24
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))

    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx") // [PHASE 3.5 ADDITION]

    //  this is a kotlin 2.1.0 feature
    // 2. Add Firebase Analytics (No version needed, managed by BoM)
    //implementation("com.google.firebase:firebase-analytics")
    // 3. Add Cloud Firestore (No version needed, managed by BoM)
    // Note: Use the modern artifact without the -ktx suffix as it's included by default
    //implementation("com.google.firebase:firebase-firestore")

    // TODO: [NETWORKING_INTEGRATION]
    // ACTION: Networking lead to ensure google-services.json is placed in the 'app/' directory.
    // ACTION: Verify that id("com.google.gms.google-services") is applied in the PROJECT-level build.gradle.kts.
}
