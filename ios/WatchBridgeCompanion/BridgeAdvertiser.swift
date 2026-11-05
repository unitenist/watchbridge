import Foundation
import CoreBluetooth

/// iPhone'u BLE üzerinden görünür yapar ki saat onu bulup bağlanabilsin.
/// Bildirimler için bu uygulamanın sürekli açık olması gerekmez:
/// saat bağlanıp eşleştikten sonra bildirimleri doğrudan iOS'un ANCS servisinden okur.
final class BridgeAdvertiser: NSObject, ObservableObject, CBPeripheralManagerDelegate {

    /// Saattaki Ancs.BRIDGE_SERVICE ile aynı olmalı.
    static let serviceUUID = CBUUID(string: "5A5A0001-7E57-4B1D-9C3A-1A2B3C4D5E6F")
    static let infoUUID = CBUUID(string: "5A5A0002-7E57-4B1D-9C3A-1A2B3C4D5E6F")

    @Published private(set) var state = "Başlatılıyor…"
    @Published private(set) var isAdvertising = false

    private var manager: CBPeripheralManager!
    private var serviceAdded = false

    override init() {
        super.init()
        manager = CBPeripheralManager(
            delegate: self,
            queue: nil,
            options: [CBPeripheralManagerOptionRestoreIdentifierKey: "WatchBridgeAdvertiser"]
        )
    }

    // MARK: - Kontrol

    func startAdvertising() {
        guard manager.state == .poweredOn else { return }
        guard serviceAdded else { addService(); return }
        guard !manager.isAdvertising else { return }
        manager.startAdvertising([
            CBAdvertisementDataServiceUUIDsKey: [Self.serviceUUID],
            CBAdvertisementDataLocalNameKey: "WatchBridge",
        ])
    }

    func stopAdvertising() {
        manager.stopAdvertising()
        isAdvertising = false
        state = "Yayın durduruldu"
    }

    private func addService() {
        let info = CBMutableCharacteristic(
            type: Self.infoUUID,
            properties: [.read],
            value: Data("WatchBridge".utf8),
            permissions: [.readable]
        )
        let service = CBMutableService(type: Self.serviceUUID, primary: true)
        service.characteristics = [info]
        manager.add(service)
    }

    // MARK: - CBPeripheralManagerDelegate

    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        switch peripheral.state {
        case .poweredOn:
            state = "Bluetooth açık"
            startAdvertising()
        case .unauthorized:
            state = "Bluetooth izni yok — Ayarlar'dan izin ver"
        case .poweredOff:
            state = "Bluetooth kapalı"
            isAdvertising = false
        default:
            state = "Bluetooth hazır değil"
        }
    }

    func peripheralManager(_ peripheral: CBPeripheralManager, willRestoreState dict: [String: Any]) {
        if let services = dict[CBPeripheralManagerRestoredStateServicesKey] as? [CBMutableService],
           services.contains(where: { $0.uuid == Self.serviceUUID }) {
            serviceAdded = true
        }
    }

    func peripheralManager(_ peripheral: CBPeripheralManager, didAdd service: CBService, error: Error?) {
        if let error {
            state = "Servis eklenemedi: \(error.localizedDescription)"
            return
        }
        serviceAdded = true
        startAdvertising()
    }

    func peripheralManagerDidStartAdvertising(_ peripheral: CBPeripheralManager, error: Error?) {
        if let error {
            isAdvertising = false
            state = "Yayın başlatılamadı: \(error.localizedDescription)"
        } else {
            isAdvertising = true
            state = "Saat bekleniyor — saatte \"iPhone'u eşleştir\"e dokun"
        }
    }
}
