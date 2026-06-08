plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    alias(libs.plugins.google.firebase.appdistribution)
}

android {
    namespace = "com.example.beautyparlor"
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile = file("C:\\Jenkins\\beautykeys\\beauty_parlor_release.jks")
            // These keys must match the environment variables defined in your Jenkinsfile
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "Vijay@123"
            keyAlias = "beautyalias"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "Vijay@123"
        }
    }

    defaultConfig {
        applicationId = "com.example.beautyparlor"
        minSdk = 24
        targetSdk = 35

        val jenkinsBuildNumber = System.getenv("BUILD_NUMBER")?.toInt() ?: 1

        versionCode = jenkinsBuildNumber
        versionName = "1.${jenkinsBuildNumber}"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            firebaseAppDistribution {
                artifactType = "APK"
                releaseNotes = "New test build from Jenkins"
                groups = "beauty-parlor-testers"
            }
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            firebaseAppDistribution {
                artifactType = "APK"
                // This path is correct for assembleRelease output
                artifactPath = "app/build/outputs/apk/release/app-release.apk"
                releaseNotes = "Production Release v1.1"
                groups = "beauty-parlor-testers"
            }

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
    implementation("com.google.zxing:core:3.5.1")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("com.google.firebase:firebase-functions-ktx")
    implementation ("com.google.accompanist:accompanist-pager:0.34.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.34.0")
    // Use the Firebase BOM to manage versions
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.appcheck.ktx)
    implementation(libs.firebase.config)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    implementation("com.google.code.gson:gson:2.10.1")
    // To use Kotlin annotation processing tool (kapt)
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation ("com.google.accompanist:accompanist-pager:0.28.0")
    // Optional - Kotlin Coroutines support
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation("io.coil-kt:coil-compose:2.5.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
