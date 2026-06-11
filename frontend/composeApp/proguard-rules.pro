# ========================================
# PROGUARD RULES - Frollot Mobile App
# ========================================

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Kotlin serialization
-keepattributes RuntimeVisibleAnnotations
-keep class kotlin.Metadata { *; }
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Keep data classes for serialization
-keep class com.frollot.mobile.model.** { *; }

# Ktor
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }

# Coil
-keep class coil.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# Compose
-keep class androidx.compose.** { *; }

# Don't warn about missing classes
-dontwarn org.slf4j.**
-dontwarn javax.annotation.**
-dontwarn kotlin.reflect.jvm.internal.**

