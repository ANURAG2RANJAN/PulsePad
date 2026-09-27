import SwiftUI

@main
struct PulsePadApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .preferredColorScheme(.dark)
                .statusBar(hidden: true)
        }
    }
}
