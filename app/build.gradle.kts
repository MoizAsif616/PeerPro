plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.peerpro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.peerpro"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packagingOptions {
        resources {
            // To merge specific files
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/NOTICE.md"

            // To exclude others
            excludes += "META-INF/DEPENDENCIES"
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.databinding.compiler)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
  implementation(libs.androidx.swiperefreshlayout)
  implementation(libs.firebase.storage)
  testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // For Material Components
    implementation (libs.material)
    // OR for Material 3
    implementation (libs.material3) // or latest version
    // For Kotlin extensions
    implementation (libs.androidx.core.ktx.v180)
    implementation (libs.androidx.fragment.ktx)  // For commit lambda
    implementation (libs.androidx.core.ktx.v190)      // For other extensions
    implementation (libs.androidx.fragment.ktx.v155)
    implementation (libs.material.v161)
    implementation (libs.material.v190) // Latest stable
    implementation (libs.androidx.viewpager2) // for ViewPager2
    implementation (libs.androidx.recyclerview) // Latest stable version
    // If you need Material Design components (optional)
    implementation (libs.material.v1110)
  // Firebase Auth + Firestore
//  implementation (libs.firebase.auth.ktx)
//  implementation (libs.firebase.firestore.ktx)
}