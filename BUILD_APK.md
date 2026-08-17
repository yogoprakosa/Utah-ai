# Build APK Utah AI

Android's official build flow uses Gradle. From the project root:

- Windows: `gradlew.bat assembleDebug`
- macOS/Linux: `./gradlew assembleDebug`

The debug APK is produced under:
`app/build/outputs/apk/debug/app-debug.apk`

For a release APK, configure release signing in `app/build.gradle.kts` and run the appropriate release task. Android's official documentation confirms `assembleDebug` produces an installable debug APK. 

Before using real AI:
1. Deploy `backend/` over HTTPS.
2. Put `OPENAI_API_KEY` only in the backend environment.
3. Change `BASE_URL` in `ApiClient.kt` to the backend URL.
4. Test the app on a physical Android device.

This environment does not contain Android SDK/Gradle distributions and cannot resolve external downloads, so I could not truthfully compile the APK binary here.
