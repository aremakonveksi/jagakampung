plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

// Kredensial signing dibaca dari keystore.properties (tidak di-commit).
// Kalau file tidak ada, build release jatuh ke unsigned (build debug tetap jalan).
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = java.util.Properties()
val punyaKeystore = keystorePropsFile.exists()
if (punyaKeystore) {
    keystorePropsFile.inputStream().use { keystoreProps.load(it) }
}

android {
    namespace = "com.jagakampung.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.jagakampung.app"
        minSdk = 23
        targetSdk = 37
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (punyaKeystore) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("JAGA_STORE_FILE"))
                storePassword = keystoreProps.getProperty("JAGA_STORE_PASSWORD")
                keyAlias = keystoreProps.getProperty("JAGA_KEY_ALIAS")
                keyPassword = keystoreProps.getProperty("JAGA_KEY_PASSWORD")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            if (punyaKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.play.services.location)
    implementation(libs.recyclerview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
