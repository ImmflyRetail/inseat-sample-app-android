import java.util.Properties

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

val localProps = Properties().apply {
    val file = file("local.properties")
    if (file.exists()) {
        load(file.inputStream())
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Debug repository for local development
        maven {
            url = uri("http://192.168.1.66/")
            isAllowInsecureProtocol = true
        }
        maven {
            url = uri("https://app-cdn.immflyretail.link/inseat-android-sdk/")
            credentials {
                username = "${localProps.getProperty("USERNAME", "")}"
                password = "${localProps.getProperty("PASSWORD", "")}"
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
