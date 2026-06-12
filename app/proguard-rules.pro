# ExoPlayer / Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keep interface androidx.media3.** { *; }

# OkHttp + Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-dontwarn com.bumptech.glide.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# App classes
-keep class com.iptvpro.app.** { *; }
-keepclassmembers class com.iptvpro.app.** { *; }

# ViewBinding
-keep class * implements androidx.viewbinding.ViewBinding { *; }

# Lifecycle ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>();
    <init>(android.app.Application);
}

# Suppress warnings for old APIs
-dontwarn android.os.**
-dontwarn java.lang.invoke.**
