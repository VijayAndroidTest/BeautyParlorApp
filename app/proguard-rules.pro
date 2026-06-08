# Add project specific ProGuard rules here.

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Proguard annotations
-keep class proguard.annotation.** { *; }
-dontwarn proguard.annotation.**

# Fix common missing warnings for Android libraries
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.util.concurrent.ListenableFuture
-dontwarn androidx.appcompat.view.ContextThemeWrapper
# Replace 'com.example.naguorg' with the actual package where your data models live
-keep class com.example.beautyparlor.** { *; }

# If you use GSON for serialization/deserialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keepclassmembers class com.example.beautyparlor.** {
    <fields>;
}