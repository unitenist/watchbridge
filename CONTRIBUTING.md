# Katkı Rehberi

WatchBridge'e katkıda bulunmak isterseniz çok memnun oluruz! 🙌

## Nasıl katkı sağlanır

1. Depoyu **fork**'layın ve kendi dalınızı açın: `git checkout -b ozellik/aciklama`
2. Değişikliğinizi yapın; mümkünse küçük ve odaklı tutun.
3. Kotlin tarafında birim testleri çalıştırın: `cd wear && ./gradlew test`
4. Açıklayıcı bir commit mesajı yazın ve **Pull Request** açın.

## Katkıya en açık alanlar

- **AMS (müzik kontrolü)** — Apple Media Service üzerinden oynat/duraklat/ileri
- **HealthKit senkronizasyonu** — adım ve nabız verisini Apple Health'e taşımak
- Farklı **Wear OS 5** saatlerinde test sonuçları
- Çeviri ve dokümantasyon iyileştirmeleri

## Kod stili

- Kotlin: resmi [Kotlin kod stili](https://kotlinlang.org/docs/coding-conventions.html)
- Swift: Xcode varsayılan biçimlendirmesi

## Sorun bildirimi

Bir hata bulduysanız lütfen bir **Issue** açın; saat modeli, Wear OS sürümü ve `adb logcat -s WatchBridge` çıktısını eklerseniz çok yardımcı olur.

Teşekkürler! ⌚🔗📱
