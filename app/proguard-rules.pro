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

# Recommended: Preserve line number information for debugging stack traces from release builds.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name. (Keep this commented if you use the above)
#-renamesourcefileattribute SourceFile

# --- Rules for Gson (if you use it for serializing your model classes) ---
# Replace 'com.lekan.bodyfattracker.model.**' with your actual model package(s) if different.
# If you have multiple model packages, add a rule for each.
-keep class com.lekan.bodyfattracker.model.** { *; }
-keepclassmembers enum com.lekan.bodyfattracker.model.** { *; }

# --- Add any other library-specific rules below ---
# For example, if Vico or another library requires them.

# --- Rules for Kotlinx Serialization (if you use @Serializable) ---
    # Adjust package 'com.lekan.bodyfattracker.model.**' if your @Serializable classes are elsewhere
-keep @kotlinx.serialization.Serializable class com.lekan.bodyfattracker.model.** { *; }

# Keep the KSerializer implementations and the generated $serializer fields for @Serializable classes
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <methods>;
    public static kotlinx.serialization.KSerializer serializer(...);
}
# Keep Companion object's serializer method if present
-keepclassmembers class *$Companion {
    public final kotlinx.serialization.KSerializer serializer(...);
}
# Keep specific internal classes used by kotlinx.serialization
-keep class kotlinx.serialization.internal.* { *; }
-keep class kotlinx.serialization.PolymorphicSerializer { *; }
-keep class kotlinx.serialization.SealedClassSerializer { *; }
