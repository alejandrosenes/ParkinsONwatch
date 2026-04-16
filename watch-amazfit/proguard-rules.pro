# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK tools proguard configuration.

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Room entities
-keep class com.parkinson.watch.amazfit.data.local.entity.** { *; }

# Keep Ktor serialization classes
-keepnames class kotlinx.serialization.internal.** { *; }

# TensorFlow Lite
-dontwarn org.tensorflow.**
-keep class org.tensorflow.** { *; }
