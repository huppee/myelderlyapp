plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services") // 这行替换了原来的 apply plugin
}

android {
    namespace = "com.example.myelderlyapp"
    compileSdk = 35


    defaultConfig {
        applicationId = "com.example.myelderlyapp"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        viewBinding {
            // 启用 View Binding
            enable = true
        }
        buildFeatures {
            dataBinding = true
        }
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation ("androidx.compose.ui:ui:1.0.0")

    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    implementation ("androidx.appcompat:appcompat:1.6.1")

    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("org.java-websocket:Java-WebSocket:1.5.6")

    implementation ("androidx.compose.material3:material3:1.0.0")
    implementation ("androidx.compose.foundation:foundation:1.0.0")
    implementation ("androidx.compose.material:material:1.0.0")
    implementation ("androidx.compose.runtime:runtime:1.0.0")
    implementation ("androidx.navigation:navigation-compose:2.4.0-alpha10")
    implementation ("androidx.compose.material:material-icons-extended:1.5.0")
    implementation ("androidx.compose.material:material:1.5.0")

    implementation ("androidx.compose.material3:material3:1.0.0-alpha07")

    implementation ("androidx.compose.foundation:foundation:1.5.0")              // 提供 rememberRipple、collectIsPressedAsState
    implementation ("androidx.compose.material:material-icons-extended:1.5.0" )  // 提供 autoMirrored 版本的图标
    // … 你已有的：
    implementation ("androidx.compose.material3:material3:1.1.1" )               // 或你的 Material3 版本

    implementation ("androidx.compose.material:material:1.5.0")

    implementation ("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation ("com.airbnb.android:lottie-compose:6.0.0")
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // ✅ 各 Firebase 功能库（不用写版本号）
    implementation("com.google.firebase:firebase-auth-ktx")


    // 你已有的其他依赖...
    implementation("androidx.core:core-ktx:1.10.1")

    // Directly add the navigation-compose dependency with version
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation(libs.firebase.database)  // Direct version definition

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


}
