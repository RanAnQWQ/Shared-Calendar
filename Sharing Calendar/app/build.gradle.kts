plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.app.login"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.app.login"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }

    buildFeatures {
        dataBinding=true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {
    implementation ("com.google.code.gson:gson:2.8.9") // 确保版本号最新
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation ("mysql:mysql-connector-java:5.1.49")
    //implementation ("mysql:mysql-connector-java:8.0.33")
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.protolite.well.known.types)
    implementation(libs.databinding.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("joda-time:joda-time:2.9.1")
    //implementation(kotlin("stdlib")) // 使用 kotlin("stdlib")

}
