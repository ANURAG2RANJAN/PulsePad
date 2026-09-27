import SwiftUI

enum ActiveTab {
    case trackpad
    case keyboard
    case media
    case macros
    case settings
}

struct ContentView: View {
    @ObservedObject var network = NetworkService.shared
    @State private var selectedTab: ActiveTab = .trackpad
    
    var body: some View {
        ZStack {
            Color(red: 0.04, green: 0.05, blue: 0.08)
                .ignoresSafeArea()
            
            VStack(spacing: 8) {
                // Top Status Bar HUD
                HStack(spacing: 10) {
                    HStack(spacing: 6) {
                        Text("PULSEPAD")
                            .font(.system(size: 15, weight: .black, design: .rounded))
                            .foregroundColor(.white)
                        Text("PC")
                            .font(.system(size: 11, weight: .bold))
                            .padding(.horizontal, 4)
                            .padding(.vertical, 2)
                            .background(Color.cyan.opacity(0.2))
                            .foregroundColor(.cyan)
                            .cornerRadius(4)
                    }
                    
                    Spacer()
                    
                    // Telemetry
                    HStack(spacing: 8) {
                        Text("\(network.packetRate) Hz")
                            .font(.system(size: 10, weight: .bold, design: .monospaced))
                            .foregroundColor(.cyan)
                        
                        Circle()
                            .fill(network.isConnected ? Color.green : Color.red)
                            .frame(width: 8, height: 8)
                        
                        Text(network.isConnected ? "ONLINE" : "SEARCHING")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(network.isConnected ? .green : .red)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color(white: 0.12))
                    .cornerRadius(12)
                }
                .padding(.horizontal, 14)
                .padding(.top, 6)
                
                // Screen Content
                Group {
                    switch selectedTab {
                    case .trackpad:
                        TrackpadView()
                    case .keyboard:
                        KeyboardView()
                    case .media:
                        MediaControlsView()
                    case .macros:
                        MacroDeckView()
                    case .settings:
                        SettingsView()
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                
                // Bottom Tab Bar
                HStack(spacing: 4) {
                    TabButton(title: "Trackpad", icon: "cursorarrow.rays", isSelected: selectedTab == .trackpad) {
                        selectedTab = .trackpad
                    }
                    TabButton(title: "Keyboard", icon: "keyboard", isSelected: selectedTab == .keyboard) {
                        selectedTab = .keyboard
                    }
                    TabButton(title: "Media", icon: "play.tv", isSelected: selectedTab == .media) {
                        selectedTab = .media
                    }
                    TabButton(title: "Macros", icon: "bolt.fill", isSelected: selectedTab == .macros) {
                        selectedTab = .macros
                    }
                    TabButton(title: "Settings", icon: "gearshape.fill", isSelected: selectedTab == .settings) {
                        selectedTab = .settings
                    }
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 6)
                .background(Color(red: 0.07, green: 0.09, blue: 0.14))
                .cornerRadius(12)
                .padding(.horizontal, 8)
                .padding(.bottom, 2)
            }
        }
    }
}

struct TabButton: View {
    let title: String
    let icon: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: {
            HapticManager.shared.subtleTap()
            action()
        }) {
            VStack(spacing: 3) {
                Image(systemName: icon)
                    .font(.system(size: 15, weight: .bold))
                Text(title)
                    .font(.system(size: 9, weight: .semibold))
            }
            .frame(maxWidth: .infinity, minHeight: 40)
            .foregroundColor(isSelected ? .cyan : .gray)
            .background(isSelected ? Color.cyan.opacity(0.15) : Color.clear)
            .cornerRadius(8)
        }
    }
}

struct MediaControlsView: View {
    @ObservedObject var network = NetworkService.shared
    
    var body: some View {
        VStack(spacing: 12) {
            Text("MULTIMEDIA & AUDIO DECK")
                .font(.system(size: 11, weight: .bold, design: .monospaced))
                .foregroundColor(.cyan)
            
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                MediaCard(title: "VOL UP", icon: "speaker.wave.3.fill") {
                    network.send(endpoint: "consumer", payload: ConsumerPayload(code: "VOL_UP"))
                }
                MediaCard(title: "MUTE", icon: "speaker.slash.fill") {
                    network.send(endpoint: "consumer", payload: ConsumerPayload(code: "MUTE"))
                }
                MediaCard(title: "VOL DOWN", icon: "speaker.wave.1.fill") {
                    network.send(endpoint: "consumer", payload: ConsumerPayload(code: "VOL_DOWN"))
                }
                MediaCard(title: "PREV", icon: "backward.fill") {
                    network.send(endpoint: "consumer", payload: ConsumerPayload(code: "PREV_TRACK"))
                }
                MediaCard(title: "PLAY / PAUSE", icon: "playpause.fill") {
                    network.send(endpoint: "consumer", payload: ConsumerPayload(code: "PLAY_PAUSE"))
                }
                MediaCard(title: "NEXT", icon: "forward.fill") {
                    network.send(endpoint: "consumer", payload: ConsumerPayload(code: "NEXT_TRACK"))
                }
            }
            .padding(.horizontal, 16)
            Spacer()
        }
        .padding(.top, 16)
    }
}

struct MediaCard: View {
    let title: String
    let icon: String
    let action: () -> Void
    
    var body: some View {
        Button(action: {
            HapticManager.shared.keyClick()
            action()
        }) {
            VStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.cyan)
                Text(title)
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(.white)
            }
            .frame(maxWidth: .infinity, minHeight: 90)
            .background(Color(red: 0.08, green: 0.11, blue: 0.18))
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.cyan.opacity(0.2), lineWidth: 1)
            )
        }
    }
}

struct MacroDeckView: View {
    @ObservedObject var network = NetworkService.shared
    
    let macros: [(String, String, String)] = [
        ("Task Manager", "Ctrl+Shift+Esc", "taskmgr"),
        ("Screen Snipping", "Win+Shift+S", "snip"),
        ("Show Desktop", "Win+D", "desktop"),
        ("Lock Workstation", "Win+L", "lock"),
        ("Command Palette", "Ctrl+Shift+P", "vscode"),
        ("Toggle Terminal", "Ctrl+`", "terminal"),
        ("Close Window", "Alt+F4", "altf4"),
        ("App Switcher", "Alt+Tab", "alttab")
    ]
    
    var body: some View {
        VStack(spacing: 12) {
            Text("PRODUCTIVITY MACRO SUITE")
                .font(.system(size: 11, weight: .bold, design: .monospaced))
                .foregroundColor(.cyan)
            
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                ForEach(macros, id: \.2) { title, subtitle, id in
                    Button(action: {
                        HapticManager.shared.keyClick()
                        network.send(endpoint: "macro", payload: MacroPayload(name: id))
                    }) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(title)
                                .font(.system(size: 13, weight: .bold))
                                .foregroundColor(.white)
                            Text(subtitle)
                                .font(.system(size: 10, weight: .medium, design: .monospaced))
                                .foregroundColor(.cyan)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(14)
                        .background(Color(red: 0.08, green: 0.11, blue: 0.18))
                        .cornerRadius(10)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color.cyan.opacity(0.2), lineWidth: 1)
                        )
                    }
                }
            }
            .padding(.horizontal, 16)
            Spacer()
        }
        .padding(.top, 16)
    }
}

struct SettingsView: View {
    @ObservedObject var network = NetworkService.shared
    @State private var ipText: String = ""
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("DEVICE & NETWORK SETTINGS")
                .font(.system(size: 12, weight: .bold, design: .monospaced))
                .foregroundColor(.cyan)
            
            VStack(alignment: .leading, spacing: 6) {
                Text("PC Server Host / IP Address")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(.white)
                
                HStack(spacing: 8) {
                    TextField("192.168.1.7:8080", text: $ipText)
                        .textFieldStyle(PlainTextFieldStyle())
                        .padding(10)
                        .background(Color(red: 0.09, green: 0.12, blue: 0.19))
                        .cornerRadius(8)
                        .foregroundColor(.white)
                    
                    Button("Save") {
                        HapticManager.shared.subtleTap()
                        network.updateServerAddress(ipText)
                    }
                    .font(.system(size: 12, weight: .bold))
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(Color.cyan)
                    .foregroundColor(.black)
                    .cornerRadius(8)
                }
            }
            .padding(14)
            .background(Color(red: 0.07, green: 0.09, blue: 0.15))
            .cornerRadius(12)
            
            VStack(alignment: .leading, spacing: 6) {
                Text("Zero-Cost Apple Installation Notice")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(.cyan)
                
                Text("You can also run PulsePad completely free without Xcode or Apple Developer accounts by opening Safari to http://\(network.serverAddress) and selecting 'Add to Home Screen'.")
                    .font(.system(size: 11))
                    .foregroundColor(.gray)
            }
            .padding(14)
            .background(Color(red: 0.07, green: 0.09, blue: 0.15))
            .cornerRadius(12)
            
            Spacer()
        }
        .padding(16)
        .onAppear {
            ipText = network.serverAddress
        }
    }
}
