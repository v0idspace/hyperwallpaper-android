# ProGuard rules for HyperWallpaper
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.v0idspace.hyperwallpaper.data.model.** { *; }
