import SwiftUI

struct TrackpadView: View {
    @ObservedObject var network = NetworkService.shared
    @State private var lastDragLocation: CGPoint? = nil
    @State private var touchPoints: [CGPoint] = []
    @State private var sensitivity: CGFloat = 1.4
    
    var body: some View {
        VStack(spacing: 8) {
            // DPI Selector
            HStack(spacing: 8) {
                Text("SENSITIVITY:")
                    .font(.system(size: 10, weight: .bold, design: .monospaced))
                    .foregroundColor(.gray)
                
                ForEach([("LOW", CGFloat(0.8)), ("BAL", CGFloat(1.4)), ("HIGH", CGFloat(2.2)), ("EXT", CGFloat(3.2))], id: \.0) { label, val in
                    Button(action: {
                        sensitivity = val
                        HapticManager.shared.subtleTap()
                    }) {
                        Text(label)
                            .font(.system(size: 10, weight: .bold))
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(sensitivity == val ? Color.cyan.opacity(0.3) : Color(white: 0.15))
                            .foregroundColor(sensitivity == val ? .cyan : .white)
                            .cornerRadius(4)
                            .overlay(
                                RoundedRectangle(cornerRadius: 4)
                                    .stroke(sensitivity == val ? Color.cyan : Color.clear, lineWidth: 1)
                            )
                    }
                }
                Spacer()
            }
            .padding(.horizontal, 12)
            
            // Touchpad Surface
            GeometryReader { geo in
                ZStack {
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Color(red: 0.06, green: 0.08, blue: 0.14))
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(Color.cyan.opacity(0.2), lineWidth: 1.5)
                        )
                    
                    // Radar Grid Pattern
                    Path { path in
                        let step: CGFloat = 36
                        for x in stride(from: step, to: geo.size.width, by: step) {
                            for y in stride(from: step, to: geo.size.height, by: step) {
                                path.addEllipse(in: CGRect(x: x - 1.5, y: y - 1.5, width: 3, height: 3))
                            }
                        }
                    }
                    .fill(Color.cyan.opacity(0.12))
                    
                    // Ripples
                    ForEach(0..<touchPoints.count, id: \.self) { idx in
                        let pt = touchPoints[idx]
                        Circle()
                            .stroke(Color.cyan.opacity(0.6), lineWidth: 2)
                            .frame(width: 44, height: 44)
                            .position(pt)
                        
                        Circle()
                            .fill(Color.white)
                            .frame(width: 6, height: 6)
                            .position(pt)
                    }
                    
                    // Gesture Detector
                    Color.clear
                        .contentShape(Rectangle())
                        .gesture(
                            DragGesture(minimumDistance: 0)
                                .onChanged { val in
                                    touchPoints = [val.location]
                                    if let last = lastDragLocation {
                                        let dx = Int((val.location.x - last.x) * sensitivity)
                                        let dy = Int((val.location.y - last.y) * sensitivity)
                                        if dx != 0 || dy != 0 {
                                            network.send(endpoint: "mouse", payload: MousePayload(action: .move, dx: dx, dy: dy))
                                        }
                                    }
                                    lastDragLocation = val.location
                                }
                                .onEnded { val in
                                    if lastDragLocation == val.startLocation {
                                        // Tap = Left Click
                                        HapticManager.shared.mouseClick()
                                        network.send(endpoint: "mouse", payload: MousePayload(action: .click, button: .left))
                                    }
                                    lastDragLocation = nil
                                    touchPoints = []
                                }
                        )
                }
            }
            .padding(.horizontal, 8)
            
            // Bottom Mouse Buttons
            HStack(spacing: 8) {
                Button(action: {
                    HapticManager.shared.mouseClick()
                    network.send(endpoint: "mouse", payload: MousePayload(action: .click, button: .left))
                }) {
                    Text("LEFT CLICK")
                        .font(.system(size: 13, weight: .bold))
                        .frame(maxWidth: .infinity, maxHeight: 46)
                        .background(Color(red: 0.08, green: 0.12, blue: 0.20))
                        .foregroundColor(.white)
                        .cornerRadius(10)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color.cyan.opacity(0.3), lineWidth: 1)
                        )
                }
                
                Button(action: {
                    HapticManager.shared.mouseClick()
                    network.send(endpoint: "mouse", payload: MousePayload(action: .click, button: .middle))
                }) {
                    Text("MIDDLE")
                        .font(.system(size: 11, weight: .bold))
                        .frame(width: 80, maxHeight: 46)
                        .background(Color(red: 0.08, green: 0.12, blue: 0.20))
                        .foregroundColor(.gray)
                        .cornerRadius(10)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                        )
                }
                
                Button(action: {
                    HapticManager.shared.mouseClick()
                    network.send(endpoint: "mouse", payload: MousePayload(action: .click, button: .right))
                }) {
                    Text("RIGHT CLICK")
                        .font(.system(size: 13, weight: .bold))
                        .frame(maxWidth: .infinity, maxHeight: 46)
                        .background(Color(red: 0.08, green: 0.12, blue: 0.20))
                        .foregroundColor(.white)
                        .cornerRadius(10)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color.cyan.opacity(0.3), lineWidth: 1)
                        )
                }
            }
            .padding(.horizontal, 8)
            .padding(.bottom, 6)
        }
    }
}
