pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri(rootDir.resolve("repo")) }
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "self_provisioner_jatt_android"
include(":self-provisioner-jatt")
include(":sample")
