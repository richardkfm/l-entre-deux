# ProGuard / R8 rules for l’entre-deux.
# Keep this file small. Add a rule only when a release build proves it
# is needed, and explain why above the rule.

# Compose, AndroidX, and Room ship their own consumer ProGuard rules
# inside their AARs (consumer-rules.pro). R8 picks those up
# automatically — we do not duplicate them here.

# We do not use reflection, JSON deserialization, or JNI in app code,
# so no model- or class-level keep rules are needed.

# Strip Log.v / Log.d / Log.i calls from release builds. We do not call
# these today; the rule is cheap insurance against future regressions
# leaving developer logging in a shipped APK.
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}
