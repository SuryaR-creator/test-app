# GenzPluse Staff Security Hardening & Obfuscation Rules
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep data models
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.domain.model.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose & Navigation
-keepclassmembers class androidx.compose.material.icons.** {
    public static final ** INSTANCE;
}

