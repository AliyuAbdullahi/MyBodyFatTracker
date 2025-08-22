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

// Read version properties
val versionPropsFile = rootProject.file("app/version.properties") // Adjusted path to be relative to rootProject
val versionProps = Properties()
if (versionPropsFile.exists()) {
    versionPropsFile.inputStream().use { versionProps.load(it) }
} else {
    throw GradleException("Could not read version.properties!")
}

val appVersionCode = versionProps.getProperty("versionCode", "1").toInt()
val appVersionName = versionProps.getProperty("versionName", "1.0.0")

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
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
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(localProperties.getProperty("RELEASE_STORE_FILE", ""))
                storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD", "")
                keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS", "")
                keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD", "")
            }
        }

        release {
            isMinifyEnabled = true // <-- CHANGED
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField(
                "Boolean",
                "IS_SUPER_USER",
                localProperties.getProperty("IS_SUPER_USER", "false")
            )
            buildConfigField(
                "String",
                "PROFILE_BANNER_AD_UNIT_ID",
                "\"${localProperties.getProperty("ProfileBanner", "")}\""
            )
            buildConfigField(
                "String",
                "HOME_BANNER_AD_UNIT_ID",
                "\"${localProperties.getProperty("HomeBanner", "")}\""
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
                "HOME_BANNER_AD_UNIT_ID",
                "\"ca-app-pub-3940256099942544/6300978111\"" // Google's test banner ID
            )
            buildConfigField(
                "String",
                "EDUCATION_INTERSTITIAL_AD_UNIT_ID",
                "\"ca-app-pub-3940256099942544/1033173712\"" // Google's test interstitial ID
            )
            buildConfigField(
                "Boolean",
                "IS_SUPER_USER",
                localProperties.getProperty("IS_SUPER_USER", "true")
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

// Define version increment tasks
tasks.register("incrementPatch") {
    group = "versioning"
    description = "Increments the patch version and versionCode."
    doLast {
        val currentVersionCode = versionProps.getProperty("versionCode").toInt()
        val currentVersionName = versionProps.getProperty("versionName")
        val parts = currentVersionName.split(".").map { it.toInt() }.toMutableList()
        parts[2] = parts[2] + 1 // Increment patch

        versionProps.setProperty("versionCode", (currentVersionCode + 1).toString())
        versionProps.setProperty("versionName", parts.joinToString("."))
        versionPropsFile.outputStream().use { versionProps.store(it, null) }
        println("Version updated to: ${parts.joinToString(".")} (Code: ${currentVersionCode + 1})")
    }
}

tasks.register("incrementMinor") {
    group = "versioning"
    description = "Increments the minor version, resets patch to 0, and increments versionCode."
    doLast {
        val currentVersionCode = versionProps.getProperty("versionCode").toInt()
        val currentVersionName = versionProps.getProperty("versionName")
        val parts = currentVersionName.split(".").map { it.toInt() }.toMutableList()
        parts[1] = parts[1] + 1 // Increment minor
        parts[2] = 0             // Reset patch

        versionProps.setProperty("versionCode", (currentVersionCode + 1).toString())
        versionProps.setProperty("versionName", parts.joinToString("."))
        versionPropsFile.outputStream().use { versionProps.store(it, null) }
        println("Version updated to: ${parts.joinToString(".")} (Code: ${currentVersionCode + 1})")
    }
}

tasks.register("incrementMajor") {
    group = "versioning"
    description = "Increments the major version, resets minor and patch to 0, and increments versionCode."
    doLast {
        val currentVersionCode = versionProps.getProperty("versionCode").toInt()
        val currentVersionName = versionProps.getProperty("versionName")
        val parts = currentVersionName.split(".").map { it.toInt() }.toMutableList()
        parts[0] = parts[0] + 1 // Increment major
        parts[1] = 0             // Reset minor
        parts[2] = 0             // Reset patch

        versionProps.setProperty("versionCode", (currentVersionCode + 1).toString())
        versionProps.setProperty("versionName", parts.joinToString("."))
        versionPropsFile.outputStream().use { versionProps.store(it, null) }
        println("Version updated to: ${parts.joinToString(".")} (Code: ${currentVersionCode + 1})")
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
