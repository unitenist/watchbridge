<div align="center">

# ⌚🔗📱 WatchBridge

### Samsung Galaxy Watch 7'yi iPhone'la konuştur

**iPhone bildirimlerini (WhatsApp, SMS, arama, e‑posta…) Galaxy Watch 7'ye getiren açık kaynak köprü.**
*Open-source bridge that brings iPhone notifications to a Samsung Galaxy Watch 7.*

![Platform](https://img.shields.io/badge/Wear_OS_5-Kotlin-3DDC84?logo=android&logoColor=white)
![iOS](https://img.shields.io/badge/iOS-SwiftUI-000000?logo=apple&logoColor=white)
![BLE](https://img.shields.io/badge/Bluetooth-ANCS-0082FC?logo=bluetooth&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green)
![Status](https://img.shields.io/badge/durum-çalışıyor-17916b)

⭐ İşine yaradıysa bir yıldız bırak — başkaları da bulabilsin!

</div>

---

## 📸 Demo

<!-- GIF hazır olunca aşağıdaki satırın yorumunu kaldır: -->
<!-- ![WatchBridge demo](docs/demo.gif) -->

iPhone'dan gelen bir WhatsApp / SMS bildiriminin birkaç saniye içinde Galaxy Watch 7 ekranında belirişini gösteren kısa bir demo yakında buraya eklenecek. Katkı sağlamak isteyen varsa kendi kurulumundan bir GIF/ekran görüntüsü paylaşabilir (bkz. [CONTRIBUTING](CONTRIBUTING.md)).

---

## Neden?

Galaxy Watch 7, Wear OS 5 ile birlikte **iPhone desteğini resmi olarak kaldırdı.** iPhone kullanıp Galaxy Watch takanlar, bildirimlerini saatte göremediği için ya pahalı üçüncü parti uygulamalara (Merge vb.) para veriyor ya da saati yarım kullanıyor.

**WatchBridge** tam olarak bu boşluğu dolduruyor — hem de o ücretli uygulamaların kullandığı yöntemin aynısıyla: **Apple Notification Center Service (ANCS).** iPhone, bildirimlerini zaten Bluetooth LE üzerinden standart bir servis olarak yayınlıyor; WatchBridge bu servise saatten bağlanıp bildirimleri saatinde gösteriyor. Abonelik yok, sunucu yok, verin cihazından çıkmıyor.

```
iPhone (iOS)                                  Galaxy Watch 7 (Wear OS 5)
┌───────────────────────────┐   BLE    ┌─────────────────────────────────┐
│ WatchBridge Companion     │ ◄─────── │ tarama (5A5A0001… servisi)      │
│  sadece "buradayım" yayını│          │                                 │
│                           │          │ bağlan → bond (eşleşme)         │
│ iOS ANCS servisi (yerleşik)│ ───────► │ Notification Source → yeni olay │
│  bildirimleri BLE'den     │ ◄─────── │ Control Point → "detayı ver"    │
│  paylaşır                 │ ───────► │ Data Source → başlık, mesaj…    │
└───────────────────────────┘          │ → saatte Android bildirimi      │
                                       └─────────────────────────────────┘
```

- **`wear/`** — Kotlin + Jetpack Compose (Wear OS). Asıl iş burada: BLE merkezi, ANCS istemcisi, saatte bildirim + aksiyonlar, arka plan servisi.
- **`ios/WatchBridgeCompanion/`** — SwiftUI + CoreBluetooth. Yalnızca ilk eşleşmede iPhone'u saate görünür yapar; sonrasında kapatabilirsin.

## ✨ Neler çalışıyor

| Özellik | Durum |
|---|---|
| Tüm iPhone bildirimleri (başlık, mesaj, uygulama adı) | ✅ |
| Gelen arama — saatten **Cevapla / Reddet** | ✅ |
| Bildirimi iPhone'da silince saatten de kalkması | ✅ |
| Saatten bildirimi temizleme | ✅ |
| Saat yeniden başlayınca otomatik yeniden bağlanma | ✅ |
| Sessiz / önceden var olan bildirimler titreşimsiz | ✅ |
| Mesajlara saatten yanıt yazma | ❌ ANCS desteklemiyor |
| Müzik kontrolü (AMS) | ⏳ Yol haritasında |
| Adım/nabız → Apple Health | ⏳ Yol haritasında |

## 🚀 Kurulum (özet)

> Ayrıntılı adımlar aşağıda. Gereken: bir Mac (Xcode + ADB), saatin bir kez Android ile ilk kurulumunun yapılmış olması.

**1 · Saate (ADB kablosuz):** Saatte Geliştirici seçenekleri → Kablosuz hata ayıklama'yı aç, `adb pair` + `adb connect`, sonra `wear/` projesini Android Studio'dan **Run** (ya da `./gradlew installDebug`).

**2 · iPhone:** Xcode'da SwiftUI "WatchBridgeCompanion" projesi aç, `ios/WatchBridgeCompanion/` içindeki 3 `.swift` dosyasını ekle, `NSBluetoothAlwaysUsageDescription` ve Background Mode **"Acts as a Bluetooth LE accessory"** ayarla, kendi iPhone'una **Run**.

**3 · Eşleştir:** Companion uygulamayı ön planda tut → saatte WatchBridge'i aç → **iPhone'u eşleştir** → iPhone'da eşleşme + bildirim iznini onayla. Durum **Bağlı ✓** olunca hazır.

<details>
<summary><b>Ayrıntılı kurulum adımları</b></summary>

### Saate yükleme (ADB, kablosuz)
1. Saatte **Ayarlar → Saat hakkında → Yazılım → Yazılım sürümü**'ne 5 kez dokun → Geliştirici seçenekleri açılır.
2. **Geliştirici seçenekleri → ADB hata ayıklama** ve **Kablosuz hata ayıklama**'yı aç (saat ve Mac aynı Wi‑Fi'da).
3. Kablosuz hata ayıklama → **Yeni cihaz eşleştir** → ekrandaki IP:port ve 6 haneli kodla:
   ```bash
   adb pair 192.168.1.50:41234      # eşleştirme portu + kod
   adb connect 192.168.1.50:5555    # ana port
   ```
4. `wear/` klasörünü Android Studio ile aç, cihaz olarak saati seç, **Run**. Ya da `cd wear && ./gradlew installDebug`.

> Saati bir Android telefonla kurduysan, Galaxy Wearable'dan o bağlantıyı **koparma** (saat sıfırlanır). Telefon kapalı/uzakta olabilir.

### iPhone uygulaması
1. Xcode → **New Project → iOS App (SwiftUI)**, adı `WatchBridgeCompanion`.
2. `ios/WatchBridgeCompanion/` içindeki 3 `.swift` dosyasını projeye sürükle (varsayılan `ContentView.swift` ve `…App.swift` yerine).
3. **Info** → `NSBluetoothAlwaysUsageDescription` = "Saatin iPhone'u bulabilmesi için Bluetooth gerekiyor."
4. **Signing & Capabilities → + Background Modes → "Acts as a Bluetooth LE accessory"**.
5. Kendi iPhone'una Run.

### Eşleştirme
1. iPhone'da companion uygulamayı **açık ve ön planda** tut.
2. Saatte WatchBridge → izinleri ver → **iPhone'u eşleştir**.
3. iPhone'da **Bluetooth Eşleşme İsteği → Eşleştir**, sonra **bildirimleri göstermesine izin ver**.
4. iPhone **Ayarlar → Bluetooth → (saat adı) ⓘ → Sistem Bildirimlerini Paylaş** açık olsun.
5. Durum **Bağlı ✓** olunca companion'ı kapatabilirsin; saat kendi yeniden bağlanır.

</details>

## 🧪 Test

Saf Kotlin ANCS parser'ının birim testleri:
```bash
cd wear && ./gradlew test
```

## ⚠️ Bilinen noktalar

- **Yeniden bağlanma:** iPhone rastgele (RPA) BLE adresi kullanır. Bond sonrası Android genelde çözer; takılırsa companion açıkken saatte tekrar "iPhone'u eşleştir".
- **Pil:** Servis `connectedDevice` tipinde foreground service. Takılırsa saatte Ayarlar → Uygulamalar → WatchBridge → Pil → **Kısıtlanmamış**.
- Log: `adb logcat -s WatchBridge GattQueue`

## 🤝 Katkı

Issue ve PR'lar memnuniyetle karşılanır — özellikle **AMS (müzik kontrolü)** ve **HealthKit senkronizasyonu** katkıya açık. Farklı Wear OS 5 saatlerinde test sonuçlarını da paylaşabilirsin.

## 📄 Lisans

MIT — [LICENSE](LICENSE).

## ⚖️ Sorumluluk reddi

Kişisel, deneysel bir projedir. Apple, Samsung veya Google ile bir ilişkisi yoktur; ANCS, AMS gibi adlar ilgili sahiplerine aittir. Yalnızca kendi cihazlarında kullan.
