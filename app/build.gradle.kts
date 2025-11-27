plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("com.google.gms.google-services")
}

@Suppress("EditedTargetSdkVersion")
android {
    namespace = "com.example.proyecto1"

    compileSdk = 36


    defaultConfig {
        applicationId = "com.example.proyecto1"
        minSdk = 26  // Cambiado de 24 a 26 para soportar adaptive icons
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // URL configurada para dispositivo físico con IP local del PC
            buildConfigField("String", "API_BASE_URL", "\"http://172.19.5.121:5120/\"")
        }
        debug {
            // IP local del PC WiFi: 192.168.100.4 (nueva red)
            // IMPORTANTE: PC y celular deben estar en la MISMA red WiFi
            buildConfigField("String", "API_BASE_URL", "\"http://192.168.18.246:5120/\"")
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
        compose = true
        buildConfig = true  // Habilitar BuildConfig para usar API_BASE_URL
    }

    composeOptions {
        // Actualizado a versión alineada con Kotlin 2.0 y últimas mejoras de Compose
        kotlinCompilerExtensionVersion = "1.7.8"
    }

    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        resources.excludes.add("META-INF/licenses/poi/*")
        resources.excludes.add("META-INF/LICENSE")
        resources.excludes.add("META-INF/NOTICE")
    }
}

dependencies {

    // BOM alineado a versión reciente compatible con compiler 1.7.8
    val composeBom = platform("androidx.compose:compose-bom:2024.09.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core Compose & Material3
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Icons extendidos (se mantiene, versión gestionada por BOM; quitar versión explícita anterior)
    implementation("androidx.compose.material:material-icons-extended")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.9.0")

    // Navigation Compose (última estable compatible con BOM)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Lifecycle / ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Material Components
    implementation("com.google.android.material:material:1.12.0")

    // PDF y Excel
    implementation("com.itextpdf:itextg:5.5.10")
    implementation(libs.apache.poi)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
