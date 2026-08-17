# Utah AI — Cloud Build (No PC)

This version is prepared for GitHub Actions without requiring a Gradle wrapper JAR.

The workflow installs:
- Java 17
- Android SDK API 36
- Android build-tools 36.0.0
- Gradle 8.13

Then it runs `gradle assembleDebug` and uploads the APK as an Actions artifact.

## From Android phone
1. Upload the project contents to your GitHub repository.
2. Open **Actions**.
3. Select **Build Utah AI APK**.
4. Tap **Run workflow**.
5. Wait for the green check.
6. Open the completed workflow run.
7. Download artifact **UtahAI-debug-apk**.
8. Extract the artifact and install the APK.

The current Android client uses `http://10.0.2.2:3000`, which is for an emulator. A physical phone needs an HTTPS backend URL in `ApiClient.kt`.

Never put `OPENAI_API_KEY` in the GitHub repository or APK.
