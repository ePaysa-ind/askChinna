# askChinna - AI-Powered Crop Pest & Disease Identification App

askChinna is an Android application that helps farmers identify pests and diseases in their crops using AI technology. The app uses computer vision and machine learning to analyze crop images and provide detailed diagnosis and treatment recommendations.

## Features

- 📸 **Image Capture**: Take photos of affected crops directly from the app
- 🤖 **AI Analysis**: Powered by Google's Gemini AI for accurate pest/disease identification
- 🌾 **Multi-crop Support**: Supports 10+ major crops including rice, wheat, cotton, etc.
- 📊 **Detailed Reports**: Get comprehensive analysis with:
  - Problem identification
  - Severity assessment
  - Treatment recommendations
  - Action plans
- 📱 **Offline Capability**: Core features work without internet
- 🔒 **Secure**: Phone number authentication with OTP
- 🇮🇳 **India-focused**: Tailored for Indian crops and farming conditions

## Technology Stack

- **Language**: Kotlin
- **Architecture**: MVVM with Clean Architecture
- **UI**: View Binding (migrated from Compose for better compatibility)
- **Dependency Injection**: Hilt
- **Database**: Room
- **Authentication**: Firebase Auth
- **AI/ML**: Google Gemini API
- **Image Processing**: CameraX
- **Networking**: Retrofit + OkHttp

## Setup Instructions

1. Clone the repository:
```bash
git clone https://github.com/ePaysa-ind/askChinna.git
```

2. Open in Android Studio (Arctic Fox or later)

3. Set up Firebase:
   - Create a Firebase project
   - Add `google-services.json` to `app/` directory
   - Enable Phone Authentication

4. Add API Keys:
   - Create `local.properties` in project root
   - Add your Gemini API key:
   ```
   GEMINI_API_KEY=your_api_key_here
   ```

5. Build and run the app

## Project Structure

```
askChinna/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/askchinna/
│   │   │   │   ├── data/          # Data layer
│   │   │   │   ├── di/            # Dependency injection
│   │   │   │   ├── ui/            # UI components
│   │   │   │   └── util/          # Utilities
│   │   │   └── res/               # Resources
│   │   └── test/                  # Unit tests
│   └── build.gradle.kts
├── gradle/
├── .gitignore
├── build.gradle.kts
└── README.md
```

## Development Guidelines

1. Follow MVVM architecture pattern
2. Use Kotlin coroutines for async operations
3. Write unit tests for ViewModels and repositories
4. Use resource strings for all user-facing text
5. Handle errors gracefully with user-friendly messages

## Current Status

- ✅ Core functionality implemented
- ✅ ViewBinding migration complete
- ✅ Error handling improved
- ✅ Performance optimizations done
- 🔧 Testing on various devices
- 📱 Ready for production release

## Known Issues

- SharedPreferences encryption may fail on some devices (falls back to regular storage)
- Some vendor-specific library warnings (harmless)

## Contributing

This is a private project. For any queries or access requests, please contact the admin.

## License

Copyright © 2025 ePaysa. All rights reserved.

## Contact

- GitHub: [@ePaysa-ind](https://github.com/ePaysa-ind)
- Email: admin@epaysa.com