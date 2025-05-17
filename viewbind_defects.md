# View Binding Defects & Rules

## Common Issues

1. **Unresolved reference to Binding Class**
   - Cause: Layout file name mismatch, wrong root element, or not in `res/layout`.
   - Rule: Layout file must be in `src/main/res/layout` and named in snake_case (e.g., `view_detail_expandable.xml`).

2. **Binding Not Generated for `<merge>` Root**
   - Cause: Using `<merge>` as root in a layout for a custom view.
   - Rule: Use a concrete layout root (e.g., `LinearLayout`, `CardView`) for custom views that use direct binding.

3. **Import Errors**
   - Cause: Missing or incorrect import for the generated binding class.
   - Rule: Always import the binding class from the correct package (e.g., `com.example.askchinna.databinding.ViewDetailExpandableBinding`).

4. **IDE/Build System Not Recognizing Generated Sources**
   - Cause: IDE not indexing `build/generated` or stale cache.
   - Rule: Restart IDE after layout changes, ensure generated sources are marked as source roots.

5. **Multiple Layouts with Same Name**
   - Cause: Duplicate layout files in different resource folders.
   - Rule: Ensure only one layout file with a given name exists unless intentionally using resource qualifiers.

6. **Direct Use of Binding with `<merge>`**
   - Cause: Trying to use binding directly on a `<merge>` root layout.
   - Rule: Only use binding with `<merge>` when included in another layout, not in custom view constructors.

## Prevention Checklist

- [ ] Layout file names match expected binding class names.
- [ ] Layout files are in the correct directory.
- [ ] No duplicate layout files unless using qualifiers.
- [ ] Custom views use a concrete root for direct binding.
- [ ] Imports for binding classes are correct.
- [ ] IDE is restarted after major layout changes.
- [ ] Generated sources are indexed by the IDE.

## Troubleshooting Steps

1. Clean and rebuild the project.
2. Delete `.gradle` and `build` folders if issues persist.
3. Restart the IDE.
4. Check for correct file names, locations, and root elements.
5. Verify imports and source set configuration.

## Implementation Patterns

### Correct Pattern
```kotlin
// Direct initialization with non-nullable val
private val binding: ViewDetailExpandableBinding = ViewDetailExpandableBinding.inflate(
    LayoutInflater.from(context), this, true
)
```

### Incorrect Pattern
```kotlin
// Avoid nullable/lateinit bindings
private lateinit var binding: ViewUsageLimitBinding
// Later trying to bind
binding = ViewUsageLimitBinding.bind(this)
```

## Best Practices

1. **Initialization**
   - Use non-nullable val for binding
   - Initialize in constructor or init block
   - Use inflate() with attachToParent=true for custom views

2. **Cleanup**
   - Implement onDetachedFromWindow() for proper cleanup
   - Set binding to null if using nullable binding
   - Clear any listeners or callbacks

3. **Error Handling**
   - Add try-catch blocks around binding operations
   - Log errors with meaningful messages
   - Provide fallback UI states

4. **State Management**
   - Implement state restoration
   - Handle configuration changes
   - Save/restore view state

5. **Memory Management**
   - Avoid memory leaks
   - Clear references in onDetachedFromWindow
   - Use weak references for callbacks

## Common Fixes

1. **Binding Class Not Found**
   - Verify layout file name matches binding class
   - Check layout file location
   - Clean and rebuild project

2. **Null Pointer Exceptions**
   - Use non-nullable binding
   - Initialize in constructor
   - Add null checks

3. **Layout Inflation Errors**
   - Check root element type
   - Verify layout parameters
   - Ensure proper context

4. **Resource Not Found**
   - Verify resource IDs
   - Check resource qualifiers
   - Clean and rebuild

5. **Memory Leaks**
   - Implement proper cleanup
   - Clear references
   - Use weak references

## Recent Fixes (2025-05-12)

### SummaryView.kt Fixes
- Added `<layout>` tag to `view_summary.xml`
- Added `<data>` section with `android.view.View` import
- Fixed view binding initialization using direct binding
- Maintained proper error handling and cleanup
- Verified binding generation in `build/generated` directory

### ResultActivity.kt Fixes
- Fixed view binding initialization
- Maintained existing layout structure without `<layout>` tag
- Verified binding generation and usage
- Confirmed proper cleanup and error handling

### DetailExpandableView.kt Fixes
- Verified proper `<layout>` and `<data>` structure
- Confirmed correct binding initialization
- Maintained proper error handling and cleanup
- Verified binding generation

### OtpVerificationActivity.kt & activity_otp_verification.xml Fixes
- Fixed LiveData/Flow mismatch: replaced collectLatest with observe for LiveData
- Corrected view binding for included layout: used binding.networkStatusView.root
- Verified binding class generation for both activity and included view
- No build or runtime errors after fix

### dialog_otp_resend.xml & DialogOtpResendBinding Fixes
- Verified correct binding class generation for dialog_otp_resend.xml
- Confirmed no build or runtime errors
- Confirmed correct usage in OtpResendDialogFragment.kt

### view_action_plan.xml & ActionPlanView.kt Fixes
- Verified correct binding and adapter usage
- Confirmed binding class generation
- No build or runtime errors

### Key Learnings
1. Not all layouts need `<layout>` tag if using direct view binding
2. Consistency in binding initialization patterns
3. Proper error handling and cleanup in all views
4. Verification of binding generation in build directory

---

_Last updated: 2025-05-12_ 