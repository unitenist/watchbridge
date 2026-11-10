package com.sasa.watchbridge.ancs

import java.util.UUID

/** Apple Notification Center Service (ANCS) sabitleri — Apple'ın yayımladığı spesifikasyona göre. */
object Ancs {
    val SERVICE: UUID = UUID.fromString("7905F431-B5CE-4E99-A40F-4B1E122D00D0")
    val NOTIFICATION_SOURCE: UUID = UUID.fromString("9FBF120D-6301-42D9-8C58-25E699A21DBD")
    val CONTROL_POINT: UUID = UUID.fromString("69D1D8F3-45E1-49A8-9821-9BBDFDAAD9D9")
    val DATA_SOURCE: UUID = UUID.fromString("22EAC6E9-24D6-4BB5-BE44-B36ACE7C7BFB")
    val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

    /** iPhone'daki companion uygulamanın yayınladığı servis; saat iPhone'u bununla bulur. */
    val BRIDGE_SERVICE: UUID = UUID.fromString("5A5A0001-7E57-4B1D-9C3A-1A2B3C4D5E6F")
}

enum class EventId(val code: Int) {
    ADDED(0), MODIFIED(1), REMOVED(2);

    companion object {
        fun of(code: Int): EventId? = entries.firstOrNull { it.code == code }
    }
}

enum class Category(val code: Int, val label: String) {
    OTHER(0, "Diğer"),
    INCOMING_CALL(1, "Gelen arama"),
    MISSED_CALL(2, "Cevapsız arama"),
    VOICEMAIL(3, "Sesli mesaj"),
    SOCIAL(4, "Sosyal"),
    SCHEDULE(5, "Takvim"),
    EMAIL(6, "E-posta"),
    NEWS(7, "Haber"),
    HEALTH_AND_FITNESS(8, "Sağlık"),
    BUSINESS_AND_FINANCE(9, "Finans"),
    LOCATION(10, "Konum"),
    ENTERTAINMENT(11, "Eğlence");

    companion object {
        fun of(code: Int): Category = entries.firstOrNull { it.code == code } ?: OTHER
    }
}

object EventFlags {
    const val SILENT = 1 shl 0
    const val IMPORTANT = 1 shl 1
    const val PRE_EXISTING = 1 shl 2
    const val POSITIVE_ACTION = 1 shl 3
    const val NEGATIVE_ACTION = 1 shl 4
}

object CommandId {
    const val GET_NOTIFICATION_ATTRIBUTES = 0
    const val GET_APP_ATTRIBUTES = 1
    const val PERFORM_NOTIFICATION_ACTION = 2
}

object NotificationAttribute {
    const val APP_IDENTIFIER = 0
    const val TITLE = 1
    const val SUBTITLE = 2
    const val MESSAGE = 3
    const val MESSAGE_SIZE = 4
    const val DATE = 5
    const val POSITIVE_ACTION_LABEL = 6
    const val NEGATIVE_ACTION_LABEL = 7

    /** Bu alanlar istekte 2 baytlık maksimum uzunluk parametresi ister. */
    val WITH_MAX_LENGTH = setOf(TITLE, SUBTITLE, MESSAGE)
}

object AppAttribute {
    const val DISPLAY_NAME = 0
}

object ActionId {
    const val POSITIVE = 0
    const val NEGATIVE = 1
}

/** Control Point'e yazma hatasında iPhone'un döndürdüğü ATT hata kodları. */
object AncsError {
    const val UNKNOWN_COMMAND = 0xA0
    const val INVALID_COMMAND = 0xA1
    const val INVALID_PARAMETER = 0xA2
    const val ACTION_FAILED = 0xA3
}
