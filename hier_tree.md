# AskChinna Project Hierarchy

## File Status Legend
✅ - Reviewed and Fixed
🔄 - In Progress
❌ - Not Reviewed
🔵 - Skipped (UI/Resources)
📦 - Nice to Have (Not Critical for MVP)

## Version Compatibility Matrix
- Kotlin: 1.9.22
- AGP: 8.2.2
- Compose: 2024.02.00
- Compose Compiler: 1.5.8
- KSP: 1.9.22-1.0.17
- Firebase BOM: 32.7.2
- Room: 2.6.1
- Hilt: 2.50

## Critical Dependencies
### Core
- androidx.core.ktx: 1.12.0
- androidx.lifecycle.runtime.ktx: 2.7.0
- androidx.fragment.ktx: 1.6.2
- androidx.security.crypto: 1.1.0-alpha06
- androidx.appcompat: 1.6.1
- androidx.constraintlayout: 2.1.4

### Compose
- androidx.activity.compose: 1.8.2
- androidx.compose.bom: 2024.02.00
- androidx.material3: 1.2.0
- androidx.navigation.compose: 2.7.7

### Firebase
- firebase-bom: 32.7.2
- firebase-auth
- firebase-firestore
- firebase-storage
- firebase-analytics
- firebase-crashlytics
- firebase-perf

### Testing
- androidx.arch.core:core-testing: 2.2.0
- androidx.lifecycle:lifecycle-runtime-testing: 2.7.0
- org.jetbrains.kotlinx:kotlinx-coroutines-test: 1.7.3
- junit: 4.13.2
- espresso-core: 3.5.1
- mockk: 1.13.8
- robolectric: 4.11.1

## Build Configuration
- ✅ build.gradle.kts (v1.3)
- ✅ app/build.gradle.kts (v1.3)
- ✅ gradle/libs.versions.toml (v1.1)
- ✅ settings.gradle.kts (v1.0)
- ✅ gradle.properties (v1.0)
- ✅ proguard-rules.pro (v1.0)

## Configuration Files
- ✅ AndroidManifest.xml (v1.1)
- ✅ data_extraction_rules.xml (v1.0)
- ✅ backup_rules.xml (v1.0)

## Resource Files
### Values
- ✅ strings.xml (v1.0)
- ✅ colors.xml (v1.0)
- ✅ dimens.xml (v1.0)
- ✅ attrs.xml (v1.0)
- ✅ style.xml (v1.0)
- ✅ themes.xml (v1.0)

### Raw Data
- ✅ crops_data.json (v1.0)
- ✅ icon_legend.json (v1.0)

## Project Structure

## Core Application Files
- ✅ AskChinnaApplication.kt (1.1)
- ✅ MainActivity.kt (1.2)
- ✅ AppModule.kt (1.1)
- ✅ NetworkModule.kt (1.1)

## Authentication
- ✅ LoginActivity.kt (1.2)
- ✅ LoginViewModel.kt (1.1)
- ✅ RegisterActivity.kt (1.1)
- ✅ RegisterViewModel.kt (1.0)
- ✅ OtpVerificationActivity.kt (1.2)
- ✅ OtpVerificationViewModel.kt (1.0)
- ✅ OtpResendDialogFragment.kt (1.0)
- ✅ ForgotPasswordActivity.kt (1.0)

## Data Management
### Local Data
- ✅ AppDatabase.kt (1.0)
- ✅ SharedPreferencesManager.kt (1.1)
- ✅ dao/
  - ✅ IdentificationResultDao.kt (1.1)
  - ✅ UserDao.kt (1.1)
  - ✅ CropDao.kt (1.1)
- ✅ entity/
  - ✅ UserEntity.kt (1.1)
  - ✅ CropEntity.kt (1.1)
  - ✅ IdentificationResultEntity.kt (1.1)
- ✅ converter/
  - ✅ ListConverter.kt (1.0)
  - ✅ DateConverter.kt (1.0)

### Remote Data
- ✅ FirebaseAuthManager.kt (1.3)
- ✅ FirestoreManager.kt (1.2)
- ✅ FirestoreInitializer.kt (1.0)
- ✅ GeminiService.kt (1.5)
- ✅ ApiKeyProvider.kt (1.4)
- ✅ NetworkExceptionHandler.kt (1.2)

### Repositories
- ✅ IdentificationRepository.kt (1.6)
- ✅ CropRepository.kt (1.2)
- ✅ UserRepository.kt (1.4)

### Models
- ✅ UsageLimit.kt (1.2)
- ✅ UIState.kt (1.2)
- ✅ Crop.kt (1.1)
- ✅ User.kt (1.1)
- ✅ IdentificationResult.kt (1.1)
- ✅ Action.kt (1.1)

## UI Components
- ✅ NetworkStatusView.kt (1.0)
- ✅ LoadingView.kt (1.1)
- ✅ ErrorView.kt (1.1)
- ✅ UsageLimitView.kt (1.1)
- ✅ SessionTimerManager.kt (1.2)
- ✅ SessionTimerView.kt (1.0)
- ✅ ViewExtensions.kt (1.0)
- ✅ ComposeThemeBridge.kt (1.0)
- ✅ Dimensions.kt (1.0)

## Home
- ✅ HomeActivity.kt (1.3)
- ✅ HomeViewModel.kt (1.2)

## Crop Selection
- ✅ CropSelectionActivity.kt (1.4)
- ✅ CropSelectionViewModel.kt (1.2)
- ✅ CropAdapter.kt (1.2)

## Identification
- `ImageCaptureActivity.kt` ✅ (1.4)
- `ImagePreviewActivity.kt` ✅ (1.3)
- `IdentificationViewModel.kt` ✅ (1.3)
- `ImageQualityView.kt` ✅ (1.2)
- `ImageUploadView.kt` ✅ (1.2)

## Results
- ✅ ResultActivity.kt (1.1)
- ✅ ResultViewModel.kt (1.1)
- ✅ SummaryView.kt (1.1)
- ✅ DetailExpandableView.kt (1.1)
- ✅ ActionPlanView.kt (1.2)
- ✅ FeedbackView.kt (1.2)

## Data Models
- UsageLimit.kt (1.2) ✅
- UIState.kt (1.2) ✅
- Crop.kt (1.1) ✅
- User.kt (1.1) ✅
- IdentificationResult.kt (1.1) ✅
- Action.kt (1.1) ✅

## Data Entities
- UserEntity.kt (v1.1) ✅
- CropEntity.kt (v1.1) ✅
- IdentificationResultEntity.kt (v1.1) ✅

## Repositories
- IdentificationRepository.kt (1.6) ✅
- CropRepository.kt (1.2) ✅
- UserRepository.kt (1.4) ✅

## Remote Data Management
- ✅ FirebaseAuthManager.kt (1.3)
- ✅ FirestoreManager.kt (1.2)
- ✅ FirestoreInitializer.kt (1.0)
- ✅ GeminiService.kt (1.5)
- ✅ ApiKeyProvider.kt (1.4)
- ✅ NetworkExceptionHandler.kt (1.2)

## Utilities
- ✅ Constants.kt (1.1)
- ✅ ImageHelper.kt (1.1)
- ✅ DateTimeUtils.kt (1.2)
- ✅ SessionManager.kt (1.2)
- ✅ NetworkStateMonitor.kt (1.1)
- ✅ SimpleCoroutineUtils.kt (1.3)
- 🔵 PdfGenerator.kt

## Services
- ✅ DataSeedService.kt (1.3)

## Tests
- ✅ ExampleUnitTest.kt
- ✅ ExampleInstrumentedTest.kt

### Unit Tests
- ✅ UserRepositoryTest.kt
- ✅ IdentificationRepositoryTest.kt
- ✅ LoginViewModelTest.kt
- ✅ CropSelectionViewModelTest.kt
- ✅ IdentificationViewModelTest.kt
- ✅ ResultViewModelTest.kt
- ✅ ImageHelperTest.kt
- ✅ SessionManagerTest.kt
- ✅ ViewModelTestUtils.kt
- ✅ TestCoroutineRule.kt

### UI Tests
- ✅ LoginUITest.kt
- ✅ CropSelectionUITest.kt
- ✅ ImageCaptureUITest.kt
- ✅ ResultDisplayUITest.kt

## Review Status Summary
- ✅ Reviewed and Fixed: 63 files
- ❌ Not Reviewed: 37+ files
- 🔵 Skipped (UI/Resources): 48 files
- 📦 Nice to Have: 0 files

## Skipped XML Files (UI/Resources)
### Layout Files
- activity_login.xml
- activity_register.xml
- activity_home.xml
- activity_crop_selection.xml
- activity_image_capture.xml
- activity_image_preview.xml
- activity_result.xml
- item_crop.xml
- view_usage_limit.xml
- view_image_upload.xml
- view_summary.xml
- view_detail_expandable.xml
- view_action_plan.xml
- view_error.xml
- view_loading.xml
- view_network_status.xml
- view_image_quality.xml
- view_feedback.xml

### Drawable Files
- ic_app_logo.xml
- ic_capture.xml
- ic_upload.xml
- ic_expand.xml
- ic_collapse.xml
- ic_severity_high.xml
- ic_severity_medium.xml
- ic_severity_low.xml
- ic_pdf_export.xml
- ic_network_offline.xml

### Crop Icons
- ic_chili.xml
- ic_okra.xml
- ic_maize.xml
- ic_cotton.xml
- ic_tomato.xml
- ic_watermelon.xml
- ic_soybean.xml
- ic_rice.xml
- ic_wheat.xml
- ic_pigeon_pea.xml

### Disease Icons
- ic_fungal.xml
- ic_bacterial.xml
- ic_viral.xml
- ic_deficiency.xml

### Action Icons
- ic_spray.xml
- ic_remove.xml
- ic_water.xml
- ic_fertilize.xml
- ic_monitor.xml

### Values Files
- colors.xml
- strings.xml
- dimens.xml
- styles.xml

## Next Review Priorities
1. Core Application Files
2. Critical UI Components
3. Data Management Classes
4. Network and Utility Classes
5. Resource Files (excluding XML)

## Notes
- Prioritize files marked with ❌
- Document all changes in file headers 
- Follow project rules for error handling and resource management
- Update this file after each review
- XML files (drawables, layouts, values) are skipped as they are UI-related 