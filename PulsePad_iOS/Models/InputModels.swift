import Foundation

enum MouseAction: String, Codable {
    case move
    case click
    case down
    case up
    case wheel
}

enum MouseButton: String, Codable {
    case left
    case right
    case middle
}

struct MousePayload: Codable {
    var action: MouseAction
    var dx: Int? = nil
    var dy: Int? = nil
    var button: MouseButton? = nil
    var wheel: Int? = nil
}

struct KeyboardPayload: Codable {
    var key: String? = nil
    var char: String? = nil
}

struct ConsumerPayload: Codable {
    var code: String
}

struct MacroPayload: Codable {
    var name: String
}

struct TextPayload: Codable {
    var text: String
}
