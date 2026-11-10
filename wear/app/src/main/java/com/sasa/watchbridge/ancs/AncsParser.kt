package com.sasa.watchbridge.ancs

/** Notification Source'tan gelen 8 baytlık olay. */
data class NotificationEvent(
    val eventId: EventId,
    val flags: Int,
    val category: Category,
    val categoryCount: Int,
    val uid: Int,
) {
    val isSilent get() = flags and EventFlags.SILENT != 0
    val isImportant get() = flags and EventFlags.IMPORTANT != 0
    val isPreExisting get() = flags and EventFlags.PRE_EXISTING != 0
    val hasPositiveAction get() = flags and EventFlags.POSITIVE_ACTION != 0
    val hasNegativeAction get() = flags and EventFlags.NEGATIVE_ACTION != 0
}

object NotificationSourceParser {
    fun parse(b: ByteArray): NotificationEvent? {
        if (b.size < 8) return null
        val event = EventId.of(b[0].u()) ?: return null
        return NotificationEvent(
            eventId = event,
            flags = b[1].u(),
            category = Category.of(b[2].u()),
            categoryCount = b[3].u(),
            uid = readU32LE(b, 4),
        )
    }
}

/** Data Source'tan birleştirilmiş tam yanıt. */
data class DataSourceResponse(
    val commandId: Int,
    val uid: Int?,          // GET_NOTIFICATION_ATTRIBUTES için
    val appId: String?,     // GET_APP_ATTRIBUTES için
    val attributes: Map<Int, String>,
)

/** Saatte gösterilecek, alanları doldurulmuş iPhone bildirimi. */
data class IosNotification(
    val uid: Int,
    val appId: String,
    val title: String,
    val subtitle: String,
    val message: String,
    val date: String,
    val positiveLabel: String,
    val negativeLabel: String,
) {
    companion object {
        fun from(r: DataSourceResponse): IosNotification? {
            val uid = r.uid ?: return null
            val a = r.attributes
            return IosNotification(
                uid = uid,
                appId = a[NotificationAttribute.APP_IDENTIFIER].orEmpty(),
                title = a[NotificationAttribute.TITLE].orEmpty(),
                subtitle = a[NotificationAttribute.SUBTITLE].orEmpty(),
                message = a[NotificationAttribute.MESSAGE].orEmpty(),
                date = a[NotificationAttribute.DATE].orEmpty(),
                positiveLabel = a[NotificationAttribute.POSITIVE_ACTION_LABEL].orEmpty(),
                negativeLabel = a[NotificationAttribute.NEGATIVE_ACTION_LABEL].orEmpty(),
            )
        }
    }
}

/**
 * Data Source yanıtları MTU'dan büyük olunca birden fazla BLE paketine bölünür.
 * Bu sınıf parçaları biriktirir ve beklenen tüm alanlar geldiğinde yanıtı döndürür.
 * Aynı anda yalnızca bir istek beklemede olmalıdır.
 */
class DataSourceAssembler {
    private var buffer = ByteArray(0)
    private var command = -1
    private var attributeCount = 0

    val isWaiting get() = command >= 0

    fun begin(commandId: Int, expectedAttributes: Int) {
        buffer = ByteArray(0)
        command = commandId
        attributeCount = expectedAttributes
    }

    fun reset() {
        buffer = ByteArray(0)
        command = -1
        attributeCount = 0
    }

    fun feed(chunk: ByteArray): DataSourceResponse? {
        if (command < 0) return null
        buffer += chunk
        val result = tryParse(buffer) ?: return null
        reset()
        return result
    }

    private fun tryParse(d: ByteArray): DataSourceResponse? {
        if (d.isEmpty() || d[0].u() != command) return null
        var off: Int
        var uid: Int? = null
        var appId: String? = null
        when (command) {
            CommandId.GET_NOTIFICATION_ATTRIBUTES -> {
                if (d.size < 5) return null
                uid = readU32LE(d, 1)
                off = 5
            }
            CommandId.GET_APP_ATTRIBUTES -> {
                val end = d.indexOfByte(0, 1)
                if (end < 0) return null
                appId = String(d, 1, end - 1, Charsets.UTF_8)
                off = end + 1
            }
            else -> return null
        }
        val attrs = LinkedHashMap<Int, String>()
        repeat(attributeCount) {
            if (off + 3 > d.size) return null
            val id = d[off].u()
            val len = readU16LE(d, off + 1)
            off += 3
            if (off + len > d.size) return null
            attrs[id] = String(d, off, len, Charsets.UTF_8)
            off += len
        }
        return DataSourceResponse(command, uid, appId, attrs)
    }
}
