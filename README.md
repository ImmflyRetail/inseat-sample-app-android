# Inseat demo app

How to run the project

1. Add to your root `settings.gradle.kts` file the following code:
    ```kotlin
    dependencyResolutionManagement {
      repositories {
         maven {
            url = uri("https://app-cdn.immflyretail.link/inseat-android-sdk/")
            credentials {
               username = "enter-your-username-here"
               password = "enter-your-password-here"
            }
         } 
      }
    }
    ```
2. Add to your module's `build.gradle.kts` file the following code:
   ```kotlin
   dependencies {
       implementation("com.immflyretail.inseat.sdk:inseat:0.`.0")
   }
   ```



Typically SDK usage:

1. Initialize the SDK
   ```kotlin
   InseatSdk.initialize(
       initialize(
                applicationContext = this@MainApplication,
                apiKey = "enter-your_api_key-here"
            )
   )
   ```
2. Call the `InseatSdk.getInstance().syncProductData()` method to sync the product data.
3. Call the `InseatSdk.getInstance().start` method to start the SDK.
4. Call the `InseatSdk.getInstance().fetchMenus` method to get all menus.
5. Call the `InseatSdk.getInstance().setUserData` method to choose menu.
6. Use methods like `observeShop`, `observeProducts` observe necessary data.

