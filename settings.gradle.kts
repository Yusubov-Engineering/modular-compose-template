pluginManagement {
    includeBuild("build-logic")
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
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "modular-compose-template"

// Every directory under core/, base/ and features/ holding a build.gradle.kts
// is a module. Nothing to register by hand: `./gradlew newFeature` creates the
// folders and this picks them up.
//
// core/logger/logger-api  ->  :core:logger:logger-api
listOf("core", "base", "features").forEach { root ->
    rootDir.resolve(root).takeIf { it.isDirectory }
        ?.walkTopDown()
        ?.maxDepth(3)
        ?.filter { it.isFile && it.name == "build.gradle.kts" }
        ?.forEach { buildFile ->
            val path = buildFile.parentFile.relativeTo(rootDir).invariantSeparatorsPath
            include(":${path.replace('/', ':')}")
        }
}

include(":app")
