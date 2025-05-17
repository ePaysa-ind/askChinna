# AskChinna Project Rules

## Change Log
- 2025-05-04: Refactored DateTimeUtils to a Kotlin object (utility singleton) and removed DI usage.
- 2025-05-04: Added project error tracking file (BUILD_ERRORS.md) for systematic build error management.

## Error Tracking
- All build errors must be copied to BUILD_ERRORS.md (or build_errors.log) at the project root after each build. This enables systematic tracking and resolution of issues.

> ⚠️ CRITICAL RULE: Do not make any changes unless the root cause is shown to the user and the user confirms the change.

> 📝 IMPORTANT NOTE: The user is not a developer and requires detailed guidance. All code changes must include:
> 1. Clear explanation of the root cause
> 2. Step-by-step implementation instructions
> 3. Exact code changes needed
> 4. Verification steps for deployment
> 5. APK build and deployment guidance

## MVP Priority Rules
1. **Core Functionality First**
   - Focus on the 10 specific crops only
   - Ensure basic image capture and analysis works
   - Implement essential offline capabilities
   - Basic user authentication and session management
   - OTP verification with proper network handling

2. **Performance Over Features**
   - Optimize for low-end devices
   - Ensure smooth operation with minimal RAM
   - Prioritize stability over additional features
   - Focus on core user journey only

3. **Error Handling Priority**
   - Handle only critical errors that affect core functionality:
     * Network connectivity issues
     * Authentication failures
     * Image capture/upload failures
     * Basic data persistence errors
     * OTP verification and resend failures
   - Provide clear user feedback for critical errors
   - Implement basic retry mechanisms for network operations
   - Log critical errors for debugging
   - No need for complex error recovery strategies
   - No need for detailed error analytics

4. **Testing Focus**
   - Test on actual low-end devices
   - Verify offline functionality
   - Test with poor network conditions
   - Basic user flow testing

## MVP Exclusions
The following features are explicitly excluded from MVP phase:
1. **Language Support**
   - English only for MVP
   - No additional language translations
   - No language switching functionality

2. **Export Features**
   - No PDF export functionality
   - No report generation
   - No data export capabilities

3. **UI Enhancements**
   - No complex animations
   - No advanced transitions
   - No custom themes
   - Basic Material Design components only

4. **Nice-to-Have Features**
   - No feedback collection system
   - No advanced analytics
   - No user preferences
   - No onboarding screens

## 1. Version Alignment Check
Before making any changes to a file, verify:
- All versions in `gradle/libs.versions.toml` are compatible
- No duplicate version declarations
- No deprecated versions
- BOM (Bill of Materials) versions are up-to-date

## 2. Dependency Impact Analysis
For any dependency changes:
1. Check `app/build.gradle.kts` for:
   - Correct dependency declarations
   - Proper BOM usage
   - No redundant dependencies
   - Correct plugin declarations

2. Verify in `gradle/libs.versions.toml`:
   - Version declarations match usage
   - No conflicting versions
   - All required dependencies are listed

## 3. Change Process
1. **Pre-change Checklist**:
   ```
   □ Review current versions in libs.versions.toml
   □ Check for deprecated APIs/versions
   □ Verify BOM compatibility
   □ Review dependency tree for conflicts
   ```

2. **Change Implementation**:
   ```
   □ Make changes to specific file
   □ Update related version declarations
   □ Update dependency declarations if needed
   □ Document version changes
   ```

3. **Post-change Verification**:
   ```
   □ Verify version alignment
   □ Check for duplicate declarations
   □ Ensure all dependencies are properly declared
   □ Verify plugin compatibility
   ```

## 4. Version Management Rules
1. **Core Versions**:
   - Kotlin version must be compatible with Compose compiler
   - AGP version must be compatible with Kotlin version
   - Compose BOM version must be latest stable

2. **Dependency Rules**:
   - Use BOM for Firebase dependencies
   - Use BOM for Compose dependencies
   - Avoid direct version declarations when BOM is available

3. **Plugin Rules**:
   - Keep plugins in sync with their respective libraries
   - Use version catalog for plugin versions
   - Avoid mixing plugin declaration styles

## 5. Documentation Requirements
For any version or dependency changes:
1. Document the change in the file header
2. Update version number in file header
3. Add change log entry if significant

## 6. Testing Requirements
After any dependency or version changes:
1. Run clean build
2. Verify all tests pass
3. Check for any new lint warnings
4. Verify runtime behavior

### ViewModel Testing Requirements
1. **Dependencies**:
   - Use `androidx.arch.core:core-testing` for ViewModel testing
   - Include `androidx.lifecycle:lifecycle-runtime-testing` for lifecycle testing
   - Add `org.jetbrains.kotlinx:kotlinx-coroutines-test` for coroutine testing

2. **Test Structure**:
   - Test ViewModel initialization
   - Test state management
   - Test error handling
   - Test coroutine scopes
   - Test data binding

3. **Mocking Requirements**:
   - Mock repositories
   - Mock network calls
   - Mock database operations
   - Mock user interactions

4. **Verification Steps**:
   - Verify state updates
   - Verify error states
   - Verify loading states
   - Verify data transformations

## 7. Emergency Rollback Plan
If issues are found:
1. Revert version changes
2. Restore previous dependency declarations
3. Document the issue and resolution

## 8. Change Documentation Templates

### A. File Header Template
```kotlin
/**
 * File: [file path]
 * Copyright © 2025 askChinna
 * Created: [date]
 * Updated: [date]
 * Version: [version]
 * 
 * Change Log:
 * [version] - [date]
 * - [change description]
 * - [dependency/version updates]
 * 
 * Dependencies:
 * - [list of key dependencies used]
 * 
 * Version Requirements:
 * - Kotlin: [version]
 * - Compose: [version]
 * - Other: [version]
 */
```

### B. Version Change Template
```markdown
## Version Update: [component name]

### Current Versions
- Component: [version]
- Dependencies: [list]
- Plugins: [list]

### Proposed Changes
- New Version: [version]
- Reason: [explanation]
- Impact: [affected components]

### Verification Steps
1. [ ] Version compatibility check
2. [ ] Dependency tree analysis
3. [ ] Build verification
4. [ ] Test execution
5. [ ] Runtime verification

### Rollback Plan
1. [ ] Version revert steps
2. [ ] Dependency restore steps
3. [ ] Verification after rollback
```

### C. Dependency Update Template
```markdown
## Dependency Update: [dependency name]

### Current Configuration
```kotlin
// Current dependency declaration
implementation("group:artifact:version")
```

### Proposed Changes
```kotlin
// New dependency declaration
implementation("group:artifact:newVersion")
```

### Impact Analysis
- [ ] Direct dependencies affected
- [ ] Transitive dependencies affected
- [ ] Build configuration changes
- [ ] Runtime behavior changes

### Testing Requirements
- [ ] Unit tests
- [ ] Integration tests
- [ ] UI tests
- [ ] Performance tests
```

### D. Build Configuration Template
```
```

## 9. Manifest and Resource Rules
1. **Manifest Requirements**:
   ```
   □ All activities must be declared
   □ All permissions must be justified
   □ All features must be declared
   □ All activities must have proper themes
   □ All activities must have proper export flags
   □ All activities must have proper intent filters
   ```

2. **Resource Requirements**:
   ```
   □ All strings must be externalized
   □ All dimensions must be in dimens.xml
   □ All colors must be in colors.xml
   □ All styles must be in styles.xml
   □ All themes must be in themes.xml
   □ All raw data must be in raw/
   ```

3. **Resource Organization**:
   ```
   □ Values must be in appropriate XML files
   □ Drawables must be in drawable/
   □ Layouts must be in layout/
   □ Raw data must be in raw/
   □ Mipmaps must be in mipmap/
   ```

4. **Resource Naming**:
   ```
   □ Use lowercase with underscores
   □ Prefix with type (ic_, bg_, etc.)
   □ Use descriptive names
   □ Follow Android conventions
   ```

## 10. Build Process Rules
1. **Pre-Build Checklist**:
   ```
   □ Verify manifest completeness
   □ Verify resource availability
   □ Verify dependency alignment
   □ Verify version compatibility
   □ Verify permission declarations
   ```

2. **Build Steps**:
   ```
   □ Clean project
   □ Build debug APK
   □ Run basic tests
   □ Verify resources
   □ Check for warnings
   ```

3. **Post-Build Verification**:
   ```
   □ Verify APK size
   □ Check resource inclusion
   □ Verify manifest merging
   □ Check for missing resources
   □ Verify signing configuration
   ```

## 11. Room Entity Rules
1. **Custom Getter Methods**:
   ```
   □ Always annotate custom getter methods with @Ignore if Room manages the property
   □ Only one getter should be used by Room for each property
   □ Avoid duplicate property and method names for Room fields
   ```

> **Reference:** For canonical examples of correct view binding and data binding setup, see [chkd_files.md](chkd_files.md).