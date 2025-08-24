# NutriAI Android App

This is the Android application for NutriAI, a comprehensive AI-powered nutrition and health management system.

## Project Structure

```
NutriAI/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/nutriai/app/
│   │   │   │   ├── data/               # Data layer (API, models, repository)
│   │   │   │   ├── domain/             # Business logic
│   │   │   │   ├── presentation/       # UI (Activities, Fragments, ViewModels)
│   │   │   │   └── utils/              # Utility classes
│   │   │   ├── res/                    # Resources (layouts, drawables, values)
│   │   │   └── AndroidManifest.xml
│   │   └── test/                       # Unit tests
│   └── build.gradle                    # App-level build configuration
├── gradle/                             # Gradle wrapper
├── build.gradle                        # Project-level build configuration
└── settings.gradle                     # Project settings
```

## Setup Instructions

1. **Install Android Studio**
   - Download and install the latest version of Android Studio
   - Install Android SDK (API level 34)

2. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd nutri-ai-stable-dev-main/android-app/NutriAI
   ```

3. **Configure local.properties**
   - Update the `sdk.dir` path in `local.properties` to point to your Android SDK location

4. **Update API Base URL**
   - Open `app/src/main/java/com/nutriai/app/utils/Constants.kt`
   - Update `BASE_URL` to point to your server:
     - For local development: `http://10.0.2.2:5001/api/` (Android emulator)
     - For physical device: `http://YOUR_IP:5001/api/`
     - For production: `https://your-domain.com/api/`

5. **Build and Run**
   - Open the project in Android Studio
   - Sync project with Gradle files
   - Run the app on an emulator or physical device

## Features Implemented

### Authentication Module ✅
- User login with email and password
- User registration
- Token-based authentication
- Secure token storage using DataStore
- Input validation
- Error handling

### Architecture
- **MVVM Pattern**: Clean separation of concerns
- **Repository Pattern**: Single source of truth for data
- **Coroutines**: For asynchronous operations
- **Flow**: For reactive data streams
- **ViewBinding**: Type-safe view references

### Libraries Used
- **Retrofit**: HTTP client for API calls
- **Gson**: JSON serialization/deserialization
- **Coroutines**: Asynchronous programming
- **DataStore**: Secure data storage
- **Material Design**: UI components
- **Glide**: Image loading (prepared for future use)

## Testing the App

1. **Start the backend server**
   ```bash
   cd ../.. # Go to main project directory
   npm start
   ```

2. **Run the Android app**
   - Use Android Studio's Run button
   - Or use command line: `./gradlew installDebug`

3. **Test Authentication**
   - Register a new account
   - Login with credentials
   - Check navigation drawer for user info
   - Test logout functionality

## Play Store Release Configuration

The app is configured for Play Store release with:
- ✅ ProGuard rules for code obfuscation
- ✅ Backup rules for data security
- ✅ Network security configuration
- ✅ Proper permissions handling
- ❌ App signing (needs to be configured)
- ❌ App icons (needs to be created)
- ❌ Release build testing

## Next Steps

1. **App Signing**
   - Generate a release keystore
   - Configure signing in build.gradle
   - Update ProGuard rules if needed

2. **App Icons**
   - Create app icons for all densities
   - Add adaptive icon for Android 8.0+

3. **Testing**
   - Unit tests for ViewModels
   - UI tests with Espresso
   - Integration tests for API calls

4. **Additional Modules**
   - Food Recognition
   - Meal Tracking
   - Health Reports
   - Dashboard with analytics
   - Profile management

## Common Issues

1. **Network Error**
   - Check if backend server is running
   - Verify BASE_URL in Constants.kt
   - Check internet permissions in manifest

2. **Build Errors**
   - Sync project with Gradle files
   - Clean and rebuild project
   - Check Android SDK path in local.properties

3. **API Connection Issues**
   - For emulator: use `10.0.2.2` instead of `localhost`
   - For physical device: use your computer's IP address
   - Ensure both devices are on same network

## Contributing

1. Follow the existing architecture patterns
2. Write unit tests for new features
3. Update documentation
4. Test on multiple device sizes
5. Follow Material Design guidelines

