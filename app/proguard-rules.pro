-keep class com.cyberpunk.debttracker.data.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn kotlin.**
