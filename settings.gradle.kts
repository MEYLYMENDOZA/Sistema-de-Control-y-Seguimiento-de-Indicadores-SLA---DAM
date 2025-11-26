pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // CORRECCIÓN: Se añade el repositorio de JitPack para poder encontrar la librería de gráficos.
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Proyecto1"
include(":app")
