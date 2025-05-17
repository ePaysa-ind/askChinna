# askChinna Quick Reference

## Change Log
- 2025-05-04: Refactored DateTimeUtils from a class with DI to a Kotlin object for proper utility usage and singleton behavior. No DI required.
- 2025-05-04: Introduced project-wide error tracking via BUILD_ERRORS.md for systematic build error management.
- 2025-05-04: Updated OTP verification flow with proper view binding and network state monitoring.

## Error Tracking
- All build errors are now tracked in BUILD_ERRORS.md (or build_errors.log) at the project root. Update this file after each build to monitor and resolve issues systematically.

> ⚠️ CRITICAL RULE: Do not make any changes unless the root cause is shown to the user and the user confirms the change.

## Core Context
- Kotlin-only Android app for Indian farmers
- Target: Low-end devices (2GB+ RAM, 8GB+ storage)
- Min SDK: 23, Target SDK: 33
- Offline-first approach with Hindi support

## Version Management
- Kotlin: 1.9.22
- Compose: 1.5.8 (Compiler)
- AGP: 8.2.2
- Hilt: 2.50
- BOM: Compose 2024.02.00, Firebase 32.7.2
- Testing: 
  - androidx.arch.core:core-testing:2.2.0
  - androidx.lifecycle:lifecycle-runtime-testing:2.7.0
  - org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3

## Build Configuration
### Kapt Configuration
- Issue: Module loading failure during annotation processing
- Root Cause: Improper kapt configuration for Hilt and Room
- Fix: Updated kapt configuration in build.gradle.kts
- Impact: Affects dependency injection and database operations
- Verification: Clean build required after changes

### Source Sets
- Main source code: app/src/main/java
- Debug source code: app/src/main/java (shared)
- Test source code: app/src/test/java
- AndroidTest source code: app/src/androidTest/java

## Change Process
1. **Pre-change**:
   - Check version compatibility
   - Verify BOM usage
   - Review dependency tree

2. **Implementation**:
   - Make single file changes
   - Update versions if needed
   - Document changes

3. **Post-change**:
   - Verify version alignment
   - Run clean build
   - Test on low-end device
   - Check offline functionality

## Performance Requirements
- Cold start < 2s
- Image processing < 5s
- Memory < 100MB
- APK size < 20MB
- Battery efficient
- Low data usage

## Testing Requirements
- Test on low-end devices
- Test offline scenarios
- Test with poor network
- Test Hindi language
- Test battery impact

## Documentation
- KDoc format
- Hindi comments where needed
- Clear error messages
- Document version changes
- Document performance impact

## Emergency Response
1. **Critical Issues**:
   - Offline functionality
   - Core identification
   - User data loss
   - Security breaches

2. **Response Priority**:
   - User data protection
   - Core functionality
   - Performance
   - UI/UX

## Version Update Checklist
□ Version compatibility
□ Performance impact
□ Battery impact
□ Data usage impact
□ Offline functionality
□ Hindi language support
□ Low-end device testing
□ Documentation updates

== Technical Requirements ==

1. Architecture: MVVM with repository pattern
2. Languages: Kotlin only (no Java)
3. Dependency Injection: Hilt
4. Async: Kotlin Coroutines (not RxJava)
5. Networking: Retrofit with OkHttp
6. Image Loading: Glide
7. Security: Encrypted shared preferences for API key storage
8. Target: minSdkVersion 23, targetSdkVersion 33

== Security Requirements ==

1. Data Protection:
   - Use EncryptedSharedPreferences for all sensitive data
   - Implement proper key rotation
   - Follow Indian data protection guidelines
   - Enable secure data deletion

2. Authentication:
   - Secure token storage
   - Implement session management
   - Handle offline authentication
   - Monitor suspicious activities

3. API Security:
   - Secure API key storage
   - Implement request signing
   - Handle API failures gracefully
   - Monitor API usage patterns

== Error Handling Requirements ==

1. Logging:
   - Implement structured logging
   - Use appropriate log levels
   - Include stack traces
   - Monitor error patterns

2. User Experience:
   - Show user-friendly error messages
   - Provide Hindi translations
   - Include recovery options
   - Handle offline scenarios

3. Recovery:
   - Implement retry mechanisms
   - Cache error states
   - Queue failed operations
   - Sync when online

== Memory Management Requirements ==

1. Resource Cleanup:
   - Implement proper lifecycle management
   - Cancel coroutines appropriately
   - Unregister callbacks
   - Clear references

2. Performance:
   - Monitor memory usage
   - Track battery impact
   - Optimize network usage
   - Implement caching

3. Coroutines:
   - Use appropriate scopes
   - Handle exceptions
   - Implement cancellation
   - Monitor usage 

== Testing Requirements ==

1. ViewModel Testing:
   - Use TestCoroutineDispatcher for coroutine testing
   - Mock ViewModelScope for background operations
   - Test state management and updates
   - Verify error handling and recovery

2. Repository Testing:
   - Mock database operations
   - Test offline-first functionality
   - Verify data synchronization
   - Test error scenarios

3. UI Testing:
   - Test on low-end devices
   - Verify offline scenarios
   - Test with poor network
   - Test Hindi language support 

== OTP Verification Requirements ==

1. Network Handling:
   - Monitor network state changes
   - Disable verification when offline
   - Show clear network status
   - Handle resend attempts properly

2. UI Requirements:
   - Material Design components
   - Clear error messages
   - Proper input validation
   - Resend timer functionality

3. Security:
   - Proper session management
   - Secure OTP handling
   - Rate limiting for resend
   - Proper error handling 