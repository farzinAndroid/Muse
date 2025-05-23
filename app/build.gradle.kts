plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.hiltPlugin)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.farzin.modernmusicplayer"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.farzin.modernmusicplayer"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 12
        versionName = "1.3.7"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(project(":core:core_data"))
    implementation(project(":core:core_domain"))
    implementation(project(":core:core_media_service"))
    implementation(project(":core:core_media_store"))
    implementation(project(":core:core_model"))
    implementation(project(":core:core_common"))
    implementation(project(":core:core_datastore"))
    implementation(project(":core:core_ui"))
    implementation(project(":features:home"))
    implementation(project(":features:album"))
    implementation(project(":features:player"))
    implementation(project(":features:artist"))
    implementation(project(":features:folder"))
    implementation(project(":features:search"))
    implementation(project(":features:playlists"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // compose
    implementation(libs.bundles.composeBundle)

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    //hilt navigation
    implementation(libs.androidx.hilt.navigation.compose)

    // coil
    implementation(libs.coil.compose)

    // serialization
    implementation(libs.kotlinx.serialization.json)

    //compose navigation
    implementation(libs.androidx.navigation.compose)

}