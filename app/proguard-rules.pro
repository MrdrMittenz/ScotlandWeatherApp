-keep class com.scotlandweather.app.data.model.** { *; }
-keepattributes *Annotation*
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
