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
        maven {
            url = uri("https://app-cdn.immflyretail.link/inseat-android-sdk/")
            credentials {
                username = "put-your-username-here"
                password = "put-your-password-here"
            }
        }
    }
}

rootProject.name = "Inseat Sample App"
include(":app")

include(":core")
include(":core:common")
include(":core:navigation")
include(":core:ui")
include(":core:preferences:api")
include(":core:preferences:impl")
include(":core:extension")

include(":basket")
include(":basket_api")
include(":orders")
include(":orders_api")
include(":settings")
include(":settings_api")
include(":shop")
include(":shop_api")
