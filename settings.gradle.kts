pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AppWidget"
include(":app")
