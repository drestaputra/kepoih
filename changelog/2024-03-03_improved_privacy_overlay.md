# Changelog

## [2024-03-03] Improved Privacy Overlay
- Updated `OverlayManager.kt` to simulate a physical privacy screen more realistically.
- Replaced the simple random block noise with vertical louvers and pixel scattering to mimic the Moire effect seen on Samsung privacy software and tempered glass.
- Tuned opacity and line thickness scaling according to gyroscope blur intensity.