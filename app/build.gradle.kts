plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.ql_congviec"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ql_congviec"
        minSdk = 26
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Cho Robolectric load tài nguyên Android (R, layout, …)
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

// ✅ Chỉ định rõ JUnit4 cho tất cả task test
tasks.withType<Test>().configureEach {
    useJUnit()
    reports {
        junitXml.required.set(true) // ✅ Bật xuất file XML
        html.required.set(true)     // (giữ HTML nếu muốn)

    }
}

dependencies {
    /* ----------- app code ----------- */
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.runtime)
    implementation(libs.firebase.firestore)



    /* ----------- Unit test (src/test/java) ----------- */
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.12.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("androidx.test:core:1.5.0") // ApplicationProvider, v.v.



    /* ----------- Instrumented-test (src/androidTest/java) ----------- */
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
}
