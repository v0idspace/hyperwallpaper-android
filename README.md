# 📱 HyperWallpaper (Native Android)

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%208.0%20--%2014+-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/Language-Kotlin%201.9-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/F--Droid-100%25%20FOSS%20%2F%20No%20Trackers-success?style=for-the-badge&logo=fdroid&logoColor=white" alt="F-Droid Ready"/>
  <img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="MIT License"/>
</p>

A standalone native Android application written in **Kotlin + Jetpack Compose (Material 3)** to browse, explore, set, and download official **Xiaomi HyperOS & MIUI Smartphone Wallpapers** directly from Xiaomi's global content delivery networks.

Requires **zero backend servers, zero Python/Node runtimes, and zero account logins**. Runs natively on any modern Android device (Xiaomi 12 Pro, OnePlus 15, Samsung One UI, Pixel AOSP, ColorOS, etc.).

---

## 🌟 Key Features

- 📱 **True Edge-to-Edge Experience**: Fully immersive layout with `enableEdgeToEdge()` and Material 3 `WindowInsets`. Zero letterboxing or black bars.
- 👆 **Tap to Toggle UI**: Tap anywhere on the wallpaper to instantly hide all UI controls for a pure, unobstructed full-screen preview. Tap again to restore controls.
- 🔄 **Endless 2-Column Similar Wallpapers**: Scroll down on any wallpaper to explore an infinite 2-column vertical grid of recommended similar wallpapers inspired by the same style, tags, and creator.
- 🔙 **Deep History Navigation**: Seamless multi-level backstack navigation — tap deep into recommendations and effortlessly back out one level at a time using native Android back gestures.
- 🌏 **Multi-Region Support (EEA / GLOBAL / CN)**:
  - **EEA**: Official European Economic Area catalog.
  - **GLOBAL**: International flagship wallpapers.
  - **CN**: China mainland catalog with native Chinese taxonomy tag mapping (`自然`, `风景`, `暗黑`, `星空`, `极简`, `动漫`, etc.) ensuring full multi-page results.
- 🖼️ **Native Wallpaper Application**: Set wallpapers directly to **Home Screen**, **Lock Screen**, or **Both** via `WallpaperManager`, or launch the native system crop & set picker (`ACTION_CROP_AND_SET_WALLPAPER`).
- 💾 **Scoped Storage Master Downloads**: Directly saves uncompressed JPEG masters (with original EXIF metadata) into `Pictures/HyperWallpapers/` via modern `MediaStore` APIs.
- ⚡ **Optimized Progressive Loading**: Immediate memory and disk cache hits with Coil 2.5, using lightweight thumbnails (`webp/w480q80`) as instant placeholders before full 1080p preview hydration.
- 🎨 **Adaptive Layered Icon**: Custom multi-layered adaptive app launcher icon featuring depth, subtle glow, and frosted glass geometry.
- 💖 **Offline Favorites Vault**: Bookmark your favorite wallpapers locally on your device for instant offline access.

---

## 🛠️ Building the APK

```bash
# Clone the repository
git clone https://github.com/v0idspace/hyperwallpaper-android.git
cd hyperwallpaper-android

# Build Debug APK
./gradlew assembleDebug

# Output APK:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License

Distributed under the **MIT License**.
