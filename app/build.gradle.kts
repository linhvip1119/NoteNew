plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    id("com.google.firebase.appdistribution")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.colorphone"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.note.notebook.notepad.wisenotes"
        minSdk = 24
        targetSdk = 33
        versionCode = 7
        versionName = "1.0.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    applicationVariants.configureEach {
        if (name.contains("debug")) {
            (mergedFlavor as com.android.build.gradle.internal.core.MergedFlavor).applicationId =
                "dev.note.notebook.notepad.wisenotes"
        } else if (name.contains("release")) {
            (mergedFlavor as com.android.build.gradle.internal.core.MergedFlavor).applicationId =
                "note.notebook.notepad.wisenotes"
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

        debug {
            isDebuggable = true
            firebaseAppDistribution {
                appId = "1:72511390302:android:b47f9ed25d23697362eacf"
                releaseNotesFile = "$rootDir/firebase/note.txt"
                serviceCredentialsFile = "$rootDir/firebase/key.json"
                groups = "tester"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/license.txt")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/NOTICE.txt")
            excludes.add("META-INF/notice.txt")
            excludes.add("META-INF/ASL2.0")
            excludes.add("META-INF/*.kotlin_module")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.google.android.gms:play-services-base:18.3.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    //TODO: Dagger hilt
    val hilt_version = "2.48.1"
    implementation("com.google.dagger:hilt-android:$hilt_version")
    kapt("com.google.dagger:hilt-android-compiler:$hilt_version")
    //TODO :Retrofit + okhttp
    val retrofit_version = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
    implementation("com.squareup.retrofit2:converter-gson:$retrofit_version")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    //TODO: Glide
    val glide_version = "4.16.0"
    implementation("com.github.bumptech.glide:glide:$glide_version")
    ksp("com.github.bumptech.glide:ksp:$glide_version")
    //TODO: Navigation component
    val nav_version = "2.7.5"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    //TODO:ViewModel + LiveData + Corountine
    val lifecycle_ext = "2.2.0"
    val lifecycle_version = "2.6.2"
    implementation("androidx.lifecycle:lifecycle-extensions:$lifecycle_ext")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    val coroutines_version = "1.7.1"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
    //TODO: RoomDatabase
    val room = "2.6.0"
    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.room:room-ktx:$room")
    ksp("androidx.room:room-compiler:$room")

    implementation("com.intuit.sdp:sdp-android:1.0.6")

    // implementation("com.zxy.android:recovery:1.0.0")

    implementation(platform("com.google.firebase:firebase-bom:32.1.1"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation("com.airbnb.android:lottie:6.0.1")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    ksp("androidx.lifecycle:lifecycle-compiler:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycle_version")

    //   implementation ("com.google.android.gms:play-services-ads:22.5.0")

    // implementation("com.google.firebase:firebase-messaging-ktx")

    //  implementation("androidx.tonyodev.fetch2:xfetch2:3.1.6")

    val billing_version = "6.1.0"
    //   implementation("com.android.billingclient:billing-ktx:$billing_version")
//    implementation("com.github.yalantis:ucrop:2.2.6")

    implementation("org.greenrobot:eventbus:3.3.1")

    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:review-ktx:2.0.1")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("com.google.api-client:google-api-client-android:1.26.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev136-1.25.0")
    implementation("com.google.http-client:google-http-client-gson:1.41.0")

    implementation("com.makeramen:roundedimageview:2.3.0")

    //setup haki main
    implementation("dev.keego.haki:haki:6.5.4")
    implementation("dev.keego.haki:haki-plugin:6.5.4")

    //ads mediation
    implementation("com.unity3d.ads:unity-ads:4.9.1")
    implementation("com.google.ads.mediation:unity:4.9.1.0")
    implementation("com.applovin.mediation:unityads-adapter:4.9.1.0")
// ==================================================== Unity ====================================================

// ==================================================== ironSource ====================================================
    implementation("com.google.ads.mediation:ironsource:7.5.2.0")
    implementation("com.applovin.mediation:ironsource-adapter:7.4.0.0.1")
// ==================================================== ironSource ====================================================

// ==================================================== Meta ====================================================
    implementation("com.google.ads.mediation:facebook:6.16.0.0")
    implementation("com.applovin.mediation:facebook-adapter:6.15.0.0")
// ==================================================== Meta ====================================================

// ==================================================== Pangle ====================================================
    implementation("com.google.ads.mediation:pangle:5.5.0.8.0")
    implementation("com.applovin.mediation:bytedance-adapter:5.4.0.9.0")
// ==================================================== Pangle ====================================================

// ==================================================== Mintegral ====================================================
    implementation("com.google.ads.mediation:mintegral:16.5.41.0")
    implementation("com.applovin.mediation:mintegral-adapter:16.5.11.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
}