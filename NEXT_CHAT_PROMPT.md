# askChinna Project - Next Chat Session Prompt

## Project Overview
askChinna is an Android app designed for Indian farmers to identify crop diseases and pests using AI (Google Gemini). The app supports 10 specific crops and provides actionable recommendations.

## Current State (as of January 17, 2025)

### Recently Completed Updates

#### Priority 1: Memory Management (✅ Completed)
1. **ImagePreviewActivity**
   - Added bitmap recycling in onDestroy()
   - Clears ImageView to prevent memory leaks
   - Properly releases resources

2. **ImageCaptureActivity**
   - Implemented temp file cleanup in onDestroy()
   - Removes temporary photo files
   - Simplified cleanup logic

3. **ImageHelper**
   - Added LruCache with proper eviction and bitmap recycling
   - Implemented 50MB disk cache
   - Added progressive image loading
   - Improved bitmap rotation with recycling
   - Added cache clearing methods

#### Priority 2: Network Performance (✅ Completed)
1. **NetworkModule**
   - Increased cache size from 5MB to 20MB
   - Added exponential backoff with jitter
   - Improved retry logic for specific HTTP error codes

2. **GeminiService**
   - Already had exponential backoff
   - Has proper rate limiting

3. **Repository Classes**
   - Added timeouts to IdentificationRepository (15s read, 30s upload)
   - Added timeout to CropRepository (10s)
   - UserRepository already had proper retry logic

### Build Status
- ✅ Successfully built release APK (12.4 MB)
- Location: `/app/build/outputs/apk/release/app-release-unsigned.apk`
- Only minor warnings (unused parameters, unchecked casts)

## Remaining Priorities

### Priority 3: UI Responsiveness
**Goal**: Ensure smooth UI performance by moving heavy operations off the main thread

1. **HomeActivity** - Move DB operations to background
   - Current issue: Database queries run on main thread causing UI freezes
   - Fix: Use `viewModelScope.launch(Dispatchers.IO)` for DB operations
   - File: `/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt`
   - Methods to update: `loadUserData()`, `updateUsageInfo()`

2. **ResultActivity** - Async PDF generation
   - Current issue: PDF creation blocks UI thread
   - Fix: Move PDF generation to coroutine with progress indicator
   - File: `/app/src/main/java/com/example/askchinna/ui/results/ResultActivity.kt`
   - Add: `generatePdfAsync()` method with `withContext(Dispatchers.IO)`

3. **DataSeedService** - Implement async data loading
   - Current issue: Synchronous JSON parsing on startup
   - Fix: Use coroutines for data seeding
   - File: `/app/src/main/java/com/example/askchinna/service/DataSeedService.kt`
   - Update: `seedCropsData()` to be suspend function

### Priority 4: Error Recovery
**Goal**: Implement graceful error handling and recovery mechanisms

1. **OtpVerificationActivity** - Add OTP expiry recovery
   - Current issue: No recovery when OTP expires
   - Fix: Add automatic resend option after expiry
   - File: `/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt`
   - Add: `handleOtpExpiry()` method with retry dialog

2. **CropSelectionActivity** - Handle empty data gracefully
   - Current issue: App crashes if crop list is empty
   - Fix: Show empty state view with retry option
   - File: `/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt`
   - Add: Empty state layout and fallback data

3. **Global error handler** - Implement app-wide crash prevention
   - Current issue: No centralized error handling
   - Fix: Create custom Application error handler
   - Create: `GlobalExceptionHandler` class
   - Implement: `Thread.UncaughtExceptionHandler`
   - Log crashes and show user-friendly error screen

### Priority 5: Cache Optimization
**Goal**: Implement efficient caching to reduce network calls and improve performance

1. **CropRepository** - Cache crop data locally
   - Current issue: Fetches crop data from Firebase on every launch
   - Fix: Cache in Room database with 24-hour expiry
   - File: `/app/src/main/java/com/example/askchinna/data/repository/CropRepository.kt`
   - Add: `CropCacheEntity` with timestamp
   - Implement: Cache-first strategy with fallback to network

2. **SharedPreferencesManager** - Add cache expiry
   - Current issue: No expiration for cached preferences
   - Fix: Add timestamp to cached values
   - File: `/app/src/main/java/com/example/askchinna/data/local/SharedPreferencesManager.kt`
   - Add: `CachedValue<T>` wrapper class with expiry
   - Methods: `putCachedValue()`, `getCachedValue()`

3. **Image cache** - Implement LRU eviction policy
   - Current issue: Basic cache without smart eviction
   - Fix: Enhance existing LruCache implementation
   - File: `/app/src/main/java/com/example/askchinna/util/ImageHelper.kt`
   - Improve: Add access-based eviction
   - Add: Cache stats tracking (hits/misses)
   - Implement: Automatic cleanup on low memory

## Technical Details

### Architecture
- MVVM pattern with ViewModels
- Hilt for dependency injection
- Room for local database
- Firebase for auth and storage
- Coroutines for async operations

### Key Files to Review
1. `/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt`
2. `/app/src/main/java/com/example/askchinna/ui/results/ResultActivity.kt`
3. `/app/src/main/java/com/example/askchinna/service/DataSeedService.kt`
4. `/app/src/main/java/com/example/askchinna/data/repository/CropRepository.kt`
5. `/app/src/main/java/com/example/askchinna/data/local/SharedPreferencesManager.kt`

### Testing Required
1. Memory usage monitoring during image operations
2. Network timeout verification
3. Cache hit/miss rates
4. UI responsiveness during heavy operations
5. Error recovery scenarios

### Deployment Checklist
1. Sign the APK with release key
2. Test on low-end devices (2GB RAM)
3. Verify offline functionality
4. Check Hindi language support
5. Monitor crash reports
6. Verify API key security

## Next Steps
1. Continue with Priority 3 (UI Responsiveness)
2. Create comprehensive UI tests
3. Implement performance monitoring
4. Prepare for production deployment
5. Set up crash reporting (Crashlytics)

## Important Notes
- Target audience: Low-literacy Indian farmers
- Must work on low-end Android devices (2GB RAM, Android 6.0+)
- Offline-first approach is critical
- Image processing must be memory-efficient
- All error messages should be user-friendly

## Commands for Next Session
```bash
# To build and test
cd /mnt/c/Users/raman/AndroidStudioProjects/askChinna
./gradlew clean assembleDebug
./gradlew test

# To check specific files
grep -n "TODO\|FIXME" app/src/main/java/com/example/askchinna/**/*.kt

# To run lint checks
./gradlew lint

# To generate signed APK
./gradlew assembleRelease
```

## Questions for User
1. Do you have the signing key for the release APK?
2. Which priority should we tackle next?
3. Are there any specific performance issues you've noticed?
4. Do you need help setting up crash reporting?
5. What's the deployment timeline?