plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.adminpanel"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.adminpanel"
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
    buildFeatures{
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation("com.github.dhaval2404:imagepicker:2.1")
    implementation ("com.github.bumptech.glide:glide:3.8.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("com.github.yalantis:ucrop:2.2.6")
    implementation ("com.github.CanHub:Android-Image-Cropper:4.0.0")
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation("de.hdodenhof:circleimageview:3.0.1")
    // Firebase BOM for dependency management
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
    //implementation ("com.roomorama:caldroid:3.0.0")
    // Firebase Realtime Database
    implementation ("com.google.firebase:firebase-database")
    // Firebase Authentication (if you're using it)
    implementation("com.firebaseui:firebase-ui-firestore:8.0.0")
    implementation ("com.google.firebase:firebase-firestore:24.4.0")
    // RecyclerView
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(libs.legacy.support.v4)
    implementation(libs.recyclerview)
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-messaging:23.0.4")
    implementation ("com.android.volley:volley:1.2.1")
    implementation("androidx.core:core:1.8.0")
    implementation ("androidx.appcompat:appcompat:1.7.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.google.code.gson:gson:2.10.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}