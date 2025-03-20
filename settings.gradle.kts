pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.google.dagger")) {
                useModule("com.google.dagger:hilt-android-gradle-plugin:${requested.version}")
            }
        }
    }
    plugins {
        id("dagger.hilt.android.plugin") version "2.48.1" apply false
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SpaceInvaders"
include(":app")
