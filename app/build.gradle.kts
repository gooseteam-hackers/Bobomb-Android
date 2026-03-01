plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "org.goosepjkt.bobomb"

    compileSdk = 34

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "org.goosepjkt.bobomb"
        minSdk = 21
        targetSdk = 34
        versionCode = 201
        versionName = "2.01"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Force debuggable in release for testing purposes
            isDebuggable = true
        }

        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            isJniDebuggable = true

            // Enable additional debugging features
            isZipAlignEnabled = true
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
        isCoreLibraryDesugaringEnabled = true
    }

    lint {
        disable += "MissingTranslation"
    }

    // Disable the problematic JDK image feature
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

// Explicitly configure Kotlin options to avoid JVM target issues
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.work:work-runtime:2.8.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("com.google.android.material:material:1.14.0-alpha01")

    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.5")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("jp.wasabeef:blurry:4.0.1")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("com.github.mazenrashed:DotsIndicatorWithoutViewpager:1.0.0")

    // Core library desugaring for newer Java features
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
}