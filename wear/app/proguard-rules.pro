# WatchBridge ProGuard / R8 kuralları

# ANCS model sınıfları BLE callback'lerinde kullanılır; isimlerini koru.
-keep class com.sasa.watchbridge.ancs.** { *; }

# Kotlin metadata ve imzalar
-keepattributes *Annotation*, InnerClasses, Signature

# Foreground service, receiver ve activity manifest'ten referanslıdır;
# R8 bunları normalde tutar, açıkça belirtmek güvenlidir.
-keep class com.sasa.watchbridge.BridgeService
-keep class com.sasa.watchbridge.BootReceiver
-keep class com.sasa.watchbridge.MainActivity
