import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.firebase.google.service)
    alias(libs.plugins.crashlytics)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties") // Or project.file("local.properties") if it's module-specific
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { input ->
        localProperties.load(input)
    }
}


android {
    namespace = "com.lekan.bodyfattracker"
    compileSdk = 36

    buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "com.lekan.bodyfattracker"
        minSdk = 24
        targetSdk = 36
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
            buildConfigField(
                "String",
                "PROFILE_BANNER_AD_UNIT_ID",
                "\"${localProperties.getProperty("ProfileBanner", "")}\""
            )
            buildConfigField(
                "String",
                "EDUCATION_BANNER_AD_UNIT_ID",
                "\"${localProperties.getProperty("EducationBanner", "")}\""
            )
            buildConfigField(
                "String",
                "EDUCATION_INTERSTITIAL_AD_UNIT_ID", // Assuming you have one for interstitial
                "\"${localProperties.getProperty("EducationInterstitialAdUnit", "")}\""
            )
        }

        debug {
            // You might want to use test IDs for debug builds by default
            buildConfigField(
                "String",
                "PROFILE_BANNER_AD_UNIT_ID",
                "\"ca-app-pub-3940256099942544/6300978111\"" // Google's test banner ID
            )
            buildConfigField(
                "String",
                "EDUCATION_BANNER_AD_UNIT_ID",
                "\"ca-app-pub-3940256099942544/6300978111\""
            )
            buildConfigField(
                "String",
                "EDUCATION_INTERSTITIAL_AD_UNIT_ID",
                "\"ca-app-pub-3940256099942544/1033173712\"" // Google's test interstitial ID
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

    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.dagger.hilt.navigation)
    ksp(libs.dagger.compiler)
    implementation(libs.dagger.hilt.android)
    implementation(libs.gson)
    implementation(libs.material3.icon)

    implementation(libs.coil)
    implementation(libs.coil.network)
    implementation(libs.coil.compose)

    implementation(libs.ads)
    implementation(libs.chartCore)
    implementation(libs.chartCompose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.datastore)
    ksp(libs.room.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.crashlytics.ndk)
    implementation(libs.firebase.firestore)
    implementation(libs.youtube)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}