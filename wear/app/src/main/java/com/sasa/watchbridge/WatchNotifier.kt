package com.sasa.watchbridge

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.sasa.watchbridge.ancs.ActionId
import com.sasa.watchbridge.ancs.Category
import com.sasa.watchbridge.ancs.EventId
import com.sasa.watchbridge.ancs.IosNotification
import com.sasa.watchbridge.ancs.NotificationEvent

/** iPhone bildirimlerini saatte yerel Android bildirimi olarak gösterir. */
class WatchNotifier(private val context: Context) {

    companion object {
        const val SERVICE_ID = 1
        private const val TAG_IOS = "ios"
        private const val CH_SERVICE = "bridge_service"
        private const val CH_CALLS = "ios_calls"
        private const val CH_ALERTS = "ios_alerts"
        private const val CH_SILENT = "ios_silent"
    }

    private val nm = context.getSystemService(NotificationManager::class.java)

    init {
        nm.createNotificationChannels(
            listOf(
                NotificationChannel(CH_SERVICE, "Bağlantı durumu", NotificationManager.IMPORTANCE_MIN),
                NotificationChannel(CH_CALLS, "iPhone aramaları", NotificationManager.IMPORTANCE_HIGH).apply {
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 800, 400, 800, 400, 800, 400, 800)
                },
                NotificationChannel(CH_ALERTS, "iPhone bildirimleri", NotificationManager.IMPORTANCE_HIGH).apply {
                    enableVibration(true)
                },
                NotificationChannel(CH_SILENT, "iPhone bildirimleri (sessiz)", NotificationManager.IMPORTANCE_LOW),
            )
        )
    }

    fun serviceNotification(text: String): Notification =
        NotificationCompat.Builder(context, CH_SERVICE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("WatchBridge")
            .setContentText(text)
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    context, 0, Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

    fun updateService(text: String) {
        nm.notify(SERVICE_ID, serviceNotification(text))
    }

    fun show(event: NotificationEvent, n: IosNotification, appName: String) {
        val isCall = event.category == Category.INCOMING_CALL
        val channel = when {
            isCall -> CH_CALLS
            event.isSilent || event.isPreExisting -> CH_SILENT
            else -> CH_ALERTS
        }
        val title = n.title.ifBlank { appName }
        val body = listOf(n.subtitle, n.message).filter { it.isNotBlank() }.joinToString("\n")

        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSubText(appName)
            .setAutoCancel(true)
            .setOnlyAlertOnce(event.eventId == EventId.MODIFIED)

        if (isCall) {
            builder.setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true) // iPhone "kaldırıldı" olayı gelince kapanır
        }

        if (event.hasPositiveAction) {
            val label = n.positiveLabel.ifBlank { if (isCall) "Cevapla" else "Tamam" }
            builder.addAction(0, label, actionIntent(n.uid, ActionId.POSITIVE))
        }
        if (event.hasNegativeAction) {
            val label = n.negativeLabel.ifBlank { if (isCall) "Reddet" else "Temizle" }
            builder.addAction(0, label, actionIntent(n.uid, ActionId.NEGATIVE))
        }

        nm.notify(TAG_IOS, n.uid, builder.build())
    }

    fun cancel(uid: Int) = nm.cancel(TAG_IOS, uid)

    fun cancelAllIos() {
        nm.activeNotifications.filter { it.tag == TAG_IOS }.forEach { nm.cancel(TAG_IOS, it.id) }
    }

    private fun actionIntent(uid: Int, actionId: Int): PendingIntent {
        val intent = Intent(context, BridgeService::class.java)
            .setAction(BridgeService.ACTION_NOTIFICATION_ACTION)
            .putExtra(BridgeService.EXTRA_UID, uid)
            .putExtra(BridgeService.EXTRA_ACTION_ID, actionId)
        return PendingIntent.getService(
            context,
            (uid shl 1) or actionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
