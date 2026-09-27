# PulsePad — Universal Multifunctional Bluetooth Remote & Controller

[![Release](https://img.shields.io/badge/Release-v1.2.0-cyan.svg)](PulsePad_PC_Remote.apk)
[![Platform](https://img.shields.io/badge/Platform-Windows%20|%20Android%20|%20Apple%20iOS%20|%20macOS%20|%20Smart%20TV-blue.svg)](PulsePad_PC_Remote.apk)
[![Bluetooth](https://img.shields.io/badge/Bluetooth-HID%20Standard%20Profile-green.svg)](PulsePad_PC_Remote.apk)

**PulsePad** turns your Android phone into a universal, high-performance Bluetooth HID keyboard, trackpad, air mouse, and media remote controller. 

No server software or drivers required on the target device — it pairs natively via standard Bluetooth HID with **Windows PCs**, **Android phones & tablets**, **Apple devices (iPhone, iPad, Mac)**, and **Smart TVs**.

For controlling your PC from an **Apple iPhone or iPad**, PulsePad also includes a zero-latency companion system with 1-tap installation!

---

## 📥 Downloads

- **[PulsePad_PC_Remote.apk](PulsePad_PC_Remote.apk)** (Latest Android Release APK — 100% Standalone)

---

## 🌟 Key Features

### 1. 🌐 Universal Cross-Platform Support
- **💻 Windows 10 / 11 PC**: Native keyboard shortcuts (Win+D, Alt+Tab, Win+Shift+S), Start Menu injection, smooth precision trackpad, media controls.
- **🤖 Android Phones & Tablets**: Hardware Back key, Home key, App Switcher (Recents), Search, and native on-screen mouse pointer with right-click as Back.
- **🍎 Apple (iPhone, iPad, Mac)**: Command (`⌘`), Option (`⌥`), Spotlight Search (`⌘ + Space`), App Switcher (`⌘ + Tab`), Screenshot (`⌘ + Shift + 3`), native iPadOS circular cursor, and iOS AssistiveTouch pointer.
- **📺 Smart TV & Media Boxes**: Universal D-Pad navigation, Home, Back, Menu, Mute, Volume, and media playback control.

### 2. ⚡ 1-Tap Target Profile Switcher
- Tap the **Platform Badge** in the top status bar (`💻 WIN`, `🤖 AND`, `🍎 IOS`, `📺 TV`) to instantly switch active key mappings, trackpad quick-actions, and gesture hints.

### 3. 🎯 Multifunctional Trackpad & Quick Action Bar
- **Precision Trackpad**: 1-finger tap for left click, 2-finger tap for right click, 3-finger horizontal swipe for window switching, 3-finger swipe down for desktop.
- **OS Quick Bar**: Tailored quick buttons placed directly above mouse click buttons (Back/Home/Recents for Android; Spotlight/Switcher/Lock for Apple; Start/Desktop/Task View for Windows; D-Pad navigation for TV).
- **Physical Click Bar**: Left Click, Middle Click (Wheel click), and Right Click.
- **Dedicated Vertical Scroll Strip**: Inertial wheel scrolling with direction invert toggle.

### 4. ⌨️ Mechanical Keyboard with Dynamic Keys
- **Auto-Adapting Modifiers**: Switches between `WIN`/`ALT` on PC, `⌘ CMD`/`⌥ OPT` on Apple, and `SEARCH`/`ALT` on Android.
- **Landscape PC Desktop Mode**: Full QWERTY keyboard with F1–F12 function row, Del, Ins, PgUp, PgDn, and arrow navigation cluster.
- **Acoustic Sound Engine**: Real-time synthesized mechanical switch sound feedback (Blue Clicky, Brown Tactile, Red Linear, Cyber Synth).
- **RGB Cyber Themes**: Cyber Cyan, Synthwave Purple, Matrix Green, Solar Amber, Crimson OLED, Nordic Ice, Mono Stealth.

### 5. 🎈 Gyroscopic 6-DOF Air Mouse
- Tilt and wave your phone in the air to move the cursor on your PC, tablet, or TV using your phone's built-in IMU sensors.

---

## 📱 Quick Pairing Guides

### Pairing with Windows 10/11 PC
1. Open PulsePad and tap the status pill to open **Bluetooth Devices**.
2. Tap **Make Discoverable**.
3. On your PC: Open **Settings** → **Bluetooth & devices** → **Add device** → **Bluetooth**.
4. Select your phone's name. Windows will pair directly as a wireless Keyboard and Mouse!

### Pairing with Android Phones & Tablets
1. Tap **Make Discoverable** in PulsePad.
2. On the target Android phone or tablet: Open **Settings** → **Connected devices** → **Pair new device**.
3. Select your phone's name and confirm pairing.
4. Android will instantly show a native cursor on trackpad touch! (Right click acts as Back; middle click acts as Home).

### Pairing with Apple (iPhone / iPad / Mac)
1. Tap **Make Discoverable** in PulsePad.
2. On your iPad, iPhone, or Mac: Open **Settings** → **Bluetooth**.
3. Under *Other Devices*, select your phone's name and tap **Pair**.
4. **iPad & Mac**: Cursor works out of the box!
5. **iPhone pointer**: Go to **Settings** → **Accessibility** → **Touch** → Turn **AssistiveTouch** ON for the on-screen mouse pointer.

### Pairing with Smart TV / Media Box
1. Tap **Make Discoverable** in PulsePad.
2. On your Smart TV: Open **Settings** → **Remotes & Accessories** → **Add accessory**.
3. Select your phone's name to connect.

---

## 🍏 Using an iPhone / iPad as the Remote (iOS Edition)

Because Apple restricts third-party Bluetooth HID peripheral emulation on iOS, PulsePad provides two official methods to use an iPhone or iPad as the remote controller for your PC:

### ⚡ Method 1: 1-Tap Instant Web App (No Mac / Zero Expiration)
1. On your Windows PC, open PowerShell and run:
   ```powershell
   python pulsepad_server.py
   ```
2. The terminal will display your PC's IP address (e.g., `http://192.168.1.7:8080`).
3. On your iPhone or iPad, connect to the same Wi-Fi and open that URL in **Safari**.
4. Tap the **Share** button in Safari, scroll down, and tap **"Add to Home Screen"**.
5. Tap **Add**. A full-screen PulsePad app is now installed on your iOS home screen!

### 🛠️ Method 2: Native SwiftUI App (Xcode / AltStore)
- Open `PulsePad_iOS` in Xcode 15+.
- Select your free Apple ID under Signing & Capabilities.
- Connect your device and build with `Cmd + R`.

---

## 🛠️ Building the Android APK from Source

```bash
cd BluetoothRemote
./gradlew assembleRelease --no-daemon
```
The output APK will be generated at `BluetoothRemote/app/build/outputs/apk/release/app-release.apk`.
