Setiap perubahan dan commit, buatkan dokumentasi di folder "changelog". KepoIh Technical Specification


KepoIh Technical Specification

Application Name: KepoIh
Package Name: com.drestaputra.kepoih
Platform: Android
Framework: Flutter
Min Android Version: Android 8 (API 26)
Primary Language: Dart + Kotlin (native integration)

1. Application Overview

KepoIh adalah aplikasi privacy protection yang menggunakan sensor gyroscope untuk mendeteksi ketika seseorang mencoba melihat layar dari samping atau ketika perangkat diputar secara mencurigakan.

Ketika pola gerakan tertentu terdeteksi, aplikasi akan:

Mengaktifkan privacy overlay

Menggelapkan layar

Menampilkan privacy warning

Opsional: blur konten layar

2. Core Concept

Aplikasi memonitor perubahan rotasi perangkat menggunakan gyroscope sensor.

Jika rotasi melebihi threshold tertentu dalam waktu singkat, sistem menganggap ada kemungkinan orang lain mencoba mengintip layar.

Contoh kondisi:

Event	Interpretation
Phone suddenly tilted 30° sideways	Someone looking from side
Rapid micro rotation	Someone moving phone
Rotation spike	Suspicious movement

Jika event ini terjadi → privacy mode aktif

3. Main Features
   3.1 Gyroscope Monitoring

Continuous monitoring sensor:

Sensor:

TYPE_GYROSCOPE

Sampling rate:

SENSOR_DELAY_GAME

Data received:

x rotation
y rotation
z rotation
timestamp

Detection threshold:

ROTATION_THRESHOLD = 2.5 rad/s
ROTATION_SPIKE_TIME = 300 ms

Jika:

|x| + |y| + |z| > ROTATION_THRESHOLD

→ Trigger privacy event.

3.2 Privacy Overlay

Ketika event terdeteksi:

Overlay akan muncul:

Mode:

Blur Screen

Black Screen

Warning Screen

Default:

Black translucent overlay
opacity = 0.85
3.3 Background Monitoring

Sensor harus tetap aktif ketika:

App minimized

Screen on

User using other apps

Implementasi:

Foreground Service

Android permission:

FOREGROUND_SERVICE
SYSTEM_ALERT_WINDOW
3.4 Smart Delay

Agar tidak false positive:

Tambahkan cooldown.

PRIVACY_TRIGGER_COOLDOWN = 3 seconds

Flow:

trigger → overlay → cooldown
4. System Architecture
   Flutter UI
   │
   │ MethodChannel
   ▼
   Kotlin Sensor Service
   │
   Gyroscope Listener
   │
   Motion Analyzer
   │
   Privacy Trigger
   │
   Overlay Controller
5. Project Structure
   kepoih/
   │
   ├── lib/
   │   ├── main.dart
   │   ├── app.dart
   │   │
   │   ├── services/
   │   │   ├── gyroscope_service.dart
   │   │   ├── privacy_controller.dart
   │   │
   │   ├── overlays/
   │   │   ├── privacy_overlay.dart
   │   │
   │   └── settings/
   │       ├── settings_page.dart
   │
   ├── android/
   │   └── app/src/main/kotlin/com/drestaputra/kepoih/
   │       ├── GyroscopeService.kt
   │       ├── MotionAnalyzer.kt
   │       ├── OverlayManager.kt
   │
   └── pubspec.yaml
6. Flutter Dependencies
   sensors_plus
   flutter_overlay_window
   permission_handler
   flutter_background_service
7. Gyroscope Detection Algorithm

Pseudo code:

onGyroscopeUpdate(x, y, z):

    magnitude = abs(x) + abs(y) + abs(z)

    if magnitude > ROTATION_THRESHOLD:

        if now - lastTrigger > COOLDOWN:

            triggerPrivacy()
8. Privacy Trigger Flow
   Gyroscope Event
   │
   ▼
   Magnitude Calculation
   │
   ▼
   Threshold Check
   │
   ▼
   Cooldown Check
   │
   ▼
   Privacy Mode Trigger
   │
   ▼
   Overlay Display
9. Overlay Behaviour

Overlay type:

WindowManager Overlay

Flags:

TYPE_APPLICATION_OVERLAY
FLAG_NOT_TOUCHABLE
FLAG_NOT_FOCUSABLE

Default overlay layout:

full screen
black background
opacity 85%

Optional message:

⚠ Privacy Mode Active
10. Android Permissions

AndroidManifest.xml:

<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.WAKE_LOCK"/>
<uses-permission android:name="android.permission.BODY_SENSORS"/>
11. Foreground Service Requirements

Service type:

ForegroundService

Notification required:

"KepoIh protecting your privacy"

Notification importance:

LOW
12. Performance Constraints

CPU target:

< 3%

Sensor polling:

50hz

Memory usage target:

< 60MB
13. UI Requirements

Main screen:

Toggle Privacy Protection
Status indicator
Sensitivity slider

Sensitivity levels:

Low
Medium
High

Threshold mapping:

Mode	Threshold
Low	3.5
Medium	2.5
High	1.8
14. Settings

Configurable:

Sensitivity
Overlay style
Cooldown time
Enable/disable vibration alert
15. Optional Features (Future)

Not required for MVP.

Future roadmap:

AI motion detection
Front camera peek detection
Edge viewing detection
Privacy auto enable per app
16. Security Considerations

App tidak menyimpan data sensor.

Data hanya digunakan untuk:

real-time detection

Tidak ada network request.

17. Build Configuration

Compile SDK:

34

Min SDK:

26

Target SDK:

34
18. MVP Implementation Priority

Priority order:

1️⃣ Gyroscope monitoring
2️⃣ Privacy trigger
3️⃣ Overlay system
4️⃣ Background service
5️⃣ Settings UI

19. Expected Behaviour Example

Scenario:

User using phone in public.

Event:

phone tilted suddenly

Sensor:

x=1.4
y=1.8
z=0.9

Magnitude:

4.1 > threshold

System:

triggerPrivacy()

Result:

screen dimmed
20. Example Event Timeline
    t=0      normal
    t=1.2    phone rotated
    t=1.3    magnitude spike
    t=1.4    privacy triggered
    t=1.5    overlay active
    t=4.5    cooldown finished
21. Coding Agent Instructions

Agent should:

Implement gyroscope listener

Implement motion detection algorithm

Create foreground service

Create overlay controller

Expose control via Flutter MethodChannel

Agent must not:

store sensor data

require internet

use heavy ML libraries

22. Acceptance Criteria

App considered working if:

overlay appears when device rotated quickly

overlay disappears after cooldown

sensor works in background

battery usage minimal

Jika Anda mau, saya juga bisa buatkan versi yang jauh lebih advanced untuk coding agent, misalnya:

PRD (Product Requirement Document) format OpenAI / Claude

Autonomous coding agent prompt

Full repo scaffold (Flutter + Kotlin)

yang bisa membuat AI langsung generate 80% code KepoIh secara otomatis.

tambahkan : pixel hanya akan terlihat pada user yg melihat lurus, jika dari samping tidak jadi buram (bukan karena layar hp miring, namun penentuan pixel yg buram ini gunakan gyroscope). Paham?

Ya, saya paham maksudnya 👍
Anda ingin membuat privacy display berbasis gyroscope, dimana:

Jika user melihat layar lurus (perangkat stabil) → layar tajam

Jika sudut pandang berubah / device sedikit diputar → pixel tertentu diburamkan

Blur bukan karena layar miring, tapi karena algoritma gyroscope memutuskan pixel mana yang dirender blur

Jadi konsepnya seperti software-based privacy screen (mirip tempered glass privacy), tetapi dikontrol sensor.

Saya tambahkan bagian ini ke dokumentasi agar coding agent bisa mengimplementasikannya dengan jelas.

KepoIh Technical Specification (Updated)

Application Name: KepoIh
Package: com.drestaputra.kepoih

Purpose:

Dynamic privacy screen using gyroscope-based pixel rendering.
23. Gyroscope-Based Pixel Privacy System
    Objective

Aplikasi harus mensimulasikan privacy screen hardware menggunakan software rendering.

Perilaku:

Viewing Condition	Result
User melihat lurus	layar normal
Perangkat diputar sedikit	pixel mulai blur
Perangkat diputar lebih jauh	layar semakin tidak terbaca

Perubahan tidak memutar layar, tetapi mengubah rendering pixel.

24. Viewing Angle Model

Gyroscope menghasilkan:

x rotation
y rotation
z rotation

Digunakan untuk menghitung viewing deviation.

Formula:

deviation = sqrt(x² + y²)

Interpretasi:

Deviation	Behaviour
0.0 – 0.5	Clear screen
0.5 – 1.2	Mild blur
1.2 – 2.0	Medium blur

2.0 | Heavy blur |

25. Pixel Obfuscation Engine

Rendering menggunakan dynamic shader / overlay filter.

Blur tidak global, tetapi selective pixel noise.

Metode:

pixel skipping
random noise mask
dynamic blur kernel

Contoh pola:

Normal
████████████

Mild blur
█░█░██░█░██░

Medium blur
░█░░█░░░█░░█

Heavy blur
░░░░░░░░░░░░
26. Pixel Rendering Strategy

Engine akan:

mengambil gyroscope deviation

menghitung blur intensity

mengubah overlay shader

Pseudo flow:

gyroscope update
│
calculate deviation
│
map deviation → blur level
│
update shader intensity
│
render overlay
27. Overlay Rendering Modes

Mode yang harus didukung:

Mode 1 — Pixel Scatter

Random pixel transparency.

visible pixels: 70%
hidden pixels: 30%
Mode 2 — Noise Mask

Overlay noise pattern.

alpha noise shader
Mode 3 — Gaussian Blur

Standard blur.

Kernel size:

Level	Kernel
Mild	3px
Medium	7px
Heavy	15px
28. Dynamic Blur Calculation

Function:

blurIntensity = clamp(deviation * sensitivity)

Example:

deviation = 1.4
sensitivity = 1.2

blurIntensity = 1.68

Mapping:

0 → no blur
1 → mild
2 → medium
3 → heavy
29. Gyroscope Sampling

Sampling rate:

50hz

Smoothing filter:

low pass filter

Formula:

filtered = previous * 0.8 + new * 0.2

Purpose:

reduce jitter
30. Pixel Privacy Activation

System harus tidak langsung blur saat rotasi kecil.

Activation threshold:

deviation > 0.4

Deactivation threshold:

deviation < 0.2

Ini mencegah flickering blur.

31. Rendering Implementation

Preferred method:

Flutter Shader

Using:

FragmentProgram

Alternative:

BackdropFilter

Jika performa tidak cukup:

OpenGL shader via Android native
32. Shader Inputs

Shader harus menerima parameter:

blurIntensity
noiseSeed
pixelSkipRate

Example:

uniform float blurIntensity;
uniform float noiseSeed;
uniform float pixelSkip;
33. Gyroscope → Blur Mapping

Mapping function:

blur = min(3, deviation * sensitivity)

Example table:

deviation	blur
0.2	0
0.8	1
1.6	2
2.5	3
34. Performance Requirement

Blur rendering target:

> 45 FPS

Maximum GPU load:

< 20%

Latency:

< 60ms
35. Privacy Guarantee

Jika orang melihat dari samping:

gyroscope detect orientation change

Result:

screen unreadable

Jika user memegang lurus kembali:

screen clear again
36. UX Behaviour

User tidak boleh melihat transisi blur yang terlalu kasar.

Gunakan animasi:

blur transition duration: 120ms
37. Edge Cases

Kasus yang harus dihandle:

User walking

Gyroscope fluctuation harus tidak langsung memicu blur berat.

Gunakan:

moving average
Phone in pocket

Sensor spike harus diabaikan.

Rule:

if magnitude > 8 rad/s
ignore event
38. Security Advantage

Keuntungan pendekatan ini:

No special screen hardware required

Privacy screen berbasis software.

39. Coding Agent Implementation Priority

Agent harus implement:

1️⃣ gyroscope listener
2️⃣ deviation calculator
3️⃣ blur level mapping
4️⃣ shader renderer
5️⃣ overlay integration

40. Expected Visual Result

User view:

Screen clear

Side observer:

Screen looks noisy / blurred

💡 Catatan penting (secara engineering):

Ide Anda ini sangat unik, tetapi ada batasan fisika layar:

Gyroscope tidak benar-benar tahu orang melihat dari samping.
Yang bisa dilakukan adalah:

simulate privacy filter using motion heuristics

Namun tetap sangat realistis untuk dibuat.