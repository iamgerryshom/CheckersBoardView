plugins {
    alias(libs.plugins.android.library)
    id("maven-publish") // For publishing
}

android {
    namespace = "com.gerryshom.checkersboardview"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

}

project.afterEvaluate {
    // Ensure that publishing happens after the release AAR is generated
    publishing {
        publications {
            create<MavenPublication>("libraryProject") {
                groupId = "com.github.iamgerryshom"
                artifactId = "CheckersBoardView"
                version = "local-version-2.1.6"
                artifact(layout.buildDirectory.file("outputs/aar/${project.name}-release.aar"))
            }
        }
        repositories {
            maven {
                url = uri("https://jitpack.io")
                credentials {
                    username = findProperty("jitpack.user")?.toString() ?: System.getenv("JITPACK_USER")
                    password = findProperty("jitpack.token")?.toString() ?: System.getenv("JITPACK_TOKEN")
                }
            }
        }
    }


    // Ensure the 'publishLibraryProjectPublicationToMavenRepository' depends on the 'bundleReleaseAar' task
    tasks.named("publishLibraryProjectPublicationToMavenRepository") {
        dependsOn(tasks.named("bundleReleaseAar"))
    }
}

dependencies {

    implementation ("com.squareup.retrofit2:converter-gson:2.5.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}