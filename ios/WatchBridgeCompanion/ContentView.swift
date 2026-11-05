import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var bridge: BridgeAdvertiser

    var body: some View {
        NavigationStack {
            List {
                Section("Durum") {
                    Label(
                        bridge.state,
                        systemImage: bridge.isAdvertising
                            ? "dot.radiowaves.left.and.right"
                            : "antenna.radiowaves.left.and.right.slash"
                    )
                }

                Section("Eşleştirme") {
                    step(1, "Bu ekranı açık tut.")
                    step(2, "Saatte WatchBridge'i aç ve \"iPhone'u eşleştir\"e dokun.")
                    step(3, "iPhone'da çıkan Bluetooth eşleşme isteğini ve bildirim izni sorusunu onayla.")
                    step(4, "Ayarlar → Bluetooth → saatinin adı (i) → \"Sistem Bildirimlerini Paylaş\" açık olsun.")
                }

                Section {
                    if bridge.isAdvertising {
                        Button("Yayını durdur", role: .destructive) { bridge.stopAdvertising() }
                    } else {
                        Button("Yayını başlat") { bridge.startAdvertising() }
                    }
                } footer: {
                    Text("Eşleştikten sonra bu uygulamayı kapatabilirsin; bildirimler iOS üzerinden doğrudan saate gider.")
                }
            }
            .navigationTitle("WatchBridge")
        }
    }

    private func step(_ n: Int, _ text: String) -> some View {
        HStack(alignment: .top, spacing: 10) {
            Text("\(n)")
                .font(.footnote.bold())
                .frame(width: 22, height: 22)
                .background(Circle().fill(Color.accentColor.opacity(0.15)))
            Text(text).font(.callout)
        }
    }
}
