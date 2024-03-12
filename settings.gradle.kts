pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")

        maven { url = uri("https://android-sdk.is.com") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://artifact.bytedance.com/repository/pangle") }
        maven { url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea") }
        maven {
            url = uri("https://maven.pkg.jetbrains.space/keego/p/haki/haki")
            credentials {
                username = "keego"
                password = "keego"
            }
        }
    }
}

buildscript {
    repositories {
        google()       // here
        mavenCentral()
    }
    dependencies {
    }
}

rootProject.name = "Note"
include(":app")
