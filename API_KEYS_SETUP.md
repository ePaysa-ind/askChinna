# API Keys Setup Guide

## Important Security Notice

API keys should NEVER be committed to version control. This repository is configured to exclude sensitive files from Git.

## Required Files

1. **local.properties** - Contains your API keys
2. **app/google-services.json** - Firebase configuration file

Both files are excluded from Git via `.gitignore`.

## Setup Instructions

### 1. Create local.properties

Create a file named `local.properties` in the project root with the following content:

```
sdk.dir=<YOUR_ANDROID_SDK_PATH>
gemini.api.key=<YOUR_GEMINI_API_KEY>
firebase.api.key=<YOUR_FIREBASE_API_KEY>
```

### 2. Get google-services.json

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Select your project (or create one)
3. Click on the Android icon to add an Android app
4. Download the `google-services.json` file
5. Place it in the `app/` directory

### 3. Get API Keys

#### Gemini API Key
1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Create a new API key
3. Copy the key and add it to `local.properties`

#### Firebase API Key
1. The Firebase API key is included in `google-services.json`
2. The app will read it at runtime from FirebaseApp.getInstance()

## Security Best Practices

1. **Never commit API keys** to version control
2. **Rotate keys regularly** if they're accidentally exposed
3. **Use environment variables** in CI/CD pipelines
4. **Restrict API key usage** in the Google Cloud Console
5. **Monitor API usage** for suspicious activity

## Troubleshooting

If you receive errors about missing API keys:

1. Verify `local.properties` exists and contains the keys
2. Ensure `google-services.json` is in the `app/` directory
3. Clean and rebuild the project
4. Check that the files are not tracked by Git: `git status`

## For CI/CD

Set environment variables:
- `GEMINI_API_KEY` - Your Gemini API key
- Create `google-services.json` from secrets or environment variables