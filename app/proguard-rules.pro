# ProGuard rules for Child Monitor App

# Keep DatabaseHelper and its inner classes
-keep class com.example.childmonitor.database.DatabaseHelper { *; }
-keep class com.example.childmonitor.database.DatabaseHelper$* { *; }

# Keep Activities
-keep class com.example.childmonitor.activities.** { *; }

# Keep Services
-keep class com.example.childmonitor.services.** { *; }

# Keep Receivers
-keep class com.example.childmonitor.receivers.** { *; }

# Keep Monitoring classes
-keep class com.example.childmonitor.monitoring.** { *; }

# Keep BCrypt
-keep class org.mindrot.jbcrypt.BCrypt { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# General
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
