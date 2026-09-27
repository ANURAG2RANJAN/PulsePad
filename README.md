# PulsePad PC - Apple iOS Edition (iPhone & iPad)

Welcome to **PulsePad PC** for Apple iOS. Because Apple tightly restricts third-party Bluetooth HID peripheral emulation on iOS, PulsePad PC provides **two official methods** for 100% free installation on any iPhone or iPad.

---

## ⚡ Method 1: 1-Tap Instant Installation (Recommended — 100% Free, Zero Expiration)

No Mac, no Xcode, no Apple Developer Account, and no 7-day provisioning expiration timer required.

### Step 1: Start Companion Server on your PC
1. On your Windows PC, open PowerShell or Terminal in this folder.
2. Run:
   ```powershell
   python pulsepad_server.py
   ```
3. The server will output your local network address (e.g., `http://192.168.1.7:8080`).

### Step 2: Install to Apple iPhone / iPad
1. Connect your iPhone / iPad to the **same Wi-Fi network** as your PC.
2. Open **Safari** on your iOS device and browse to `http://192.168.1.7:8080` (or whatever IP is printed in your PC terminal).
3. Tap the **Share** button (the square with an arrow pointing up at the bottom of Safari).
4. Scroll down and tap **"Add to Home Screen"**.
5. Tap **Add** in the top-right corner.

🎉 **Done!** A dark-neon **PulsePad PC** icon is now installed on your iOS Home Screen. When opened:
- It launches **full-screen without browser bars** (matching a native iOS app).
- Plays real-time mechanical Cherry switch audio feedback synthesized directly through Apple Web Audio API.
- Delivers multi-touch trackpad gestures (1-tap click, 2-finger right-click, 2-finger inertial scroll).
- Supports dynamic landscape desktop keyboard and portrait typing.
- Never expires!

---

## 🛠️ Method 2: Native SwiftUI Compilation / AltStore Sideloading

For developers or advanced users with a Mac or sideloading utilities:

### Requirements:
- macOS with Xcode 15+ OR Windows with AltStore / Sideloadly.
- Free Apple ID (Personal Team).

### Building with Xcode:
1. Open Xcode and create a new iOS App project named `PulsePad_iOS` (Bundle Identifier: `com.yourname.pulsepad`).
2. Drag and drop the `PulsePad_iOS` source files (`PulsePadApp.swift`, `Models/`, `Services/`, `Views/`, `Info.plist`) into the project navigator.
3. Under **Signing & Capabilities**, select your **Personal Team** (Free Apple ID).
4. Connect your iPhone via USB, select it as the run target, and hit **Cmd + R** to run!
5. On your iPhone, navigate to **Settings > General > VPN & Device Management** and tap **Trust** for your developer certificate.

---

## 🖥️ PC Input Injection Protocol

The companion server (`pulsepad_server.py`) operates via zero-latency Windows C-Types User32 calls:
- `user32.mouse_event` (sub-millisecond relative mouse coordinates, wheel delta, and button states)
- `user32.keybd_event` (Unicode scan codes and direct VK mappings)
- Zero external Python pip dependencies (uses Python standard library).
