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
    }

    composeOptions {
        // ESTE ES EL COMPILADOR DE COMPOSE PARA KOTLIN 2.0
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")

    packagingOptions {
        // Excluir archivos de licencia de Apache POI para evitar conflictos
        exclude("META-INF/licenses/poi/*")
        exclude("META-INF/LICENSE")
        exclude("META-INF/NOTICE")

    }
}

dependencies {

    val composeBom = platform("androidx.compose:compose-bom:2024.05.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // Íconos Material (conjunto extendido para acceder a más íconos como Cloud, PowerSettingsNew, etc.)
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.activity:activity-compose:1.9.0")

    // ViewModel + Compose modern (Flow)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    // Proporciona la función viewModel() para Compose (alineada con lifecycle 2.8.0)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Retrofit para llamadas HTTP
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation("com.google.android.material:material:1.12.0") 
    implementation("androidx.compose.material:material-icons-extended:1.6.7")
    implementation("com.itextpdf:itextg:5.5.10") // This was the missing line
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)

    // DEPENDENCIAS ESPECÍFICAS
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.apache.poi) // Librería para leer Excel .xlsx


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
// LiveData (útil, aunque este ejemplo usa StateFlow)
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    dependencies {
        // ... otras dependencias

        // Compose (Asegúrate de tener estas)
        implementation("androidx.activity:activity-compose:1.9.0")
        implementation(platform("androidx.compose:compose-bom:2024.06.00"))
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.ui:ui-graphics")
        implementation("androidx.compose.ui:ui-tooling-preview")
        implementation("androidx.compose.material3:material3")

        // Íconos (necesarios para Icons.Default.Menu, etc.)
        implementation("androidx.compose.material:material-icons-extended")

        // ViewModel para Compose
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

        // ... otras dependencias
    }

}