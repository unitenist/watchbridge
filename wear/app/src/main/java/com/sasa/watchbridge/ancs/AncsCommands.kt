package com.sasa.watchbridge.ancs

import java.io.ByteArrayOutputStream

/** Control Point'e yazılacak ANCS komutları. */
object AncsCommands {
    const val MAX_TEXT_LENGTH = 512

    /** Bir bildirim için istediğimiz alanlar. Yanıt bu sırayla gelir. */
    val NOTIFICATION_ATTRIBUTES = listOf(
        NotificationAttribute.APP_IDENTIFIER,
        NotificationAttribute.TITLE,
        NotificationAttribute.SUBTITLE,
        NotificationAttribute.MESSAGE,
        NotificationAttribute.DATE,
        NotificationAttribute.POSITIVE_ACTION_LABEL,
        NotificationAttribute.NEGATIVE_ACTION_LABEL,
    )

    fun getNotificationAttributes(uid: Int, maxLength: Int = MAX_TEXT_LENGTH): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(CommandId.GET_NOTIFICATION_ATTRIBUTES)
        out.writeU32LE(uid)
        for (attr in NOTIFICATION_ATTRIBUTES) {
            out.write(attr)
            if (attr in NotificationAttribute.WITH_MAX_LENGTH) out.writeU16LE(maxLength)
        }
        return out.toByteArray()
    }

    fun getAppAttributes(appId: String): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(CommandId.GET_APP_ATTRIBUTES)
        out.write(appId.toByteArray(Charsets.UTF_8))
        out.write(0) // null sonlandırıcı
        out.write(AppAttribute.DISPLAY_NAME)
        return out.toByteArray()
    }

    fun performNotificationAction(uid: Int, actionId: Int): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(CommandId.PERFORM_NOTIFICATION_ACTION)
        out.writeU32LE(uid)
        out.write(actionId)
        return out.toByteArray()
    }
}
