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

# Giữ lại các data class dùng để parse kết quả từ Gmail API và Room Database
-keep class com.gbao86.sub_lazy.data.** { *; }

# Cấu hình annotations cho serialization (Gson/Moshi/Retrofit)
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses

# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep @androidx.room.Entity class * { *; }
-keep class * extends androidx.room.RoomDatabase

# WorkManager
-keep class * extends androidx.work.ListenableWorker {
    <init>(...);
}
-keep class * extends androidx.work.Worker {
    <init>(...);
}

# Moshi
-keep class * extends com.squareup.moshi.JsonAdapter {
    <init>(...);
}
-keep class * extends com.squareup.moshi.JsonAdapter$Factory {
    <init>(...);
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class *$$JsonAdapter { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# NotificationListenerService
-keep class * extends android.service.notification.NotificationListenerService { *; }

# Kotlin Metadata
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes KotlinMetadata