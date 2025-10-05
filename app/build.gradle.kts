plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.lifeline"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.lifeline"
        minSdk = 26
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
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.gridlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.android.gif.drawable) //dependency for gif images
    implementation(libs.material.v190) //dependency for material components
    implementation(libs.circularprogressbar) //dependency for custom progress bar
    implementation(libs.gson.v2101) //for shared preferences
    implementation(libs.material.calendarview) //for calendar view
    implementation(libs.androidx.core.ktx.v1120) //for notifications
    implementation(libs.androidx.core.splashscreen) //for animated logo
    implementation(libs.androidx.lifecycle.viewmodel.ktx) //for viewModels
    implementation(libs.android.spinkit) //spin kit progress bar
    implementation(libs.mpandroidchart.v310) //for customizable charts


}