# Keep Parcelable models for Room
-keep class com.cyberpunk.debttracker.data.model.** { *; }

# Kotlin
-dontwarn kotlin.**

# Apache POI desktop-only references (AWT/Saxon/Batik/OSGi) - unused on Android
-dontwarn java.awt.**
-dontwarn javax.xml.stream.**
-dontwarn net.sf.saxon.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.**
