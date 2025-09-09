plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.studymate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.studymate"
        minSdk = 27
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

    packagingOptions {
        exclude ("META-INF/DEPENDENCIES")
        // Add more exclusions if necessary
    }
}

dependencies {
    implementation(libs.recyclerview)
    val lifecycle_version = "2.8.4"

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.support.annotations)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Adding Glide dependencies
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.github.clans:fab:1.6.4")

    // Adding Lottie for animations
    implementation("com.airbnb.android:lottie:5.2.0")

    // AndroidX Libraries
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.google.firebase:firebase-analytics:21.0.0")


    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")

    // Adding aunthentication for google dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // PDF Viewer Library
    implementation("com.github.barteksc:android-pdf-viewer:2.8.2")
    implementation ("de.hdodenhof:circleimageview:3.1.0")

}
