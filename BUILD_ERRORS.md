# Build Error Log

## ✅ Fixed Issues
- View binding issues in custom views:
  - app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt
  - app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt
  - app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt
  - app/src/main/java/com/example/askchinna/ui/common/NetworkStatusView.kt
  - app/src/main/java/com/example/askchinna/ui/common/ErrorView.kt
  - app/src/main/java/com/example/askchinna/ui/common/LoadingView.kt
  - app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt
  - app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt
  - app/src/main/java/com/example/askchinna/ui/results/FeedbackView.kt
  - app/src/main/java/com/example/askchinna/ui/results/SummaryView.kt
  - app/src/main/java/com/example/askchinna/ui/results/DetailExpandableView.kt
  - app/src/main/java/com/example/askchinna/ui/results/ActionPlanView.kt
- ActivityImageCaptureBinding and view binding issues in ImageCaptureActivity fixed (binding class generated, correct imports, and view IDs).
- Missing string resources (error_image_selection, error_crop_selection) added to strings.xml.
- View ID mismatches in ImageCaptureActivity resolved to match layout file.

---

## ⏳ Pending Issues
- Activity/Fragment binding issues (e.g., ActivityRegisterBinding, ActivityImagePreviewBinding, etc.)
- Unresolved references in activities/fragments (e.g., btnSubmit, loadingView, errorView, contentLayout, etc.)
- Type argument issues (e.g., UIState.Success needs a type parameter)
- Flow collection and network state issues (e.g., isNetworkAvailable, collectLatest)
- Resource reference issues (e.g., missing strings, drawables, colors)
- Constants and companion object issues (e.g., MAX_MONTHLY_IDENTIFICATIONS)
- Snackbar and dialog resource issues
- Other unresolved references in adapters and viewmodels

---

# (Original error log continues below)
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:158:24 One type argument expected. Use 'class 'Success'' if you don't want to pass type arguments
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:174:35 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [suspend fun <T> Flow<T>.collectLatest(action: suspend (T) -> Unit): Unit]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:174:51 cannot infer a type for this parameter. Please specify it explicitly.
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:176:24 One type argument expected. Use 'class 'Success'' if you don't want to pass type arguments
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:189:45 Too many arguments for public final fun com/example/askchinna/util/NetworkStateMonitor.startMonitoring(): kotlin/Unit
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:191:33 Function invocation 'isNetworkAvailable()' expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:191:52 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [suspend fun <T> Flow<T>.collectLatest(action: suspend (T) -> Unit): Unit]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:191:68 cannot infer a type for this parameter. Please specify it explicitly.
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:192:25 Unresolved reference: networkStatusView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:193:25 Unresolved reference: buttonVerify
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:194:29 Unresolved reference: editTextOtp
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:200:17 Unresolved reference: progressBar
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:201:17 Unresolved reference: contentGroup
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:205:18 None of the following functions are applicable: [@NonNull() static fun make(@NonNull() p0: @EnhancedNullability View, @NonNull() p1: @EnhancedNullability CharSequence, p2: Int): @EnhancedNullability Snackbar, @NonNull() static fun make(@NonNull() p0: @EnhancedNullability View, @StringRes() p1: Int, p2: Int): @EnhancedNullability Snackbar]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:205:31 Cannot access 'val File.root: File': it is internal in file
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:206:14 Unresolved reference: setAction
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:207:41 Function invocation 'isNetworkAvailable()' expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:207:60 Unresolved reference: value
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:208:39 Unresolved reference: editTextOtp
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:220:18 None of the following functions are applicable: [@NonNull() static fun make(@NonNull() p0: @EnhancedNullability View, @NonNull() p1: @EnhancedNullability CharSequence, p2: Int): @EnhancedNullability Snackbar, @NonNull() static fun make(@NonNull() p0: @EnhancedNullability View, @StringRes() p1: Int, p2: Int): @EnhancedNullability Snackbar]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:220:31 Cannot access 'val File.root: File': it is internal in file
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationActivity.kt:220:69 Unresolved reference: show
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/OtpVerificationViewModel.kt:86:28 Unresolved reference: resendOtp
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:26:30 Unresolved reference: databinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:27:38 Unresolved reference: viewmodel
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:41:35 Unresolved reference: ActivityRegisterBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:55:19 Unresolved reference: ActivityRegisterBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:56:9 None of the following functions are applicable: [fun setContentView(@LayoutRes() p0: Int): Unit, fun setContentView(p0: View!): Unit]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:56:32 Cannot access 'val File.root: File': it is internal in file
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:66:29 Unresolved reference: editTextName
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:67:31 Unresolved reference: editTextMobile
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:68:34 Unresolved reference: buttonRegister
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:69:31 Unresolved reference: progressBar
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:70:37 Unresolved reference: networkStatusView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:74:45 Too many arguments for public final fun com/example/askchinna/util/NetworkStateMonitor.startMonitoring(): kotlin/Unit
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:76:33 Function invocation 'isNetworkAvailable()' expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:76:52 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [suspend fun <T> Flow<T>.collectLatest(action: suspend (T) -> Unit): Unit]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:76:68 cannot infer a type for this parameter. Please specify it explicitly.
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:135:38 Function invocation 'isNetworkAvailable()' expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:135:57 Unresolved reference: value
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:136:46 Unresolved reference: error_no_network
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:143:27 Unresolved reference: register
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:148:17 Unresolved reference: textViewLogin
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:153:17 Unresolved reference: imageButtonBack
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:160:23 Unresolved reference: registerState
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:160:53 cannot infer a type for this parameter. Please specify it explicitly.
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:166:24 One type argument expected. Use 'class 'Success'' if you don't want to pass type arguments     
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:186:18 None of the following functions are applicable: [@NonNull() static fun make(@NonNull() p0: @EnhancedNullability View, @NonNull() p1: @EnhancedNullability CharSequence, p2: Int): @EnhancedNullability Snackbar, @NonNull() static fun make(@NonNull() p0: @EnhancedNullability View, @StringRes() p1: Int, p2: Int): @EnhancedNullability Snackbar]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:186:31 Cannot access 'val File.root: File': it is internal in file
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:187:14 Unresolved reference: setAction
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/auth/RegisterActivity.kt:191:31 Unresolved reference: register
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/ErrorView.kt:38:5 Property must be initialized or be abstract
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/ErrorView.kt:39:5 Property must be initialized or be abstract
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/ErrorView.kt:40:5 Property must be initialized or be abstract
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/LoadingView.kt:37:5 Property must be initialized or be abstract
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/LoadingView.kt:38:5 Property must be initialized or be abstract
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/LoadingView.kt:39:5 Property must be initialized or be abstract
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/NetworkStatusView.kt:14:47 Unresolved reference: CloudOff
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/NetworkStatusView.kt:15:47 Unresolved reference: CloudQueue
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/NetworkStatusView.kt:45:69 Unresolved reference: CloudQueue
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/NetworkStatusView.kt:45:99 Unresolved reference: CloudOff
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:19:30 Unresolved reference: databinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:31:26 Unresolved reference: ViewUsageLimitBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:36:23 Unresolved reference: ViewUsageLimitBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:62:13 Argument type mismatch: actual type is 'T?' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:62:22 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [@InlineOnly() fun <T> T.apply(block: T.() -> Unit): T
    [R|Contract description]
     <
        CallsInPlace(block, EXACTLY_ONCE)
    >]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:64:17 Unresolved reference: progressBar
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:64:29 Variable expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:65:17 Unresolved reference: progressBar
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:68:17 Unresolved reference: tvCurrentCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:69:17 Unresolved reference: tvRemainingCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:70:17 Unresolved reference: tvMaxCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:73:17 Unresolved reference: tvPremiumStatus
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:78:63 Unresolved reference: warning
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:79:37 Unresolved reference: success
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:82:17 Unresolved reference: tvRemainingCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:83:17 Unresolved reference: progressBar
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:86:13 Argument type mismatch: actual type is 'kotlin/Unit' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:101:13 Argument type mismatch: actual type is 'T?' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:101:22 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [@InlineOnly() fun <T> T.apply(block: T.() -> Unit): T
    [R|Contract description]
     <
        CallsInPlace(block, EXACTLY_ONCE)
    >]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:102:17 Unresolved reference: progressBar
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:103:17 Unresolved reference: tvCurrentCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:104:17 Unresolved reference: tvRemainingCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:105:17 Unresolved reference: tvMaxCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:108:13 Argument type mismatch: actual type is 'kotlin/Unit' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:123:13 Argument type mismatch: actual type is 'T?' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:123:22 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [@InlineOnly() fun <T> T.apply(block: T.() -> Unit): T
    [R|Contract description]
     <
        CallsInPlace(block, EXACTLY_ONCE)
    >]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:124:17 Unresolved reference: progressBar
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:125:17 Unresolved reference: tvCurrentCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:126:17 Unresolved reference: tvRemainingCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:127:17 Unresolved reference: tvMaxCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:128:17 Unresolved reference: tvError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:129:17 Unresolved reference: tvError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:132:13 Argument type mismatch: actual type is 'kotlin/Unit' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:143:13 Argument type mismatch: actual type is 'T?' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:143:22 Unresolved reference: tvError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:145:17 Unresolved reference: text
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/common/UsageLimitView.kt:148:13 Argument type mismatch: actual type is 'kotlin/Int' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:27:30 Unresolved reference: databinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:58:27 Unresolved reference: ItemCropBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:116:9 'getCurrentList' hides member of supertype 'ListAdapter' and needs 'override' modifier      
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:118:53 Unresolved reference: ItemCropBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:119:33 Argument type mismatch: actual type is 'java/io/File' but '@EnhancedNullability android/view/View' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:119:41 Cannot access 'val File.root: File': it is internal in file
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:125:25 Cannot access 'val File.root: File': it is internal in file
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:125:30 Unresolved reference: setOnClickListener
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:145:17 Argument type mismatch: actual type is 'T' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:145:25 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [@InlineOnly() fun <T> T.apply(block: T.() -> Unit): T
    [R|Contract description]
     <
        CallsInPlace(block, EXACTLY_ONCE)
    >]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:146:33 Unresolved reference: loadImage
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:146:43 Unresolved reference: imgCrop
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:146:57 Unresolved reference: iconResId
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:147:21 Unresolved reference: txtCropName
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:150:17 Argument type mismatch: actual type is 'kotlin/Int' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropAdapter.kt:157:25 Unresolved reference: imgCrop
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:34:30 Unresolved reference: databinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:50:35 Unresolved reference: ActivityCropSelectionBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:59:40 Argument type mismatch: actual type is 'kotlin/Throwable' but 'kotlin/Exception' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:77:23 Unresolved reference: ActivityCropSelectionBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:78:13 None of the following functions are applicable: [fun setContentView(@LayoutRes() p0: Int): Unit, fun setContentView(p0: View!): Unit]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:78:36 Cannot access 'val File.root: File': it is internal in file
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:102:41 Unresolved reference: toolbarCropSelection
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:106:21 Unresolved reference: btnBack
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:123:27 No value passed for parameter 'onCropClicked'
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:127:13 Argument type mismatch: actual type is 'T' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:127:21 Unresolved reference: recyclerCrops
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:128:17 Unresolved reference: layoutManager
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:129:17 Unresolved reference: adapter
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:130:17 Unresolved reference: setHasFixedSize
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:133:13 Argument type mismatch: actual type is 'kotlin/Unit' but 'K' was expected        
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:139:21 Unresolved reference: viewError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:193:21 Unresolved reference: tvUsageLimit
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:193:60 Unresolved reference: usage_limit_format
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:201:21 Unresolved reference: layoutLoading
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:202:21 Unresolved reference: recyclerCrops
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:203:21 Unresolved reference: viewError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:211:21 Unresolved reference: layoutLoading
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:212:21 Unresolved reference: recyclerCrops
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:213:21 Unresolved reference: viewError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:221:21 Unresolved reference: layoutLoading
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:222:21 Unresolved reference: recyclerCrops
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:223:21 Unresolved reference: viewError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:235:13 Argument type mismatch: actual type is 'T' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:235:21 Unresolved reference: viewError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:236:17 Unresolved reference: visibility
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:237:17 Unresolved reference: setError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:240:13 Argument type mismatch: actual type is 'kotlin/Unit' but 'K' was expected        
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:251:46 Unresolved reference: error_not_initialized
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:271:47 Unresolved reference: EXTRA_CROP
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:281:33 Function invocation 'isNetworkAvailable()' expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:281:52 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [suspend fun <T> Flow<T>.collectLatest(action: suspend (T) -> Unit): Unit]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:281:68 cannot infer a type for this parameter. Please specify it explicitly.
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionActivity.kt:283:25 Unresolved reference: not
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionViewModel.kt:57:73 Unresolved reference: Initial
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionViewModel.kt:84:33 Function invocation 'isNetworkAvailable()' expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionViewModel.kt:84:52 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [suspend fun <T> Flow<T>.collectLatest(action: suspend (T) -> Unit): Unit]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/cropselection/CropSelectionViewModel.kt:84:68 cannot infer a type for this parameter. Please specify it explicitly.
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:37:30 Unresolved reference: databinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:51:35 Unresolved reference: ActivityHomeBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:68:23 Unresolved reference: ActivityHomeBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:69:13 None of the following functions are applicable: [fun setContentView(@LayoutRes() p0: Int): Unit, fun setContentView(p0: View!): Unit]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:69:36 Cannot access 'val File.root: File': it is internal in file
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:97:41 Unresolved reference: toolbar
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:106:21 Unresolved reference: btnStartIdentification
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:113:21 Unresolved reference: btnHelp
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:123:33 Function invocation 'isNetworkAvailable()' expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:123:52 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [suspend fun <T> Flow<T>.collectLatest(action: suspend (T) -> Unit): Unit]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:123:68 cannot infer a type for this parameter. Please specify it explicitly.
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:125:29 Unresolved reference: btnStartIdentification
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:126:25 Unresolved reference: not
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:146:33 Unresolved reference: layoutUserInfo
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:147:33 Unresolved reference: progressBar
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:150:33 Unresolved reference: progressBar
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:151:33 Unresolved reference: layoutUserInfo
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:154:33 Unresolved reference: tvUserName
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:155:33 Unresolved reference: tvUserRole
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:159:33 Unresolved reference: progressBar
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:171:33 Unresolved reference: usageLimitView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:174:33 Unresolved reference: usageLimitView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:179:33 Unresolved reference: usageLimitView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:180:33 Unresolved reference: usageLimitView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:193:29 Unresolved reference: sessionTimerView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:194:29 Unresolved reference: sessionTimerView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:210:38 Unresolved reference: MAX_MONTHLY_IDENTIFICATIONS
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:215:21 Unresolved reference: usageLimitView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:221:21 Unresolved reference: btnStartIdentification
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:221:101 Function invocation 'isNetworkAvailable()' expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:221:120 Unresolved reference: value
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:372:23 Unresolved reference: restoreState
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:381:18 Overload resolution ambiguity between candidates: [fun setTitle(@StringRes() p0: Int): AlertDialog.Builder!, fun setTitle(@Nullable() p0: CharSequence?): AlertDialog.Builder!]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:381:36 Unresolved reference: help_title
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeActivity.kt:382:18 Unresolved reference: setMessage
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/HomeViewModel.kt:289:32 Unresolved reference: signOut
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:18:30 Unresolved reference: databinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:29:26 Unresolved reference: ViewSessionTimerBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:35:19 Unresolved reference: ViewSessionTimerBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:69:17 Unresolved reference: tvTimeRemaining
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:70:17 Unresolved reference: progressBarTimer
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:76:25 Unresolved reference: progressBarTimer
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:77:25 Unresolved reference: tvTimeRemaining
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:81:25 Unresolved reference: progressBarTimer
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:82:25 Unresolved reference: tvTimeRemaining
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:86:25 Unresolved reference: progressBarTimer
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:87:25 Unresolved reference: tvTimeRemaining
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:99:21 Unresolved reference: tvWarning
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:100:21 Unresolved reference: tvWarning
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:101:21 Unresolved reference: ivWarningIcon
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:102:21 Unresolved reference: ivWarningIcon
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:104:21 Unresolved reference: tvWarning
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:105:21 Unresolved reference: tvWarning
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:106:21 Unresolved reference: ivWarningIcon
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:107:21 Unresolved reference: ivWarningIcon
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:118:21 Unresolved reference: tvPaused
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/SessionTimerView.kt:120:21 Unresolved reference: tvPaused
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:10:30 Unresolved reference: databinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:22:26 Unresolved reference: ViewUsageLimitBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:23:9 Unresolved reference: ViewUsageLimitBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:41:21 Unresolved reference: progressBarUsage
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:41:38 Variable expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:42:21 Unresolved reference: progressBarUsage
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:44:21 Unresolved reference: tvUsageCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:45:21 Unresolved reference: tvRemainingCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:47:21 Unresolved reference: ivWarning
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:48:21 Unresolved reference: tvRemainingCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:51:21 Unresolved reference: progressBarUsage
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:56:21 Unresolved reference: progressBarUsage
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:56:38 Variable expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:57:21 Unresolved reference: progressBarUsage
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:59:21 Unresolved reference: tvUsageCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:62:21 Unresolved reference: tvRemainingCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:68:25 Unresolved reference: ivWarning
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:69:25 Unresolved reference: tvRemainingCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:72:25 Unresolved reference: progressBarUsage
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:77:25 Unresolved reference: ivWarning
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:78:25 Unresolved reference: tvRemainingCount
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:81:25 Unresolved reference: progressBarUsage
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:90:17 Unresolved reference: progressBarLoading
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:91:17 Unresolved reference: layoutContent
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:96:17 Unresolved reference: tvError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:97:17 Unresolved reference: tvError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:98:17 Unresolved reference: layoutContent
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:103:17 Unresolved reference: tvError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/home/UsageLimitView.kt:104:17 Unresolved reference: layoutContent
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/IdentificationViewModel.kt:121:33 Function invocation 'isNetworkAvailable()' expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/IdentificationViewModel.kt:121:52 Unresolved reference: observeForever
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/IdentificationViewModel.kt:380:55 Unresolved reference: identifyPestOrDisease
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:43:30 Unresolved reference: databinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:59:35 Unresolved reference: ActivityImageCaptureBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:119:50 Unresolved reference: error_image_capture
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:138:50 Unresolved reference: error_image_selection
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:149:23 Unresolved reference: ActivityImageCaptureBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:150:13 None of the following functions are applicable: [fun setContentView(@LayoutRes() p0: Int): Unit, fun setContentView(p0: View!): Unit]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:150:36 Cannot access 'val File.root: File': it is internal in file
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:158:46 Unresolved reference: error_crop_selection
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:226:25 Unresolved reference: tvCropName
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:227:25 Unresolved reference: ivCropIcon
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:227:58 Unresolved reference: iconResId
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:230:21 Unresolved reference: sessionTimerView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:243:21 Unresolved reference: btnBack
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:244:21 Unresolved reference: btnCapture
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:245:21 Unresolved reference: btnGallery
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:260:29 Unresolved reference: networkStatusView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:260:52 Unresolved reference: visibility
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:292:44 Unresolved reference: camera_permission_rationale
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:324:44 Unresolved reference: storage_permission_rationale
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:417:18 Overload resolution ambiguity between candidates: [fun setTitle(@StringRes() p0: Int): AlertDialog.Builder!, fun setTitle(@Nullable() p0: CharSequence?): AlertDialog.Builder!]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:417:36 Unresolved reference: permission_required
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:418:18 Unresolved reference: setMessage
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:419:45 Unresolved reference: settings
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:433:18 Overload resolution ambiguity between candidates: [fun setTitle(@StringRes() p0: Int): AlertDialog.Builder!, fun setTitle(@Nullable() p0: CharSequence?): AlertDialog.Builder!]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:433:36 Unresolved reference: permission_required
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:434:18 Unresolved reference: setMessage
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageCaptureActivity.kt:472:23 Unresolved reference: checkNetworkStatus
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:34:30 Unresolved reference: databinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:49:35 Unresolved reference: ActivityImagePreviewBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:70:23 Unresolved reference: ActivityImagePreviewBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:71:13 None of the following functions are applicable: [fun setContentView(@LayoutRes() p0: Int): Unit, fun setContentView(p0: View!): Unit]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:71:36 Cannot access 'val File.root: File': it is internal in file
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:77:21 Unresolved reference: sessionTimerView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:112:21 Unresolved reference: btnBack
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:113:21 Unresolved reference: btnRetake
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:117:21 Unresolved reference: btnSubmit
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:139:33 Unresolved reference: ivPreview
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:151:29 Unresolved reference: tvCropName
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:152:29 Unresolved reference: ivCropIcon
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:152:62 Unresolved reference: iconResId
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:160:29 Unresolved reference: imageQualityView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:161:25 Unresolved reference: setImageQuality
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:162:25 Unresolved reference: visibility
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:164:29 Unresolved reference: btnSubmit
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:174:37 Unresolved reference: loadingView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:174:54 Unresolved reference: visibility
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:175:37 Unresolved reference: errorView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:175:52 Unresolved reference: visibility
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:176:37 Unresolved reference: contentLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:179:37 Unresolved reference: loadingView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:179:54 Unresolved reference: visibility
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:180:37 Unresolved reference: errorView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:180:52 Unresolved reference: visibility
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:181:37 Unresolved reference: contentLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:184:37 Unresolved reference: loadingView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:184:54 Unresolved reference: visibility
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:185:37 Unresolved reference: errorView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:185:52 Unresolved reference: visibility
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:194:37 Unresolved reference: loadingView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:194:54 Unresolved reference: visibility
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:195:37 Unresolved reference: errorView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:195:52 Unresolved reference: visibility
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:196:37 Unresolved reference: contentLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:198:37 Unresolved reference: errorView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:198:52 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [fun View.setError(message: String, buttonText: String, onButtonClick: () -> Unit): Unit]  
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:223:29 Unresolved reference: networkStatusView
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:223:52 Unresolved reference: visibility
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:224:29 Unresolved reference: btnSubmit
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:262:18 Overload resolution ambiguity between candidates: [fun setTitle(@StringRes() p0: Int): AlertDialog.Builder!, fun setTitle(@Nullable() p0: CharSequence?): AlertDialog.Builder!]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImagePreviewActivity.kt:262:36 Unresolved reference: image_quality_warning_title
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:30:30 Unresolved reference: databinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:43:26 Unresolved reference: ViewImageQualityBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:53:23 Unresolved reference: ViewImageQualityBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:84:13 Argument type mismatch: actual type is 'T?' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:84:22 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [@InlineOnly() fun <T> T.apply(block: T.() -> Unit): T
    [R|Contract description]
     <
        CallsInPlace(block, EXACTLY_ONCE)
    >]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:87:21 Unresolved reference: ivResolution
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:88:21 Unresolved reference: tvResolution
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:95:21 Unresolved reference: ivFocus
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:96:21 Unresolved reference: tvFocus
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:103:21 Unresolved reference: ivBrightness
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:104:21 Unresolved reference: tvBrightness
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:111:21 Unresolved reference: tvError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:112:21 Unresolved reference: tvError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:114:21 Unresolved reference: tvError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:123:17 Unresolved reference: tvOverallStatus
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:124:17 Unresolved reference: tvOverallStatus
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:127:58 Unresolved reference: success
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:134:13 Argument type mismatch: actual type is 'kotlin/Unit' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:148:22 Unresolved reference: setImageResource
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:149:40 Unresolved reference: ic_check_circle
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:150:33 Unresolved reference: ic_error
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:152:22 Unresolved reference: setTextColor
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:155:41 Unresolved reference: success
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:171:13 Argument type mismatch: actual type is 'T?' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:171:22 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [@InlineOnly() fun <T> T.apply(block: T.() -> Unit): T
    [R|Contract description]
     <
        CallsInPlace(block, EXACTLY_ONCE)
    >]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:172:17 Unresolved reference: tvError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:173:17 Unresolved reference: tvError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:174:17 Unresolved reference: tvOverallStatus
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:175:17 Unresolved reference: tvOverallStatus
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:178:13 Argument type mismatch: actual type is 'kotlin/Int' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:192:22 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [@InlineOnly() fun <T> T.apply(block: T.() -> Unit): T
    [R|Contract description]
     <
        CallsInPlace(block, EXACTLY_ONCE)
    >]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:193:17 Unresolved reference: tvError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:194:17 Unresolved reference: tvOverallStatus
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageQualityView.kt:195:17 Unresolved reference: tvOverallStatus
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:33:30 Unresolved reference: databinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:46:26 Unresolved reference: ViewImageUploadBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:61:23 Unresolved reference: ViewImageUploadBinding
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:62:73 Unresolved reference: anim
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:96:13 Argument type mismatch: actual type is 'T?' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:96:22 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [@InlineOnly() fun <T> T.apply(block: T.() -> Unit): T
    [R|Contract description]
     <
        CallsInPlace(block, EXACTLY_ONCE)
    >]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:97:17 Unresolved reference: progressBar
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:98:17 Unresolved reference: tvProgress
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:101:17 Unresolved reference: progressLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:102:17 Unresolved reference: statusLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:103:17 Unresolved reference: errorLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:107:21 Unresolved reference: ivProgress
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:112:13 Argument type mismatch: actual type is 'kotlin/Unit' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:142:13 Argument type mismatch: actual type is 'T?' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:142:22 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [@InlineOnly() fun <T> T.apply(block: T.() -> Unit): T
    [R|Contract description]
     <
        CallsInPlace(block, EXACTLY_ONCE)
    >]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:144:17 Unresolved reference: ivProgress
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:147:17 Unresolved reference: progressLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:148:17 Unresolved reference: statusLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:149:17 Unresolved reference: errorLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:152:17 Unresolved reference: ivStatus
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:153:47 Unresolved reference: ic_check_circle
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:154:37 Unresolved reference: ic_error
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:156:17 Unresolved reference: tvStatus
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:159:48 Unresolved reference: success
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:163:17 Unresolved reference: tvStatus
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:170:13 Argument type mismatch: actual type is 'kotlin/Unit' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:198:13 Argument type mismatch: actual type is 'T?' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:198:22 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [@InlineOnly() fun <T> T.apply(block: T.() -> Unit): T
    [R|Contract description]
     <
        CallsInPlace(block, EXACTLY_ONCE)
    >]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:200:17 Unresolved reference: ivProgress
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:203:17 Unresolved reference: progressLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:204:17 Unresolved reference: statusLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:205:17 Unresolved reference: errorLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:208:17 Unresolved reference: tvError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:212:13 Argument type mismatch: actual type is 'kotlin/Unit' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:222:13 Argument type mismatch: actual type is 'T?' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:222:22 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [@InlineOnly() fun <T> T.apply(block: T.() -> Unit): T
    [R|Contract description]
     <
        CallsInPlace(block, EXACTLY_ONCE)
    >]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:224:17 Unresolved reference: ivProgress
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:227:17 Unresolved reference: progressLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:228:17 Unresolved reference: statusLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:229:17 Unresolved reference: errorLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:232:17 Unresolved reference: tvError
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:235:13 Argument type mismatch: actual type is 'kotlin/Int' but 'K' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:249:22 Unresolved reference. None of the following candidates is applicable because of receiver type mismatch: [@InlineOnly() fun <T> T.apply(block: T.() -> Unit): T
    [R|Contract description]
     <
        CallsInPlace(block, EXACTLY_ONCE)
    >]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:251:17 Unresolved reference: ivProgress
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:254:17 Unresolved reference: progressBar
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:255:17 Unresolved reference: tvProgress
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:258:17 Unresolved reference: progressLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:259:17 Unresolved reference: statusLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/identification/ImageUploadView.kt:260:17 Unresolved reference: errorLayout
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/onboarding/OnboardingActivity.kt:74:38 Unresolved reference: isOnboardingCompleted
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/onboarding/OnboardingActivity.kt:168:34 Unresolved reference: setOnboardingCompleted
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/ActionPlanView.kt:45:43 Unresolved reference: text_action_plan_title
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/ActionPlanView.kt:46:50 Unresolved reference: layout_actions
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/ActionPlanView.kt:47:42 Unresolved reference: image_action_plan_icon
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/ActionPlanView.kt:128:42 Unresolved reference: type
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/ActionPlanView.kt:129:24 Unresolved reference: Type
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/ActionPlanView.kt:130:24 Unresolved reference: Type
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/ActionPlanView.kt:131:24 Unresolved reference: Type
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/FeedbackView.kt:51:43 Unresolved reference: rating_bar
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/FeedbackView.kt:52:46 Unresolved reference: text_feedback_comment
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/FeedbackView.kt:54:42 Unresolved reference: image_feedback_icon
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/FeedbackView.kt:55:46 Unresolved reference: text_thank_you
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/ResultActivity.kt:235:57 Argument type mismatch: actual type is 'kotlin/Function1<@R|kotlin/ParameterName|(name = String(rating))  kotlin/Float, kotlin/Unit>' but 'kotlin/Function2<@R|kotlin/ParameterName|(name = String(rating))  kotlin/Float, @R|kotlin/ParameterName|(name = String(comment))  kotlin/String, kotlin/Unit>' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/ResultActivity.kt:238:40 'when' expression must be exhaustive, add necessary 'else' branch
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/ResultActivity.kt:239:38 Unresolved reference: FeedbackType
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/ResultActivity.kt:240:38 Unresolved reference: FeedbackType
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/ResultActivity.kt:241:38 Unresolved reference: FeedbackType
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/ResultActivity.kt:243:46 Argument type mismatch: actual type is 'kotlin/Unit' but 'com/example/askchinna/ui/results/FeedbackType' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/ui/results/ResultViewModel.kt:132:54 Unresolved reference: identifyPestOrDisease
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/DateTimeUtils.kt:32:13 Const 'val' is only allowed on top level, in named objects, or in companion objects
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/DateTimeUtils.kt:33:13 Const 'val' is only allowed on top level, in named objects, or in companion objects
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/DateTimeUtils.kt:34:13 Const 'val' is only allowed on top level, in named objects, or in companion objects
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/DateTimeUtils.kt:35:13 Const 'val' is only allowed on top level, in named objects, or in companion objects
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/DateTimeUtils.kt:303:33 Unresolved reference: DISPLAY_DATE_FORMAT
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/DateTimeUtils.kt:313:33 Unresolved reference: DISPLAY_TIME_FORMAT
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/DateTimeUtils.kt:323:33 Unresolved reference: DISPLAY_DATE_TIME_FORMAT
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/PdfGenerator.kt:485:21 Val cannot be reassigned
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/SessionManager.kt:115:37 Unresolved reference: hasSessionExpired
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/SessionManager.kt:139:39 Assignment type mismatch: actual type is 'kotlin/Long' but 'kotlin/Int?' was expected
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/SessionManager.kt:165:32 Unresolved reference: hasSessionExpired
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/SessionManager.kt:213:41 Overload resolution ambiguity between candidates: [@IntrinsicConstEvaluation() fun compareTo(other: Byte): Int, @IntrinsicConstEvaluation() fun compareTo(other: Double): Int, @IntrinsicConstEvaluation() fun compareTo(other: Float): Int, ...]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/SessionManager.kt:213:54 Unresolved reference: MAX_MONTHLY_IDENTIFICATIONS
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/SessionManager.kt:222:5 Conflicting overloads: [fun incrementUsageCount(): Boolean]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/SessionManager.kt:226:42 Unresolved reference: isWithinLastNDays
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/SessionManager.kt:236:40 Overload resolution ambiguity between candidates: [@IntrinsicConstEvaluation() fun compareTo(other: Byte): Int, @IntrinsicConstEvaluation() fun compareTo(other: Double): Int, @IntrinsicConstEvaluation() fun compareTo(other: Float): Int, ...]
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/SessionManager.kt:236:53 Unresolved reference: MAX_MONTHLY_IDENTIFICATIONS
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/SessionManager.kt:249:57 Unresolved reference: isWithinLastNDays
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/SessionManager.kt:252:24 Unresolved reference: MAX_MONTHLY_IDENTIFICATIONS
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/SessionManager.kt:255:23 Unresolved reference: MAX_MONTHLY_IDENTIFICATIONS
e: file:///C:/Users/raman/AndroidStudioProjects/askChinna/app/src/main/java/com/example/askchinna/util/SessionManager.kt:325:5 Conflicting overloads: [fun incrementUsageCount(): UsageLimit]
AAPT2 aapt2-8.2.2-10154469-windows Daemon #0: shutdown
AAPT2 aapt2-8.2.2-10154469-windows Daemon #1: shutdown
[Incubating] Problems report is available at: file:///C:/Users/raman/AndroidStudioProjects/askChinna/build/reports/problems/problems-report.html
                                                                                                                  
FAILURE: Build completed with 2 failures.

1: Task failed with an exception.
-----------
* What went wrong:
Execution failed for task ':app:kspReleaseKotlin'.
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction
   > Compilation error. 