# Changelog

## [2024-03-03] Fixed default activity not found error
- Updated `android/app/src/main/AndroidManifest.xml` to use fully qualified class names for `MainActivity` and `GyroscopeService`.
- Removed deprecated `package` attribute from the `<manifest>` tag, relying on `namespace` in `build.gradle.kts` instead. This prevents IDEs and build tools from failing to locate the default launcher activity.