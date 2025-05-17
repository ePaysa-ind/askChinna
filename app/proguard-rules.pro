# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# ----------------------------------------------------------------------
#  DataBinding Keep Rules
# ----------------------------------------------------------------------
-keep class androidx.databinding.** { *; }
-keep class com.example.askchinna.databinding.** { *; }  # Replace with your package name!
-keep class * implements androidx.databinding.DataBinderMapper { *; }
-keep @androidx.databinding.BindingMethods class * { *; }
-keep @androidx.databinding.BindingAdapter class * { *; }
-keep class android.databinding.** { *; }
-keep class net.example.**databinding.** { *; } # Add these if you use other package names
-keep class com.example.askchinna.**databinding.** { *; } # Replace with your package name!

-keepnames class androidx.databinding.** {
    *;
}
-keepnames class android.databinding.** {
    *;
}
-keepnames class net.example.**databinding.** {
    *;
} # Add these if you use other package names
-keepnames class com.example.askchinna.**databinding.** {
    *;
}