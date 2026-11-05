import SwiftUI

@main
struct WatchBridgeCompanionApp: App {
    @StateObject private var bridge = BridgeAdvertiser()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(bridge)
        }
    }
}
