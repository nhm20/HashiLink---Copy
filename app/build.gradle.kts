import java.util.Properties
import java.io.FileInputStream

plugins {
            alias(libs.plugins.android.application)
            alias(libs.plugins.kotlin.android)
            id("kotlin-parcelize")
            alias(libs.plugins.google.gms.google.services)
        }

        // Load local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }

        android {
            namespace = "com.example.hashilink"
            compileSdk {
                version = release(36)
            }

            defaultConfig {
                applicationId = "com.example.hashilink"
                minSdk = 24
                targetSdk = 36
                versionCode = 1
                versionName = "1.0"

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                
                // Load Stripe keys from local.properties
                buildConfigField("String", "STRIPE_PUBLISHABLE_KEY", 
                    "\"${localProperties.getProperty("STRIPE_PUBLISHABLE_KEY", "")}\"")
                buildConfigField("String", "STRIPE_SERVER_URL", 
                    "\"${localProperties.getProperty("STRIPE_SERVER_URL", "http://10.0.2.2:3000")}\"")
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
                buildConfig = true
            }
        }

        dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.appcompat)
            implementation(libs.material)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.constraintlayout)
            implementation(libs.firebase.auth)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.ai)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // Cloudinary for image uploads
    implementation("com.cloudinary:cloudinary-android:3.1.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Stripe payment gateway
    implementation("com.stripe:stripe-android:20.37.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

        }