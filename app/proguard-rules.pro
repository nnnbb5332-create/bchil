# Proguard rules for Child Monitor App

# Keep all classes in our app package
-keep class com.example.childmonitor.** { *; }

# Keep Google Play Services
-keep class com.google.android.gms.** { *; }

# Keep OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep Gson
-keep class com.google.gson.** { *; }

# Keep Room
-keep class androidx.room.** { *; }

# Keep WorkManager
-keep class androidx.work.** { *; }

# Keep Android X
-keep class androidx.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
