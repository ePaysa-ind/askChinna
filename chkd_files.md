# chkd_files: View Binding & Data Binding Reference

## File Verification & Fixing Order Logic
- Files are checked and fixed in order of core user flow and MVP priority.
- Priority is given to files required for app startup, login, registration, OTP, and main features.
- The order follows the reference list below, as well as the actual usage in the app.
- XMLs are moved back into layout/ one by one, and view binding is verified/fixed for each.

### Completed:
- DetailExpandableView.kt & view_detail_expandable.xml
- SummaryView.kt & view_summary.xml
- ResultActivity.kt & activity_result.xml
- LoginActivity.kt & activity_login.xml
- ImageCaptureActivity.kt & activity_image_capture.xml
- ImagePreviewActivity.kt & activity_image_preview.xml
- OtpVerificationActivity.kt & activity_otp_verification.xml
- dialog_otp_resend.xml & DialogOtpResendBinding
- view_action_plan.xml & ActionPlanView.kt
- view_error.xml & ViewErrorBinding
- view_network_status.xml & ViewNetworkStatusBinding
- view_image_upload.xml & ImageUploadView.kt
- view_image_quality.xml & ImageQualityView.kt
- view_feedback.xml & FeedbackView.kt
- view_loading.xml & LoadingView.kt
- view_usage_limit.xml & UsageLimitView.kt (and attrs.xml)
- view_session_timer.xml & SessionTimerView.kt
- HomeActivity.kt & activity_home.xml
- MainActivity.kt
- NetworkStatusView.kt & view_network_status.xml
- RegisterActivity.kt & activity_register.xml
- NetworkStateMonitor.kt (utility, verified as working)
- OnboardingActivity.kt & activity_onboarding.xml
- item_onboarding_page.xml
- ActivityForgotPassword.kt & activity_forgot_password.xml
- ActivityCropSelection.kt & activity_crop_selection.xml
- UserRepository.kt (fixed and verified)
- CropRepository.kt (fixed and verified)
- IdentificationRepository.kt (fixed and verified)
- AppModule.kt (fixed and verified)
- DataSeedService.kt (fixed and verified)
- HomeViewModel.kt (fixed and verified)
- LoginViewModel.kt (fixed and verified)
- RegisterViewModel.kt (fixed and verified)
- OtpVerificationViewModel.kt (fixed and verified)
- ForgotPasswordViewModel.kt (fixed and verified)
- ResultViewModel.kt (fixed and verified)
- IdentificationViewModel.kt (fixed and verified)
- CropSelectionViewModel.kt (fixed and verified)
- SessionTimerManager.kt (fixed and verified)

### Skipped (Not Required):
- view_crop_selection.xml (not referenced in code, not needed)

### Next To Work On:

### Yet To Be Worked On:
// (add more as needed)

---

## Reference Files

### 1. DetailExpandableView.kt & view_detail_expandable.xml
- `<layout>` and `<data>` tags present in XML
- Data binding variable(s) and expressions used
- View binding and data binding both work
- No build or runtime errors

### 2. SummaryView.kt & view_summary.xml
- `<layout>` and `<data>` tags present in XML
- No variables yet, but structure is future-proofed
- View binding works
- No build or runtime errors

### 3. ResultActivity.kt & activity_result.xml
- Standard view binding (no `<layout>`/`<data>` in XML)
- View binding works
- No build or runtime errors

### 4. view_error.xml & ViewErrorBinding
- Used as an `<include>` in `activity_result.xml`
- Correctly located in `res/layout/`
- View binding class (`ViewErrorBinding`) is generated and used without errors
- No build or runtime errors

### 5. LoginActivity.kt & activity_login.xml
- Uses standard view binding (no <layout>/<data> in XML)
- Custom view <com.example.askchinna.ui.common.NetworkStatusView> used for network status (replaces <include>)
- All referenced views and bindings are correct
- No build or runtime errors

### 6. view_network_status.xml & ViewNetworkStatusBinding
- Used as a custom view in activity_login.xml
- Root is <androidx.cardview.widget.CardView>
- No <layout> or <data> needed
- View binding class (`ViewNetworkStatusBinding`) is generated and used without errors
- No build or runtime errors

### 7. ImageCaptureActivity.kt & activity_image_capture.xml
- Uses standard view binding (no <layout>/<data> in XML)
- Custom views: <com.example.askchinna.ui.home.SessionTimerView>, <com.example.askchinna.ui.common.NetworkStatusView>
- All referenced views and bindings are correct
- No build or runtime errors

### 8. ImagePreviewActivity.kt & activity_image_preview.xml
- Uses standard view binding (no <layout>/<data> in XML)
- Custom views: <com.example.askchinna.ui.home.SessionTimerView>, <com.example.askchinna.ui.common.NetworkStatusView>, <com.example.askchinna.ui.identification.ImageQualityView>, <com.example.askchinna.ui.identification.ImageUploadView>
- All referenced views and bindings are correct
- No build or runtime errors

### 9. OtpVerificationActivity.kt & activity_otp_verification.xml
- Uses standard view binding (no <layout>/<data> in XML)
- Includes custom view: <com.example.askchinna.ui.common.NetworkStatusView> via <include>
- All referenced views and bindings are correct
- LiveData is observed using observe(), not collectLatest
- View binding for included layout accessed via binding.networkStatusView.root
- No build or runtime errors after fix

### 10. dialog_otp_resend.xml & DialogOtpResendBinding
- Used as a dialog in OtpVerificationActivity
- Root is <androidx.constraintlayout.widget.ConstraintLayout>
- No <layout> or <data> needed
- View binding class (`DialogOtpResendBinding`) is generated and used without errors
- No build or runtime errors

### 11. view_action_plan.xml & ActionPlanView.kt
- Used as a custom view for displaying recommended actions
- Root is <androidx.cardview.widget.CardView>
- Adapter and binding classes are generated and used without errors
- No build or runtime errors

### 13. view_image_quality.xml & ImageQualityView.kt
- Used as a custom view for displaying image quality analysis results
- Root is <LinearLayout>
- View binding class (`ViewImageQualityBinding`) is generated and used without errors
- No build or runtime errors

### 14. view_feedback.xml & FeedbackView.kt
- Used as a custom view for user feedback on identification results
- Root is <merge> (inflated into CardView)
- View binding class (`ViewFeedbackBinding`) is generated and used without errors
- Correct usage: ViewFeedbackBinding.inflate(LayoutInflater.from(context), this) (two arguments only)
- No build or runtime errors

---

## Verification Checklist
- All relevant binding classes are generated in `