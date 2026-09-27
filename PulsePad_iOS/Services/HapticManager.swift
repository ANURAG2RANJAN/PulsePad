import UIKit

final class HapticManager {
    static let shared = HapticManager()
    
    private let lightImpact = UIImpactFeedbackGenerator(style: .light)
    private let mediumImpact = UIImpactFeedbackGenerator(style: .medium)
    private let rigidImpact = UIImpactFeedbackGenerator(style: .rigid)
    private let heavyImpact = UIImpactFeedbackGenerator(style: .heavy)
    private let selectionFeedback = UISelectionFeedbackGenerator()
    
    private init() {
        prepareAll()
    }
    
    func prepareAll() {
        lightImpact.prepare()
        mediumImpact.prepare()
        rigidImpact.prepare()
        heavyImpact.prepare()
        selectionFeedback.prepare()
    }
    
    func keyClick() {
        rigidImpact.impactOccurred(intensity: 0.85)
    }
    
    func mouseClick() {
        heavyImpact.impactOccurred(intensity: 1.0)
    }
    
    func scrollTick() {
        selectionFeedback.selectionChanged()
    }
    
    func subtleTap() {
        lightImpact.impactOccurred(intensity: 0.5)
    }
}
