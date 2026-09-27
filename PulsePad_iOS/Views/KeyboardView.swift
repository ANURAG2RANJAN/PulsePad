import SwiftUI

struct KeyboardView: View {
    @ObservedObject var network = NetworkService.shared
    @State private var textInput: String = ""
    @State private var isShiftActive: Bool = false
    @State private var isCapsLock: Bool = false
    
    let portraitRows = [
        ["ESC", "F1", "F2", "F3", "F4", "F5", "DEL"],
        ["`", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-"],
        ["TAB", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "⌫"],
        ["CAPS", "A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "ENTER"],
        ["SHIFT", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "/"],
        ["CTRL", "WIN", "ALT", "SPACE", "◀", "▲", "▼", "▶"]
    ]
    
    let shiftSymbols: [String: String] = [
        "`": "~", "1": "!", "2": "@", "3": "#", "4": "$", "5": "%",
        "6": "^", "7": "&", "8": "*", "9": "(", "0": ")", "-": "_",
        "=": "+", "[": "{", "]": "}", "\\": "|", ";": ":", "'": "\"",
        ",": "<", ".": ">", "/": "?"
    ]
    
    var body: some View {
        VStack(spacing: 6) {
            // Text Input Bar
            HStack(spacing: 6) {
                TextField("Type on PC...", text: $textInput)
                    .textFieldStyle(PlainTextFieldStyle())
                    .padding(8)
                    .background(Color(red: 0.08, green: 0.12, blue: 0.18))
                    .cornerRadius(8)
                    .foregroundColor(.white)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color.cyan.opacity(0.3), lineWidth: 1)
                    )
                
                Button(action: {
                    if !textInput.isEmpty {
                        HapticManager.shared.keyClick()
                        network.send(endpoint: "text", payload: TextPayload(text: textInput))
                        textInput = ""
                    }
                }) {
                    Text("SEND")
                        .font(.system(size: 11, weight: .bold))
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(Color.cyan)
                        .foregroundColor(.black)
                        .cornerRadius(8)
                }
            }
            .padding(.horizontal, 8)
            
            // Key Grid
            VStack(spacing: 5) {
                ForEach(0..<portraitRows.count, id: \.self) { rowIndex in
                    HStack(spacing: 4) {
                        ForEach(portraitRows[rowIndex], id: \.self) { key in
                            KeyCapView(
                                title: displayTitle(for: key),
                                originalKey: key,
                                isAccent: ["ESC", "ENTER", "BKSP", "⌫"].contains(key),
                                isWide: ["TAB", "CAPS", "SHIFT", "CTRL", "ALT", "WIN"].contains(key),
                                isSpace: key == "SPACE",
                                isActiveMod: (key == "SHIFT" && isShiftActive) || (key == "CAPS" && isCapsLock)
                            ) {
                                handleKeyPress(key)
                            }
                        }
                    }
                }
            }
            .padding(.horizontal, 6)
        }
    }
    
    private func displayTitle(for key: String) -> String {
        if isShiftActive, let shifted = shiftSymbols[key] {
            return shifted
        }
        if key.count == 1 && key.first!.isLetter {
            return (isShiftActive || isCapsLock) ? key : key.lowercased()
        }
        return key
    }
    
    private func handleKeyPress(_ key: String) {
        HapticManager.shared.keyClick()
        
        if key == "SHIFT" {
            isShiftActive.toggle()
            return
        }
        if key == "CAPS" {
            isCapsLock.toggle()
            return
        }
        
        var charToSend = key
        if key.count == 1 {
            if key.first!.isLetter {
                charToSend = (isShiftActive || isCapsLock) ? key : key.lowercased()
            } else if isShiftActive, let shifted = shiftSymbols[key] {
                charToSend = shifted
            }
            if isShiftActive {
                isShiftActive = false
            }
        }
        
        network.send(endpoint: "keyboard", payload: KeyboardPayload(key: charToSend))
    }
}

struct KeyCapView: View {
    let title: String
    let originalKey: String
    let isAccent: Bool
    let isWide: Bool
    let isSpace: Bool
    let isActiveMod: Bool
    let action: () -> Void
    
    @State private var isPressed: Bool = false
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: isSpace ? 11 : 12, weight: .bold))
                .foregroundColor(isActiveMod ? .cyan : (isAccent ? .cyan : .white))
                .frame(maxWidth: isSpace ? .infinity : (isWide ? 58 : 42), minHeight: 40)
                .background(
                    isActiveMod ? Color.cyan.opacity(0.3) :
                    (isPressed ? Color.cyan.opacity(0.4) : (isAccent ? Color.cyan.opacity(0.12) : Color(red: 0.10, green: 0.13, blue: 0.20)))
                )
                .cornerRadius(6)
                .overlay(
                    RoundedRectangle(cornerRadius: 6)
                        .stroke(isActiveMod ? Color.cyan : (isAccent ? Color.cyan.opacity(0.4) : Color.white.opacity(0.1)), lineWidth: 1)
                )
                .scaleEffect(isPressed ? 0.94 : 1.0)
        }
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in isPressed = true }
                .onEnded { _ in isPressed = false }
        )
    }
}
